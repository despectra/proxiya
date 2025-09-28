package ru.despectra.proxiya.controller.grpc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
import ru.despectra.proxiya.controller.data.ProxiyaGenericError
import ru.despectra.proxiya.controller.repository.ProxyRepository
import ru.despectra.proxiya.controller.utils.makeGenericError
import ru.despectra.proxiya.proto.errors.ErrorType
import ru.despectra.proxiya.proto.service.control.ControllerStatus
import ru.despectra.proxiya.proto.service.control.CreateProxyRequest
import ru.despectra.proxiya.proto.service.control.CreateProxyResult
import ru.despectra.proxiya.proto.service.control.DeleteProxyRequest
import ru.despectra.proxiya.proto.service.control.DeleteProxyResult
import ru.despectra.proxiya.proto.service.control.ProxiesList
import ru.despectra.proxiya.proto.service.control.ProxyControlServiceGrpcKt
import ru.despectra.proxiya.proto.service.control.StartProxyRequest
import ru.despectra.proxiya.proto.service.control.StartProxyResult
import ru.despectra.proxiya.proto.service.control.StopProxyRequest
import ru.despectra.proxiya.proto.service.control.StopProxyResult
import ru.despectra.proxiya.proto.service.control.StreamControllerStatusRequest
import ru.despectra.proxiya.proto.service.control.StreamProxiesRequest
import ru.despectra.proxiya.proto.service.control.UpdateProxyRequest
import ru.despectra.proxiya.proto.service.control.UpdateProxyResult
import ru.despectra.proxiya.proto.service.control.controllerStatus
import ru.despectra.proxiya.proto.service.control.createProxyResult
import ru.despectra.proxiya.proto.service.control.deleteProxyResult
import ru.despectra.proxiya.proto.service.control.paramsOrNull
import ru.despectra.proxiya.proto.service.control.proxiesList
import ru.despectra.proxiya.proto.service.control.startProxyResult
import ru.despectra.proxiya.proto.service.control.stopProxyResult
import ru.despectra.proxiya.proto.service.control.updateProxyResult

/**
 * Реализация gRPC сервиса для управления прокси.
 *
 * Методы этого сервиса вызываются из **UI-приложения**
 */
class ProxyControlServiceImpl(
    private val proxyRepository: ProxyRepository,
) : ProxyControlServiceGrpcKt.ProxyControlServiceCoroutineImplBase() {

    private val logger = LoggerFactory.getLogger(ProxyControlServiceImpl::class.java)

    override suspend fun createProxy(request: CreateProxyRequest): CreateProxyResult {
        logger.debug("Creating proxy with params: {}", request.paramsOrNull)
        val params = request.paramsOrNull ?: return createProxyResult {
            error = makeGenericError(ErrorType.PROXY_PARAMS_INCOMPLETE, "Proxy params missing")
        }
        return proxyRepository.createProxy(params).fold(
            onSuccess = { proxyId ->
                logger.debug("Created proxy with ID: {}", proxyId)
                createProxyResult {
                    this.proxyId = proxyId
                }
            },
            onFailure = { error ->
                logger.error("Failed to create proxy", error)
                createProxyResult {
                    this.error = when (error) {
                        is ProxiyaGenericError -> error.genericError
                        else -> makeGenericError(
                            ErrorType.INTERNAL_ERROR,
                            "Unknown failure while creating proxy: ${error.message}"
                        )
                    }
                }
            }
        )
    }

    override suspend fun updateProxy(request: UpdateProxyRequest): UpdateProxyResult {
        logger.debug("Updating proxy with ID {} with params {}", request.proxyId, request.paramsOrNull)
        if (request.hasProxyId().not()) {
            return updateProxyResult {
                error = makeGenericError(ErrorType.PROXY_PARAMS_INCOMPLETE, "Proxy ID is missing")
            }
        }
        val params = request.paramsOrNull ?: return updateProxyResult {
            error = makeGenericError(ErrorType.PROXY_PARAMS_INCOMPLETE, "Proxy params missing")
        }
        return proxyRepository.updateProxy(request.proxyId, params).fold(
            onSuccess = {
                logger.debug("Updated proxy with ID: {}", request.proxyId)
                updateProxyResult {}
            },
            onFailure = { error ->
                logger.error("Failed to update proxy with ID: {}", request.proxyId, error)
                updateProxyResult {
                    this.error = when (error) {
                        is ProxiyaGenericError -> error.genericError
                        else -> makeGenericError(
                            ErrorType.INTERNAL_ERROR,
                            "Unknown failure while updating proxy: ${error.message}"
                        )
                    }
                }
            }
        )
    }

    override suspend fun deleteProxy(request: DeleteProxyRequest): DeleteProxyResult {
        logger.debug("Deleting proxy with ID {}", request.proxyId)
        if (request.hasProxyId().not()) {
            return deleteProxyResult {
                error = makeGenericError(ErrorType.PROXY_PARAMS_INCOMPLETE, "Proxy ID is missing")
            }
        }
        return proxyRepository.removeProxy(request.proxyId).fold(
            onSuccess = {
                logger.debug("Deleted proxy with ID: {}", request.proxyId)
                deleteProxyResult {}
            },
            onFailure = { error ->
                logger.error("Failed to delete proxy with ID: {}", request.proxyId, error)
                deleteProxyResult {
                    this.error = when (error) {
                        is ProxiyaGenericError -> error.genericError
                        else -> makeGenericError(
                            ErrorType.INTERNAL_ERROR,
                            "Unknown failure while deleting proxy: ${error.message}"
                        )
                    }
                }
            }
        )
    }

    override suspend fun startProxy(request: StartProxyRequest): StartProxyResult {
        logger.debug("Starting proxy with ID {}", request.proxyId)
        if (request.hasProxyId().not()) {
            return startProxyResult {
                error = makeGenericError(ErrorType.PROXY_PARAMS_INCOMPLETE, "Proxy ID is missing")
            }
        }
        return proxyRepository.startProxy(request.proxyId).fold(
            onSuccess = { processId ->
                logger.debug("Started proxy with ID: {}, PID {}", request.proxyId, processId)
                startProxyResult {
                    this.processId = processId
                }
            },
            onFailure = { error ->
                logger.error("Failed to start proxy with ID: {}", request.proxyId, error)
                startProxyResult {
                    this.error = when (error) {
                        is ProxiyaGenericError -> error.genericError
                        else -> makeGenericError(
                            ErrorType.INTERNAL_ERROR,
                            "Unknown failure while starting proxy: ${error.message}"
                        )
                    }
                }
            }
        )
    }

    override suspend fun stopProxy(request: StopProxyRequest): StopProxyResult {
        logger.debug("Stopping proxy with ID {}", request.proxyId)
        if (request.hasProxyId().not()) {
            return stopProxyResult {
                error = makeGenericError(ErrorType.PROXY_PARAMS_INCOMPLETE, "Proxy ID is missing")
            }
        }
        return proxyRepository.stopProxy(request.proxyId).fold(
            onSuccess = {
                logger.debug("Stopped proxy with ID: {}", request.proxyId)
                stopProxyResult {}
            },
            onFailure = { error ->
                logger.error("Failed to stop proxy with ID: {}", request.proxyId, error)
                stopProxyResult {
                    this.error = when (error) {
                        is ProxiyaGenericError -> error.genericError
                        else -> makeGenericError(
                            ErrorType.INTERNAL_ERROR,
                            "Unknown failure while stopping proxy: ${error.message}"
                        )
                    }
                }
            }
        )
    }

    override fun streamProxies(request: StreamProxiesRequest): Flow<ProxiesList> {
        return proxyRepository.proxiesFlow.map { proxies ->
            proxiesList {
                proxyInstance.addAll(proxies)
            }
        }
    }

    override fun streamControllerStatus(request: StreamControllerStatusRequest): Flow<ControllerStatus> {
        return flow {
            emit(controllerStatus {}) // TODO send controller health
        }
    }
}
