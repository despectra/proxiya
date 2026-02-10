package ru.despectra.proxiya.app.data.interception

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.despectra.proxiya.app.service.ProxyDataService
import ru.despectra.proxiya.proto.data.interception.InterceptedRequestNotification
import ru.despectra.proxiya.proto.data.interception.InterceptedResponseNotification
import ru.despectra.proxiya.proto.data.interception.RequestProcessingResult
import ru.despectra.proxiya.proto.data.interception.ResponseProcessingResult
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap

typealias RequestId = String

class ProxyInterceptionRepository(
    private val proxyDataService: ProxyDataService,
) : Closeable {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // TODO use
    private val requestProcessingResultFlow = MutableSharedFlow<RequestProcessingResult>()
    // TODO use
    private val responseProcessingResultFlow = MutableSharedFlow<ResponseProcessingResult>()

    private val requestsMap = ConcurrentHashMap<RequestId, InterceptedRequestNotification>()
    private val responsesMap = ConcurrentHashMap<RequestId, InterceptedResponseNotification>()

    private val interceptedRequestsIdsFlow = MutableStateFlow<List<InterceptedRequestKey>>(emptyList())

    init {
        scope.launch {
            proxyDataService
                .interceptedRequestsFlow(requestProcessingResultFlow)
                .onEach { interceptedRequestNotification ->
                    if (!interceptedRequestNotification.hasRequestId()) {
                        return@onEach
                    }

                    val requestId = interceptedRequestNotification.requestId

                    // TODO maybe index by proxy id 🤔
                    // val proxyId = interceptedRequestNotification.proxyId

                    if (!requestsMap.containsKey(requestId)) {
                        requestsMap[requestId] = interceptedRequestNotification
                        interceptedRequestsIdsFlow.update {
                            it + InterceptedRequestKey(requestId, hasResponse = false)
                        }
                    }
                }
                .flowOn(Dispatchers.Default)
                .collect()

            proxyDataService
                .interceptedResponsesFlow(responseProcessingResultFlow)
                .onEach { interceptedResponseNotification ->
                    if (!interceptedResponseNotification.hasRequestId()) {
                        return@onEach
                    }

                    val requestId = interceptedResponseNotification.requestId
                    if (requestsMap.containsKey(requestId) &&
                        !responsesMap.containsKey(requestId)
                    ) {
                        responsesMap[requestId] = interceptedResponseNotification
                        interceptedRequestsIdsFlow.update {
                            it.map { interceptedRequestKey ->
                                if (interceptedRequestKey.requestId == requestId) {
                                    interceptedRequestKey.copy(hasResponse = true)
                                } else {
                                    interceptedRequestKey
                                }
                            }
                        }
                    }
                }
                .flowOn(Dispatchers.Default)
                .collect()
        }
    }

    fun interceptedRequestsByProxyId(
        proxyId: String,
    ): Flow<List<InterceptedRequest>> {
        return interceptedRequestsIdsFlow.map { requestKeys ->
            requestKeys
                .mapNotNull { key ->
                    val requestId = key.requestId
                    val request = requestsMap[requestId] ?: return@mapNotNull null
                    if (request.proxyId != proxyId) {
                        return@mapNotNull null
                    }

                    InterceptedRequest(
                        requestId = requestId,
                        originRequest = request,
                        associatedResponse = responsesMap[requestId],
                    )
                }
        }.flowOn(Dispatchers.Default)
    }

    override fun close() {
        scope.cancel()
    }
}