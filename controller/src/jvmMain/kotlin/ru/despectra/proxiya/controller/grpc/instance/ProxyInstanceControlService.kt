package ru.despectra.proxiya.controller.grpc.instance

import com.google.protobuf.Empty
import com.google.protobuf.empty
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
) : ProxyInstanceControlServiceGrpcKt.ProxyInstanceControlServiceCoroutineImplBase() {

    override suspend fun notifyProxyStarted(request: ProxyStartedNotification): Empty {
        return empty {  } // TODO
    }

    override suspend fun notifyProxyStopped(request: ProxyStoppedNotification): Empty {
        return empty { } // TODO
    }

    override suspend fun notifyProxyError(request: ProxyErrorNotification): Empty {
        return empty { } // TODO
    }

    override suspend fun heartbeat(request: HeartbeatNotification): Empty {
        return empty { } // TODO
    }
}