package ru.despectra.proxiya.controller.grpc

import kotlinx.coroutines.flow.Flow
import ru.despectra.proxiya.controller.interception.InterceptedEventsProducer
import ru.despectra.proxiya.proto.data.interception.InterceptedRequestNotification
import ru.despectra.proxiya.proto.data.interception.InterceptedResponseNotification
import ru.despectra.proxiya.proto.data.interception.RequestProcessingResult
import ru.despectra.proxiya.proto.data.interception.ResponseProcessingResult
import ru.despectra.proxiya.proto.service.data.ProxyDataServiceGrpcKt

/**
 * Реализация gRPC сервиса для двунаправленного стриминга данных перехваченных запросов и ответов от mitmproxy аддонов в UI
 *
 * Методы этого сервиса вызываются из **UI-приложения**
 */
class ProxyDataServiceImpl(
    private val interceptedEventsProducer: InterceptedEventsProducer,
) : ProxyDataServiceGrpcKt.ProxyDataServiceCoroutineImplBase() {
    override fun streamInterceptedRequests(
        requests: Flow<RequestProcessingResult>
    ): Flow<InterceptedRequestNotification> {
        return interceptedEventsProducer.requests()
    }

    override fun streamInterceptedResponses(
        requests: Flow<ResponseProcessingResult>
    ): Flow<InterceptedResponseNotification> {
        return interceptedEventsProducer.responses()
    }
}
