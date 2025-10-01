package ru.despectra.proxiya.controller.repository

import org.slf4j.LoggerFactory
import ru.despectra.proxiya.controller.store.ProxyStore
import ru.despectra.proxiya.controller.utils.asException
import ru.despectra.proxiya.controller.utils.asProtoTimestamp
import ru.despectra.proxiya.controller.utils.generateProxyId
import ru.despectra.proxiya.controller.utils.makeGenericError
import ru.despectra.proxiya.controller.utils.makeGenericErrorAsException
import ru.despectra.proxiya.proto.errors.ErrorType
import ru.despectra.proxiya.proto.errors.GenericError
import ru.despectra.proxiya.proto.proxy.ProcessState
import ru.despectra.proxiya.proto.proxy.ProxyParams
import ru.despectra.proxiya.proto.proxy.ProxyState
import ru.despectra.proxiya.proto.proxy.copy
import ru.despectra.proxiya.proto.proxy.processStatus
import ru.despectra.proxiya.proto.proxy.processStatusOrNull
import ru.despectra.proxiya.proto.proxy.proxyFunctionalityStatus
import ru.despectra.proxiya.proto.proxy.proxyInstance
import java.lang.AutoCloseable
import java.time.Instant

typealias ProxyId = String
typealias ProcessId = Int

/**
 * Главный репозиторий прокси-инстансов, предоставляющий функциональность для их создания,
 * обновления, запуска, остановки и удаления.
 * Также поддерживает реактивный стрим текущего списка экземпляров прокси.
 *
 * Использует [MitmproxyProcessManager] для работы с процессами прокси и управления их жизненным циклом.
 *
 * @property mitmproxyProcessManager Экземпляр [MitmproxyProcessManager], используемый для управления процессами прокси.
 */
class ProxyRepository(
    private val mitmproxyProcessManager: MitmproxyProcessManager,
    private val proxyStore: ProxyStore,
    private val proxyParamsValidator: ProxyParamsValidator,
) : AutoCloseable {

    val proxiesFlow = proxyStore.proxiesListFlow

    private val logger = LoggerFactory.getLogger(ProxyRepository::class.java)

    suspend fun createProxy(proxyParams: ProxyParams): Result<ProxyId> {
        return runCatching {
            logger.debug("Creating proxy")
            proxyParamsValidator.validate(proxyParams).getOrThrow()
            val proxyId = generateProxyId(proxyParams)
            logger.debug("new proxy id: $proxyId")
            val proxyInstance = proxyInstance {
                this.proxyId = proxyId
                this.params = proxyParams
                this.processStatus = processStatus {
                    state = ProcessState.PROCESS_STATE_STOPPED
                }
                this.functionalityStatus = proxyFunctionalityStatus {
                    state = ProxyState.PROXY_STATE_STOPPED
                }
                val now = Instant.now()
                this.createdAt = now.asProtoTimestamp()
                this.updatedAt = now.asProtoTimestamp()
            }
            proxyStore.saveProxy(proxyInstance)
            logger.debug("Created proxy with id: $proxyId")

            if (proxyParams.autostart) {
                logger.debug("Autostarting proxy with id: $proxyId")
                startProxy(proxyId)
            }
            proxyId
        }
    }

    suspend fun updateProxy(proxyId: ProxyId, updatedParams: ProxyParams): Result<Unit> {
        return runCatching {
            logger.debug("Updating proxy with id: $proxyId")
            proxyParamsValidator.validate(updatedParams).getOrThrow()
            proxyStore.updateProxy(proxyId) {
                copy {
                    this.params = updatedParams
                    this.updatedAt = Instant.now().asProtoTimestamp()
                }
            }
            logger.debug("Updated proxy with id: $proxyId")
        }
    }

    suspend fun removeProxy(proxyId: ProxyId): Result<Unit> {
        return runCatching {
            logger.debug("Removing proxy with id: $proxyId")

            stopProxy(proxyId)
            proxyStore.removeProxy(proxyId)

            logger.debug("Removed proxy with id: $proxyId")
        }
    }

    suspend fun startProxy(proxyId: ProxyId): Result<ProcessId> {
        return runCatching {
            logger.debug("Starting proxy with id: $proxyId")
            val proxyInstance = proxyStore.getProxy(proxyId)
                ?: throw makeGenericErrorAsException(
                    type = ErrorType.PROXY_NOT_FOUND,
                    message = "Proxy $proxyId not found"
                )

            if (proxyInstance.processStatusOrNull?.hasProcessId() == true) {
                val pid = proxyInstance.processStatus.processId
                logger.debug("Proxy with id: $proxyId already started in process $pid")
                return@runCatching pid
            }
            val startupStatusChannel = mitmproxyProcessManager.startProxyProcess(proxyId, proxyInstance.params)
            var processId: ProcessId? = null
            var error: GenericError? = null
            for (status in startupStatusChannel) {
                val updatedState = status.state
                when (status) {
                    is MitmproxyProcessManager.StartupStatus.Starting -> {
                        processId = null
                        error = null
                    }

                    is MitmproxyProcessManager.StartupStatus.Started -> {
                        processId = status.processId
                        error = null
                    }

                    is MitmproxyProcessManager.StartupStatus.Failed -> {
                        processId = null
                        error = makeGenericError(ErrorType.PROXY_STARTUP_FAILURE, status.errorMessage)
                    }
                }
                proxyStore.updateProxy(proxyId) {
                    copy {
                        processStatus = processStatus {
                            this.state = updatedState
                            processId?.let { this.processId = it } ?: this.clearProcessId()
                            error?.let { this.error = it } ?: this.clearError()
                        }
                    }
                }
            }
            if (processId == null) {
                logger.error("Failed to start proxy with id: $proxyId", error?.asException())
                throw error?.asException()
                    ?: makeGenericErrorAsException(ErrorType.PROXY_STARTUP_FAILURE, "Unknown error")
            }
            logger.debug("Proxy with id: $proxyId started in process $processId")
            processId
        }
    }

    suspend fun stopProxy(proxyId: ProxyId): Result<Unit> {
        return runCatching {
            logger.debug("Stopping proxy with id: $proxyId")
            val proxyInstance = proxyStore.getProxy(proxyId)
                ?: throw makeGenericErrorAsException(
                    type = ErrorType.PROXY_NOT_FOUND,
                    message = "Proxy $proxyId not found"
                )

            if (proxyInstance.processStatusOrNull?.hasProcessId() == false) {
                logger.debug("Proxy with id: $proxyId is not started")
                return@runCatching
            }

            val statusChannel = mitmproxyProcessManager.stopProxyProcess(proxyId)
            var error: GenericError? = null
            for (status in statusChannel) {
                when (status) {
                    is MitmproxyProcessManager.ShutdownStatus.Stopping -> {
                        error = null
                    }

                    is MitmproxyProcessManager.ShutdownStatus.Stopped -> {
                        error = null
                    }

                    is MitmproxyProcessManager.ShutdownStatus.Failed -> {
                        error = makeGenericError(ErrorType.PROXY_SHUTDOWN_FAILURE, status.errorMessage)
                    }
                }
                proxyStore.updateProxy(proxyId) {
                    copy {
                        processStatus = processStatus {
                            this.state = status.state
                            this.clearProcessId()
                            error?.let { this.error = it } ?: this.clearError()
                        }
                    }
                }
            }
            if (error != null) {
                logger.error("Failed to stop proxy with id: $proxyId", error.asException())
                throw error.asException()
            }
            logger.debug("Proxy with id: $proxyId stopped")
        }
    }

    suspend fun setProxyInstanceFunctionalityStatus(proxyId: ProxyId, state: ProxyState, error: GenericError?): Result<Unit> {
        return runCatching {
            proxyStore.updateProxy(proxyId) {
                copy {
                    this.functionalityStatus = proxyFunctionalityStatus {
                        this.state = state
                        if (error != null) {
                            this.error = error
                        } else {
                            this.clearError()
                        }
                    }
                    this.updatedAt = Instant.now().asProtoTimestamp()
                }
            }
        }
    }

    suspend fun setProxyInstanceStreamingStatus(proxyId: ProxyId, isStreamingEnabled: Boolean): Result<Unit> {
        return runCatching {
            proxyStore.updateProxy(proxyId) {
                copy {
                    this.functionalityStatus = proxyFunctionalityStatus {
                        this.streamingEnabled = isStreamingEnabled
                    }
                    this.updatedAt = Instant.now().asProtoTimestamp()
                }
            }
        }
    }

    override fun close() {
        // TODO триггерить close зависимостей или нет 🤔
        //mitmproxyProcessManager.close()
        //_proxiesFlow.update { emptyList() }
    }

    private fun generateProxyId(params: ProxyParams): ProxyId {
        return generateProxyId(
            name = params.name,
            params = mapOf(
                "proxy_type" to params.type.name,
                "port" to params.port.toString(),
            )
        )
    }
}