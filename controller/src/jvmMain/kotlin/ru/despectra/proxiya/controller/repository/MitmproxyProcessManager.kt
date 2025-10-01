package ru.despectra.proxiya.controller.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import ru.despectra.proxiya.controller.utils.MutexByKey
import ru.despectra.proxiya.proto.proxy.ProcessState
import ru.despectra.proxiya.proto.proxy.ProxyParams
import ru.despectra.proxiya.proto.proxy.ProxyType
import java.io.Closeable
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Класс для управления процессами запущенных прокси-инстансов mitmproxy.
 * Предоставляет методы для запуска и остановки процессов mitmproxy с заданными параметрами.
 * Реализует [Closeable] для безопасного завершения всех запущенных процессов.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MitmproxyProcessManager : Closeable {
    private val proxyProcesses = ConcurrentHashMap<ProxyId, ProxyProcessDescriptor>()
    private val mutexByKey = MutexByKey<ProxyId>()
    private var isClosing = AtomicBoolean(false)
    private val processOperationsCount = MutableStateFlow(0)

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun startProxyProcess(
        proxyId: ProxyId,
        proxyParams: ProxyParams,
    ): ReceiveChannel<StartupStatus> = coroutineScope {
        if (isClosing.get()) {
            return@coroutineScope produce {
                send(StartupStatus.Failed("Proxy process manager is closing"))
                close()
            }
        }

        produce {
            mutexByKey[proxyId].withLock {
                proxyProcesses[proxyId]?.let { existingProcess ->
                    send(StartupStatus.Started(existingProcess.processId))
                    close()
                    return@produce
                }

                try {
                    processOperationsCount.value++
                    send(StartupStatus.Starting)

                    val process = withContext(Dispatchers.IO) {
                        startMitmproxyProcess(proxyId, proxyParams)
                    }
                    val processId = process.pid().toInt()
                    proxyProcesses[proxyId] = ProxyProcessDescriptor(processId, process)

                    send(StartupStatus.Started(processId))
                } catch (e: Exception) {
                    proxyProcesses.remove(proxyId)
                    mutexByKey.remove(proxyId)
                    send(StartupStatus.Failed(e.message ?: "Unknown error"))
                } finally {
                    processOperationsCount.value--
                    close()
                }
            }
        }
    }

    suspend fun stopProxyProcess(
        proxyId: ProxyId
    ): ReceiveChannel<ShutdownStatus> = coroutineScope {
        if (isClosing.get()) {
            return@coroutineScope produce {
                send(ShutdownStatus.Failed("Proxy process manager is closing"))
                close()
            }
        }

        produce {
            mutexByKey[proxyId].withLock {
                val processDescriptor = proxyProcesses[proxyId]
                if (processDescriptor == null) {
                    send(ShutdownStatus.Stopped)
                    close()
                    return@produce
                }

                var stopped = false
                try {
                    processOperationsCount.value++
                    send(ShutdownStatus.Stopping)

                    withContext(Dispatchers.IO) {
                        stopMitmproxyProcess(processDescriptor.process)
                    }

                    stopped = true
                    send(ShutdownStatus.Stopped)
                } catch (e: Exception) {
                    send(ShutdownStatus.Failed(e.message ?: "Unknown error"))
                } finally {
                    if (stopped) {
                        proxyProcesses.remove(proxyId)
                        mutexByKey.remove(proxyId)
                    }
                    processOperationsCount.value--
                    close()
                }
            }
        }
    }

    private fun startMitmproxyProcess(proxyId: String, proxyParams: ProxyParams): Process {
        // Путь к аддону
        val addonPath = File("mitmproxy_addon/proxiya_addon.py").absolutePath // TODO from config

        val proxyMode = when (proxyParams.type) {
            ProxyType.PROXY_TYPE_SOCKS5 -> "socks5"
            else -> null // TODO support other types
        }

        // Команда для запуска mitmproxy
        val baseCommand = listOfNotNull(
            "mitmdump",
            proxyMode?.let { "--mode" }, proxyMode,
            "-s", addonPath,
            "-p", proxyParams.port.toString(),
            "--quiet",
        )

        val hostsFilteringOptions = when {
            proxyParams.hostsWhitelistList.isNotEmpty() -> {
                proxyParams.hostsWhitelistList.flatMap { listOf("--allow-host", it) }
            }

            proxyParams.hostsBlacklistList.isNotEmpty() -> {
                proxyParams.hostsBlacklistList.flatMap { listOf("--ignore-host", it) }
            }

            else -> {
                emptyList()
            }
        }

        val command = baseCommand + hostsFilteringOptions

        logger.debug("Starting mitmproxy for proxy $proxyId with command: ${command.joinToString(" ")}")

        val processBuilder = ProcessBuilder(command)
        processBuilder.directory(File("."))

        // переменные окружения
        processBuilder.environment().apply {
            put("PROXIYA_PROXY_ID", proxyId)
            put("PROXIYA_CONTROLLER_HOST", "localhost") // TODO from config
            put("PROXIYA_CONTROLLER_PORT", "50051") // TODO from config
            put("PROXIYA_USE_TLS", "false") // TODO TLS support
            put("PROXIYA_HEARTBEAT_INTERVAL", "2") // TODO from params
        }

        // Перенаправляем вывод в логи
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE)
        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE)

        val process = processBuilder.start()

        // Проверяем, что процесс запустился успешно
        if (process.isAlive) {
            logger.info("mitmproxy process for proxy $proxyId started successfully (PID: ${process.pid()})")
            return process
        } else {
            logger.error("mitmproxy process failed to start")
            throw Exception("mitmproxy process failed to start")
        }
    }

    private fun stopMitmproxyProcess(process: Process) {
        logger.debug("Stopping mitmproxy process (PID: ${process.pid()})")
        process.destroy()
        val terminatedGracefully = process.waitFor(5, TimeUnit.SECONDS)

        if (!terminatedGracefully) {
            logger.warn("mitmproxy process hasn't been stopped after 5 seconds, trying to kill forcibly")

            process.destroyForcibly()
            val terminatedForcibly = process.waitFor(5, TimeUnit.SECONDS)
            if (!terminatedForcibly) {
                logger.error("mitmproxy process hasn't been terminated after 10 seconds")
                throw Exception("mitmproxy process hasn't been terminated after 10 seconds")
            }
        }
        logger.info("mitmproxy process (PID: ${process.pid()}) stopped successfully")
    }

    override fun close() {
        runBlocking {
            if (isClosing.compareAndSet(false, true)) {
                logger.info("[SHUTDOWN] Closing mitmproxy process manager")
                processOperationsCount
                    .takeWhile { it > 0 }
                    .launchIn(this)
                    .join()

                logger.info("[SHUTDOWN] All ongoing process operations completed, destroying processes")
                proxyProcesses.forEach { (_, processDescriptor) ->
                    try {
                        stopMitmproxyProcess(processDescriptor.process)
                    } catch (e: Exception) {
                        logger.error("Error destroying process ${processDescriptor.processId}", e)
                    }
                }
                proxyProcesses.clear()
                mutexByKey.clear()
                logger.info("[SHUTDOWN] All processes destroyed")
            }
        }
    }

    sealed class StartupStatus(val state: ProcessState) {
        data object Starting : StartupStatus(ProcessState.PROCESS_STATE_STARTING)
        data class Started(val processId: ProcessId) : StartupStatus(ProcessState.PROCESS_STATE_RUNNING)
        data class Failed(val errorMessage: String) : StartupStatus(ProcessState.PROCESS_STATE_ERROR)
    }

    sealed class ShutdownStatus(val state: ProcessState) {
        data object Stopping : ShutdownStatus(ProcessState.PROCESS_STATE_STOPPING)
        data object Stopped : ShutdownStatus(ProcessState.PROCESS_STATE_STOPPED)
        data class Failed(val errorMessage: String) : ShutdownStatus(ProcessState.PROCESS_STATE_ERROR)
    }

    private data class ProxyProcessDescriptor(
        val processId: ProcessId,
        val process: Process,
    )
}