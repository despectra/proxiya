package ru.despectra.proxiya.controller.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import ru.despectra.proxiya.proto.proxy.ProcessState
import ru.despectra.proxiya.proto.proxy.ProxyParams
import ru.despectra.proxiya.proto.proxy.ProxyType
import java.io.File
import java.lang.AutoCloseable
import java.util.concurrent.TimeUnit

/**
 * Класс для управления процессами запущенных прокси-инстансов mitmproxy.
 * Предоставляет методы для запуска и остановки процессов mitmproxy с заданными параметрами.
 * Реализует [AutoCloseable] для безопасного завершения всех запущенных процессов.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MitmproxyProcessManager : AutoCloseable {
    private val proxyProcesses: MutableMap<ProxyId, ProcessDescriptor> = mutableMapOf()

    private val logger = LoggerFactory.getLogger(javaClass)

    private val scope = CoroutineScope(Dispatchers.Default.limitedParallelism(1))
    private var isClosing: Boolean = false

    suspend fun startProxyProcess(proxyId: ProxyId, proxyParams: ProxyParams): ReceiveChannel<StartupStatus> {
        return scope.async {
            if (isClosing) {
                return@async produce {
                    send(StartupStatus.Failed("Proxy process manager is closing"))
                    close()
                }
            }
            proxyProcesses[proxyId]?.let { existingProcess ->
                return@async produce {
                    send(StartupStatus.Started(existingProcess.processId))
                    close()
                }
            }
            produce {
                send(StartupStatus.Starting)
                val resultStatus = startMitmproxyProcess(proxyId, proxyParams).fold(
                    onSuccess = { process ->
                        val processId = process.pid().toInt()
                        proxyProcesses[proxyId] = ProcessDescriptor(processId, process)
                        StartupStatus.Started(processId)
                    },
                    onFailure = {
                        StartupStatus.Failed(it.message ?: "Unknown error")
                    }
                )

                send(resultStatus)
                close()
            }
        }.await()
    }

    suspend fun stopProxyProcess(proxyId: ProxyId): ReceiveChannel<ShutdownStatus> {
        return scope.async {
            if (isClosing) {
                return@async produce {
                    send(ShutdownStatus.Failed("Proxy process manager is closing"))
                    close()
                }
            }
            val processDescriptor = proxyProcesses[proxyId]
                ?: return@async produce {
                    send(ShutdownStatus.Stopped)
                    close()
                }

            produce {
                send(ShutdownStatus.Stopping)
                try {
                    val process = processDescriptor.process
                    process.destroy()
                    val terminatedGracefully = process.waitFor(5, TimeUnit.SECONDS)
                    if (!terminatedGracefully) {
                        process.destroyForcibly()
                        val terminatedForcibly = process.waitFor(5, TimeUnit.SECONDS)
                        if (!terminatedForcibly) {
                            logger.error("mitmproxy process hasn't been terminated after 10 seconds")
                            send(
                                ShutdownStatus.Failed(
                                    "mitmproxy process hasn't been terminated after 10 seconds"
                                )
                            )
                            close()
                            return@produce
                        }
                    }
                    proxyProcesses.remove(proxyId)
                    send(ShutdownStatus.Stopped)
                } catch (e: Exception) {
                    send(ShutdownStatus.Failed(e.message ?: "Unknown error"))
                }

                close()
            }
        }.await()
    }

    private fun startMitmproxyProcess(proxyId: String, proxyParams: ProxyParams): Result<Process> {
        return runCatching {
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

            logger.debug("Starting mitmproxy with command: ${command.joinToString(" ")}")

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
                logger.info("mitmproxy process started successfully (PID: ${process.pid()})")
                process
            } else {
                logger.error("mitmproxy process failed to start")
                throw Exception("mitmproxy process failed to start")
            }
        }
    }

    override fun close() {
        runBlocking {
            scope.launch {
                isClosing = true
                proxyProcesses.forEach { (_, processDescriptor) ->
                    processDescriptor.process.destroy()
                }
                proxyProcesses.clear()
            }.join()
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

    private data class ProcessDescriptor(
        val processId: ProcessId,
        val process: Process,
    )
}