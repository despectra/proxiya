package ru.despectra.proxiya.controller.grpc.instance

import ru.despectra.proxiya.proto.data.interception.InterceptedRequestNotification
import ru.despectra.proxiya.proto.data.interception.InterceptedResponseNotification
import ru.despectra.proxiya.proto.data.interception.RequestProcessingResult
import ru.despectra.proxiya.proto.data.interception.ResponseProcessingResult
import ru.despectra.proxiya.proto.data.interception.requestProcessingResult
import ru.despectra.proxiya.proto.data.interception.responseProcessingResult
import ru.despectra.proxiya.proto.service.instance.data.ProxyInstanceDataServiceGrpcKt

/**
 * Реализация gRPC сервиса для передачи перехваченных запросов и ответов из прокси-инстанса в контроллер
 * с получением действия-результата над перехваченным запросом или ответом.
 *
 * Методы этого сервиса вызываются из **mitmproxy-аддона**
 */
class ProxyInstanceDataServiceImpl(
) : ProxyInstanceDataServiceGrpcKt.ProxyInstanceDataServiceCoroutineImplBase() {

    override suspend fun onInterceptRequest(request: InterceptedRequestNotification): RequestProcessingResult {
        // TODO emit to streamed flows
        return requestProcessingResult {
            this.requestId = request.requestId
        }
    }

    override suspend fun onInterceptResponse(request: InterceptedResponseNotification): ResponseProcessingResult {
        // TODO emit to streamed flows
        return responseProcessingResult {
            this.requestId = request.requestId
        }
    }
}