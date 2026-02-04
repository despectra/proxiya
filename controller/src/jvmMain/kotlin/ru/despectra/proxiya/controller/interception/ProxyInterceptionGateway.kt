package ru.despectra.proxiya.controller.interception

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import ru.despectra.proxiya.controller.store.ProxyStore
import ru.despectra.proxiya.proto.data.interception.InterceptedRequestNotification
import ru.despectra.proxiya.proto.data.interception.InterceptedResponseNotification
import ru.despectra.proxiya.proto.data.interception.ProcessingKind
import ru.despectra.proxiya.proto.data.interception.RequestProcessingResult
import ru.despectra.proxiya.proto.data.interception.ResponseProcessingResult
import ru.despectra.proxiya.proto.data.interception.requestProcessingResult
import ru.despectra.proxiya.proto.data.interception.responseProcessingResult
import ru.despectra.proxiya.proto.proxy.ProcessState
import ru.despectra.proxiya.proto.proxy.functionalityStatusOrNull
import ru.despectra.proxiya.proto.proxy.processStatusOrNull
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap

@JvmInline
private value class PreFilterResult private constructor (private val value: Boolean) {
    val isAllowed: Boolean get() = value
    companion object {
        val ALLOW = PreFilterResult(true)
        val DENY = PreFilterResult(false)
    }
}

typealias RequestId = String

/**
 * Главный шлюз для распределения и роутинга перехваченных запросов/ответов между экземплярами прокси и внешним API.
 * Реализует сразу оба интерфейса: [InterceptedEventsProducer] и [InterceptedEventsConsumer].
 *
 * @property proxyStore хранилище проксей для наблюдения за списком активных прокси с включенным стримингом
 */
internal class ProxyInterceptionGateway(
    private val proxyStore: ProxyStore,
) : Closeable, InterceptedEventsProducer, InterceptedEventsConsumer {

    private val scope = CoroutineScope(SupervisorJob())

    private val requestsFlows = InterceptedEventFlows<InterceptedRequestNotification, RequestProcessingResult>()
    private val responsesFlows = InterceptedEventFlows<InterceptedResponseNotification, ResponseProcessingResult>()

    private val ongoingRequests = ConcurrentHashMap<RequestId, Long>()

    init {
        scope.launch {
            val enabledProxiesIds = proxyStore.proxiesListFlow
                .map { proxyInstances ->
                    proxyInstances.filter { proxy ->
                        if (proxy.processStatusOrNull?.hasState() != true) return@filter false
                        if (proxy.processStatusOrNull?.state != ProcessState.PROCESS_STATE_RUNNING) return@filter false
                        if (proxy.functionalityStatusOrNull?.streamingEnabled != true) return@filter false
                        true
                    }
                }
                .distinctUntilChanged()
                .map { proxyInstances ->
                    proxyInstances.map { it.proxyId }.toSet()
                }
                .stateIn(this)

            requestsFlows.input
                .preFilterEvents { request ->
                    if (!request.hasRequestId()) {
                        return@preFilterEvents request to PreFilterResult.DENY
                    }

                    if (!request.hasProxyId()) {
                        return@preFilterEvents request to PreFilterResult.DENY
                    }

                    val interceptedProxyId = request.proxyId
                    if (interceptedProxyId !in enabledProxiesIds.value) {
                        return@preFilterEvents request to PreFilterResult.DENY
                    }

                    if (!request.hasHttpRequest()) {
                        return@preFilterEvents request to PreFilterResult.DENY
                    }

                    request to PreFilterResult.ALLOW
                }
                // TODO processing chain engine
                .collect { (request, preFilterResult: PreFilterResult) ->
                    requestsFlows.returnToProxy(
                        requestProcessingResult {
                            requestId = request.requestId
                            processingKind = ProcessingKind.PROCESSING_KIND_NONE
                        }
                    )
                    if (preFilterResult.isAllowed) {
                        ongoingRequests[request.requestId] = System.currentTimeMillis()
                        requestsFlows.proceedToApplication(request)
                    }
                }

            responsesFlows.input
                .preFilterEvents { response ->
                    if (!response.hasRequestId()) {
                        return@preFilterEvents response to PreFilterResult.DENY
                    }

                    if (!response.hasProxyId()) {
                        return@preFilterEvents response to PreFilterResult.DENY
                    }

                    val interceptedProxyId = response.proxyId
                    if (interceptedProxyId !in enabledProxiesIds.value) {
                        return@preFilterEvents response to PreFilterResult.DENY
                    }

                    if (!response.hasHttpResponse()) {
                        return@preFilterEvents response to PreFilterResult.DENY
                    }

                    response to PreFilterResult.ALLOW
                }
                .collect { (response, preFilterResult: PreFilterResult) ->
                    responsesFlows.returnToProxy(
                        responseProcessingResult {
                            requestId = response.requestId
                            processingKind = ProcessingKind.PROCESSING_KIND_NONE
                        }
                    )
                    if (preFilterResult.isAllowed) {
                        ongoingRequests.remove(response.requestId)?.let {
                            responsesFlows.proceedToApplication(response)
                        }
                    }
                }
        }
    }

    private fun <T> Flow<T>.preFilterEvents(filterFn: (T) -> Pair<T, PreFilterResult>): Flow<Pair<T, PreFilterResult>> {
        return map {
            filterFn(it)
        }
    }

    override suspend fun acceptRequest(request: InterceptedRequestNotification): RequestProcessingResult {
        val deferredResult = scope.async {
            requestsFlows.result.first { it.requestId == request.requestId }
        }
        yield()
        requestsFlows.acceptFromProxy(request)
        return deferredResult.await() // TODO timeout handling
    }

    override suspend fun acceptResponse(response: InterceptedResponseNotification): ResponseProcessingResult {
        val deferredResult = scope.async {
            responsesFlows.result.first { it.requestId == response.requestId }
        }
        yield()
        responsesFlows.acceptFromProxy(response)
        return deferredResult.await() // TODO timeout handling
    }

    override fun requests(): Flow<InterceptedRequestNotification> {
        return requestsFlows.output.asSharedFlow()
    }

    override fun responses(): Flow<InterceptedResponseNotification> {
        return responsesFlows.output.asSharedFlow()
    }

    override fun close() {
        scope.cancel()
    }

    internal data class InterceptedEventFlows<EVENT, RESULT>(
        val input: MutableSharedFlow<EVENT> = MutableSharedFlow(),
        val output: MutableSharedFlow<EVENT> = MutableSharedFlow(),
        val result: MutableSharedFlow<RESULT> = MutableSharedFlow(),
    ) {
        suspend fun acceptFromProxy(event: EVENT) {
            input.emit(event)
        }

        suspend fun returnToProxy(result: RESULT) {
            this.result.emit(result)
        }

        suspend fun proceedToApplication(event: EVENT) {
            output.emit(event)
        }
    }
}