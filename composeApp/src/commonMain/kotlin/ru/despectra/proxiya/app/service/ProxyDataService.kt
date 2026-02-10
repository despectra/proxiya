package ru.despectra.proxiya.app.service

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.flow.Flow
import ru.despectra.proxiya.proto.data.interception.InterceptedRequestNotification
import ru.despectra.proxiya.proto.data.interception.InterceptedResponseNotification
import ru.despectra.proxiya.proto.data.interception.RequestProcessingResult
import ru.despectra.proxiya.proto.data.interception.ResponseProcessingResult
import ru.despectra.proxiya.proto.service.data.ProxyDataServiceGrpcKt

class ProxyDataService(
    val config: GrpcConfig,
) : GrpcServiceWrapper<ProxyDataServiceGrpcKt.ProxyDataServiceCoroutineStub>() {
    override fun createChannel(): ManagedChannel {
        return ManagedChannelBuilder
            .forAddress(config.host, config.port)
            .usePlaintext()
            .build()
    }

    override fun createServiceStub(
        channel: ManagedChannel
    ): ProxyDataServiceGrpcKt.ProxyDataServiceCoroutineStub {
        return ProxyDataServiceGrpcKt.ProxyDataServiceCoroutineStub(channel)
    }

    fun interceptedRequestsFlow(
        processingResultFlow: Flow<RequestProcessingResult>
    ): Flow<InterceptedRequestNotification> {
        val service = getService()
        return service.streamInterceptedRequests(processingResultFlow)
    }

    fun interceptedResponsesFlow(
        processingResultFlow: Flow<ResponseProcessingResult>
    ): Flow<InterceptedResponseNotification> {
        val service = getService()
        return service.streamInterceptedResponses(processingResultFlow)
    }
}