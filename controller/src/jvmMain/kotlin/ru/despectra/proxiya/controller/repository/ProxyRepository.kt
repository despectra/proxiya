package ru.despectra.proxiya.controller.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.slf4j.LoggerFactory
import ru.despectra.proxiya.controller.utils.asException
import ru.despectra.proxiya.controller.utils.asProtoTimestamp
import ru.despectra.proxiya.controller.utils.generateProxyId
import ru.despectra.proxiya.controller.utils.makeGenericError
import ru.despectra.proxiya.controller.utils.makeGenericErrorAsException
import ru.despectra.proxiya.proto.errors.ErrorType
import ru.despectra.proxiya.proto.errors.GenericError
import ru.despectra.proxiya.proto.proxy.ProcessState
import ru.despectra.proxiya.proto.proxy.ProxyInstance
import ru.despectra.proxiya.proto.proxy.ProxyInstanceKt
import ru.despectra.proxiya.proto.proxy.ProxyParams
import ru.despectra.proxiya.proto.proxy.ProxyState
import ru.despectra.proxiya.proto.proxy.ProxyType
import ru.despectra.proxiya.proto.proxy.copy
import ru.despectra.proxiya.proto.proxy.processStatus
import ru.despectra.proxiya.proto.proxy.processStatusOrNull
import ru.despectra.proxiya.proto.proxy.proxyFunctionalityStatus
import ru.despectra.proxiya.proto.proxy.proxyInstance
import ru.despectra.proxiya.proto.proxy.reverseProxyParamsOrNull
import ru.despectra.proxiya.proto.proxy.transparentProxyParamsOrNull
import ru.despectra.proxiya.proto.proxy.upstreamProxyParamsOrNull
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
) : AutoCloseable {

    private val _proxiesFlow = MutableStateFlow<List<ProxyInstance>>(emptyList())
    val proxiesFlow = _proxiesFlow.asStateFlow()

    private val logger = LoggerFactory.getLogger(ProxyRepository::class.java)
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun createProxy(proxyParams: ProxyParams): Result<ProxyId> {
        return scope.async {
            runCatching {
                logger.debug("Creating proxy with id:")
                validateProxyParams(proxyParams).getOrThrow()
                val proxyId = generateProxyId(proxyParams)
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
                _proxiesFlow.update { it + proxyInstance }
                logger.debug("Created proxy with id: $proxyId")

                if (proxyParams.autostart) {
                    logger.debug("Automatically starting proxy: $proxyId")
                    startProxy(proxyId)
                }
                proxyId
            }
        }.await()
    }

    suspend fun updateProxy(proxyId: ProxyId, updatedParams: ProxyParams): Result<Unit> {
        return scope.async {
            runCatching {
                val proxyInstance = tryFindProxyInstance(proxyId)
                validateProxyParams(updatedParams).getOrThrow()

                val updatedProxyInstance = proxyInstance.copy {
                    this.params = updatedParams
                    this.updatedAt = Instant.now().asProtoTimestamp()
                }
                _proxiesFlow.update { proxies ->
                    proxies.filterNot { it.proxyId == proxyId } + updatedProxyInstance
                }
            }
        }.await()
    }

    suspend fun removeProxy(proxyId: ProxyId): Result<Unit> {
        return scope.async {
            runCatching {
                val proxyInstance = tryFindProxyInstance(proxyId)
                stopProxy(proxyId)
                _proxiesFlow.update { it - proxyInstance }
            }
        }.await()
    }

    suspend fun startProxy(proxyId: ProxyId): Result<ProcessId> {
        return scope.async {
            runCatching {
                val proxyInstance = tryFindProxyInstance(proxyId)
                if (proxyInstance.processStatusOrNull?.hasProcessId() == true) {
                    return@runCatching proxyInstance.processStatus.processId
                }
                val statusChannel = mitmproxyProcessManager.startProxyProcess(proxyId, proxyInstance.params)
                var processId: ProcessId? = null
                var error: GenericError? = null
                for (status in statusChannel) {
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
                    updateSingleProxy(proxyId) {
                        processStatus = processStatus {
                            this.state = updatedState
                            processId?.let { this.processId = it } ?: this.clearProcessId()
                            error?.let { this.error = it } ?: this.clearError()
                        }
                    }
                }
                if (processId == null) {
                    throw error?.asException()
                        ?: makeGenericErrorAsException(ErrorType.PROXY_STARTUP_FAILURE, "Unknown error")
                }
                processId
            }
        }.await()
    }

    suspend fun stopProxy(proxyId: ProxyId): Result<Unit> {
        return scope.async {
            runCatching {
                val proxyInstance = tryFindProxyInstance(proxyId)
                if (proxyInstance.processStatusOrNull?.hasProcessId() == false) {
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
                    updateSingleProxy(proxyId) {
                        processStatus = processStatus {
                            this.state = status.state
                            this.clearProcessId()
                            error?.let { this.error = it } ?: this.clearError()
                        }
                    }
                }
                if (error != null) {
                    throw error.asException()
                }
                Unit
            }
        }.await()
    }

    override fun close() {
        mitmproxyProcessManager.close()
        _proxiesFlow.update { emptyList() }
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

    private fun tryFindProxyInstance(proxyId: ProxyId): ProxyInstance {
        return _proxiesFlow.value.find { it.proxyId == proxyId }
            ?: throw makeGenericErrorAsException(
                type = ErrorType.PROXY_NOT_FOUND,
                message = "Proxy $proxyId not found"
            )
    }

    private fun updateSingleProxy(proxyId: ProxyId, updater: ProxyInstanceKt.Dsl.() -> Unit) =
        _proxiesFlow.update { proxies ->
            val proxy = proxies.find { it.proxyId == proxyId } ?: return@update proxies
            proxies.filterNot { it.proxyId == proxyId } + proxy.copy {
                updater(this)
                updatedAt = Instant.now().asProtoTimestamp()
            }
        }

    private fun validateProxyParams(proxyParams: ProxyParams): Result<Boolean> = runCatching {
        if (proxyParams.name.trim().isEmpty()) {
            throw makeGenericErrorAsException(
                ErrorType.PROXY_PARAMS_INCOMPLETE,
                "Proxy name is required"
            )
        }

        if (proxyParams.port !in 0..65535) {
            throw makeGenericErrorAsException(
                ErrorType.PROXY_PARAMS_INVALID,
                "Proxy port ${proxyParams.port} is incorrect"
            )
        }

        if (proxyParams.port in _proxiesFlow.value.map { it.params.port }.toSet()) {
            throw makeGenericErrorAsException(
                ErrorType.PROXY_PORT_ALREADY_IN_USE,
                "Proxy port ${proxyParams.port} is already in use"
            )
        }

        if (proxyParams.type == ProxyType.PROXY_TYPE_UPSTREAM && proxyParams.upstreamProxyParamsOrNull == null) {
            throw makeGenericErrorAsException(
                ErrorType.PROXY_PARAMS_INCOMPLETE,
                "Upstream proxy params are required"
            )
        }

        if (proxyParams.type == ProxyType.PROXY_TYPE_REVERSE && proxyParams.reverseProxyParamsOrNull == null) {
            throw makeGenericErrorAsException(
                ErrorType.PROXY_PARAMS_INCOMPLETE,
                "Reverse proxy params are required"
            )
        }

        if (proxyParams.type == ProxyType.PROXY_TYPE_TRANSPARENT && proxyParams.transparentProxyParamsOrNull == null) {
            throw makeGenericErrorAsException(
                ErrorType.PROXY_PARAMS_INCOMPLETE,
                "Transparent proxy params are required"
            )
        }

        true
    }
}