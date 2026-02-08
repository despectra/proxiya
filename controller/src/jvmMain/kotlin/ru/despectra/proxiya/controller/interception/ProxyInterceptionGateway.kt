package ru.despectra.proxiya.controller.interception

import com.google.protobuf.Timestamp
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.slf4j.LoggerFactory
import ru.despectra.proxiya.controller.repository.ProxyId
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

private typealias RequestId = String

/**
 * Класс для распределения и роутинга перехваченных запросов/ответов между экземплярами прокси и приложением-потребителем.
 * Реализует интерфейсы [InterceptedEventsProducer] и [InterceptedEventsConsumer].
 *
 * @property proxyStore хранилище проксей для наблюдения за списком активных прокси с включенным стримингом
 */
internal class ProxyInterceptionGateway(
    private val proxyStore: ProxyStore,
) : Closeable, InterceptedEventsProducer, InterceptedEventsConsumer {

    private val logger = LoggerFactory.getLogger(ProxyInterceptionGateway::class.java)

    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("ProxyInterceptionGateway"))

    private val requestsFlows = InterceptedEventFlows<InterceptedRequestNotification, RequestProcessingResult>()
    private val responsesFlows = InterceptedEventFlows<InterceptedResponseNotification, ResponseProcessingResult>()

    private val ongoingRequests = ConcurrentHashMap<RequestId, Timestamp>()

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
                .onEach {
                    logger.info("Enabled proxies changed: ${it.joinToString { it.params.name }}")
                }
                .map { proxyInstances ->
                    proxyInstances.map { it.proxyId }.toSet()
                }
                .stateIn(this)

            requestsFlows.input
                .preFilterRequests(enabledProxiesIds)
                // TODO processing chain engine
                .collect { (request, preFilterResult: PreFilterResult) ->
                    requestsFlows.routeBackToProxy(
                        requestProcessingResult {
                            requestId = request.requestId
                            processingKind = ProcessingKind.PROCESSING_KIND_NONE
                        }
                    )
                    if (preFilterResult.isAllowed) {
                        ongoingRequests[request.requestId] = request.timestamp
                        logger.debug("Routing request to application: ${request.requestId}")
                        requestsFlows.routeToApplication(request)
                    }
                }

            responsesFlows.input
                .preFilterResponses(enabledProxiesIds)
                // TODO processing chain engine
                .collect { (response, preFilterResult: PreFilterResult) ->
                    responsesFlows.routeBackToProxy(
                        responseProcessingResult {
                            requestId = response.requestId
                            processingKind = ProcessingKind.PROCESSING_KIND_NONE
                        }
                    )
                    if (preFilterResult.isAllowed) {
                        ongoingRequests.remove(response.requestId)?.let {
                            logger.debug("Routing response to application: ${response.requestId}")
                            responsesFlows.routeToApplication(response)
                        } ?: let {
                            logger.debug("Response for unknown request: ${response.requestId}")
                        }
                    }
                }
        }
    }

    override suspend fun acceptRequest(request: InterceptedRequestNotification): RequestProcessingResult {
        val deferredResult = scope.async {
            requestsFlows.result.first { it.requestId == request.requestId }
                .apply {
                    logger.debug(
                        "routing request back to proxy: request id = {}, result = {}",
                        request.requestId,
                        processingKind
                    )
                }
        }
        yield()
        logger.debug("Accepting intercepted request: request id = {}", request.requestId)
        requestsFlows.acceptFromProxy(request)
        return deferredResult.await() // TODO timeout handling
    }

    override suspend fun acceptResponse(response: InterceptedResponseNotification): ResponseProcessingResult {
        val deferredResult = scope.async {
            responsesFlows.result.first { it.requestId == response.requestId }
                .apply {
                    logger.debug(
                        "routing response back to proxy: request id = {}, result = {}",
                        response.requestId,
                        processingKind
                    )
                }
        }
        yield()
        logger.info("Accepting intercepted response: request id = ${response.requestId}")
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

    private fun Flow<InterceptedRequestNotification>.preFilterRequests(
        enabledProxiesIds: StateFlow<Set<ProxyId>>,
    ): Flow<Pair<InterceptedRequestNotification, PreFilterResult>> {
        return map { request ->
            if (!request.hasRequestId()) {
                logger.debug("[PRE_FILTER] 🚫 Request has no request id")
                return@map request to PreFilterResult.DENY
            }

            if (!request.hasProxyId()) {
                logger.debug("[PRE_FILTER] 🚫 Request has no associated proxy id")
                return@map request to PreFilterResult.DENY
            }

            val interceptedProxyId = request.proxyId
            if (interceptedProxyId !in enabledProxiesIds.value) {
                logger.debug("[PRE_FILTER] 🚫 Request is associated with inactive proxy id {}", interceptedProxyId)
                return@map request to PreFilterResult.DENY
            }

            if (!request.hasHttpRequest()) {
                logger.debug("[PRE_FILTER] 🚫 Request has no http payload")
                return@map request to PreFilterResult.DENY
            }

            request to PreFilterResult.ALLOW
        }
    }

    private fun Flow<InterceptedResponseNotification>.preFilterResponses(
        enabledProxiesIds: StateFlow<Set<ProxyId>>,
    ): Flow<Pair<InterceptedResponseNotification, PreFilterResult>> {
        return map { response ->
            if (!response.hasRequestId()) {
                logger.debug("[PRE_FILTER] 🚫 Response has no request id")
                return@map response to PreFilterResult.DENY
            }

            if (!response.hasProxyId()) {
                logger.debug("[PRE_FILTER] 🚫 Response has no associated proxy id")
                return@map response to PreFilterResult.DENY
            }

            val interceptedProxyId = response.proxyId
            if (interceptedProxyId !in enabledProxiesIds.value) {
                logger.debug("[PRE_FILTER] 🚫 Response is associated with inactive proxy id {}", interceptedProxyId)
                return@map response to PreFilterResult.DENY
            }

            if (!response.hasHttpResponse()) {
                logger.debug("[PRE_FILTER] 🚫 Response has no http payload")
                return@map response to PreFilterResult.DENY
            }

            response to PreFilterResult.ALLOW
        }
    }

    internal data class InterceptedEventFlows<EVENT, RESULT>(
        val input: MutableSharedFlow<EVENT> = MutableSharedFlow(),
        val output: MutableSharedFlow<EVENT> = MutableSharedFlow(),
        val result: MutableSharedFlow<RESULT> = MutableSharedFlow(),
    ) {
        suspend fun acceptFromProxy(event: EVENT) {
            input.emit(event)
        }

        suspend fun routeBackToProxy(result: RESULT) {
            this.result.emit(result)
        }

        suspend fun routeToApplication(event: EVENT) {
            output.emit(event)
        }
    }
}