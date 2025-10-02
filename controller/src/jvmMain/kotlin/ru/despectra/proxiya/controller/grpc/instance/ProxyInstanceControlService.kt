package ru.despectra.proxiya.controller.grpc.instance

import com.google.protobuf.Empty
import com.google.protobuf.empty
import org.slf4j.LoggerFactory
import ru.despectra.proxiya.controller.repository.ProxyRepository
import ru.despectra.proxiya.proto.proxy.ProxyState
import ru.despectra.proxiya.proto.service.instance.control.HeartbeatNotification
import ru.despectra.proxiya.proto.service.instance.control.ProxyErrorNotification
import ru.despectra.proxiya.proto.service.instance.control.ProxyInstanceControlServiceGrpcKt
import ru.despectra.proxiya.proto.service.instance.control.ProxyStartedNotification
import ru.despectra.proxiya.proto.service.instance.control.ProxyStoppedNotification

/**
 * Реализация gRPC сервиса для оповещений контроллера о состоянии прокси-инстансов.
 *
 * Методы этого сервиса вызываются из mitmproxy-аддона
 */
class ProxyInstanceControlService(
    private val proxyRepository: ProxyRepository,
) : ProxyInstanceControlServiceGrpcKt.ProxyInstanceControlServiceCoroutineImplBase() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun notifyProxyStarted(request: ProxyStartedNotification): Empty {
        logger.debug("Notify proxy started: proxy ID ${request.proxyId}")
        if (!request.hasProxyId()) {
            logger.error("Proxy ID is missing")
            return empty {  }
        }
        runCatching {
            proxyRepository.setProxyInstanceFunctionalityStatus(
                proxyId = request.proxyId,
                state = ProxyState.PROXY_STATE_ACTIVE,
                error = null
            )
            logger.debug("Proxy started status set")
        }.onFailure {
            logger.error("Failed to set proxy started status", it)
        }
        return empty {  }
    }

    override suspend fun notifyProxyStopped(request: ProxyStoppedNotification): Empty {
        logger.debug("Notify proxy stopped: proxy ID ${request.proxyId}")
        if (!request.hasProxyId()) {
            logger.error("Proxy ID is missing")
            return empty {  }
        }
        runCatching {
            proxyRepository.setProxyInstanceFunctionalityStatus(
                proxyId = request.proxyId,
                state = ProxyState.PROXY_STATE_STOPPED,
                error = null
            )
            logger.debug("Proxy stopped status set")
        }.onFailure {
            logger.error("Failed to set proxy stopped status", it)
        }
        return empty { }
    }

    override suspend fun notifyProxyError(request: ProxyErrorNotification): Empty {
        logger.debug("Notify proxy error: proxy ID ${request.proxyId}")
        if (!request.hasProxyId()) {
            logger.error("Proxy ID is missing")
            return empty {  }
        }
        runCatching {
            proxyRepository.setProxyInstanceFunctionalityStatus(
                proxyId = request.proxyId,
                state = ProxyState.PROXY_STATE_ERROR,
                error = request.error
            )
            logger.debug("Proxy error set")
        }.onFailure {
            logger.error("Failed to set proxy error status", it)
        }
        return empty { }
    }

    override suspend fun heartbeat(request: HeartbeatNotification): Empty {
        return empty { } // TODO
    }
}