package ru.despectra.proxiya.app.component.pane.proxy.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.despectra.proxiya.app.data.interception.ProxyInterceptionRepository
import ru.despectra.proxiya.app.data.proxy.ProxiesRepository
import ru.despectra.proxiya.app.ui.vo.ProxyHeaderVo
import ru.despectra.proxiya.app.ui.vo.ProxyInterceptionWaterfallVo
import ru.despectra.proxiya.proto.proxy.ProcessState
import ru.despectra.proxiya.proto.proxy.ProcessStatus
import ru.despectra.proxiya.proto.proxy.ProxyInstance
import ru.despectra.proxiya.proto.proxy.ProxyParams
import ru.despectra.proxiya.proto.proxy.ProxyState
import ru.despectra.proxiya.proto.proxy.ProxyType
import ru.despectra.proxiya.proto.proxy.processStatusOrNull
import java.util.logging.Level
import java.util.logging.Logger

class ProxyMainPaneComponentImpl(
    private val proxyId: String,
    private val proxiesRepository: ProxiesRepository,
    private val proxyInterceptionRepository: ProxyInterceptionRepository,
    private val interceptedRequestsFormatter: InterceptedRequestsFormatter,
    private val componentContext: ComponentContext,
) : ProxyMainPaneComponent, ComponentContext by componentContext {

    private val logger = Logger.getLogger(javaClass.simpleName)
    private val scope = coroutineScope()

    private val _proxyHeader = MutableStateFlow(ProxyHeaderVo.init)

    private val currentProxy = proxiesRepository.proxiesFlow()
        .map { proxiesList ->
            proxiesList.proxyInstanceList.find { it.proxyId == proxyId }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    private val currentWaterfall = proxyInterceptionRepository
        .interceptedRequestsByProxyId(proxyId)
        .map { interceptedRequests ->
            ProxyInterceptionWaterfallVo(
                interceptedRequests = interceptedRequests.mapNotNull { request ->
                    interceptedRequestsFormatter.format(
                        request = request.originRequest,
                        associatedResponse = request.associatedResponse,
                    )
                }
            )
        }
        .flowOn(Dispatchers.Default)
        .catch {
            logger.log(Level.SEVERE, "Error while formatting intercepted requests", it)
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = ProxyInterceptionWaterfallVo(emptyList())
        )

    override val proxyHeader: StateFlow<ProxyHeaderVo> = _proxyHeader.asStateFlow()
    override val interceptionWaterfall: StateFlow<ProxyInterceptionWaterfallVo> = currentWaterfall

    init {
        currentProxy
            .map { proxyInstance ->
                proxyInstance?.run {
                    ProxyHeaderVo(
                        name = params.name ?: "Unknown proxy",
                        description = "Proxy description for clarity", // TODO optional description
                        proxyParameters = params.formatProxyParams(),
                        proxyStatus = processStatus.formatStatus(),
                        processId = processStatus.formatProcessId(),
                        proxySwitchButton = formatProxySwitchButton(),
                        streamingButton = formatStreamingButton(),
                        filterButton = formatFilterButton(),
                        rewriteButton = formatRewriteButton(),
                        snapToLatestButton = formatSnapToLatestButton(),
                    )
                }
            }
            .flowOn(Dispatchers.Default)
            .catch {
                logger.log(Level.SEVERE, it.message, it)
            }
            .onEach { proxyHeaderVo ->
                proxyHeaderVo?.let {
                    _proxyHeader.value = it
                }
            }
            .launchIn(scope)
    }

    override fun toggleProxy() {
        scope.launch {
            try {
                val processState = currentProxy.value?.processStatusOrNull?.state
                if (processState == ProcessState.PROCESS_STATE_RUNNING) {
                    proxiesRepository.stopProxy(proxyId)
                } else if (processState == ProcessState.PROCESS_STATE_STOPPED) {
                    proxiesRepository.startProxy(proxyId)
                }
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Failed to toggle proxy", e)
            }
        }
    }

    override fun toggleInterception() {
        scope.launch {
            try {
                val isCurrentlyEnabled = currentProxy.value?.functionalityStatus?.interceptionEnabled == true
                proxiesRepository.toggleProxyInterception(
                    proxyId = proxyId,
                    isEnabled = !isCurrentlyEnabled
                )
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Failed to toggle interception", e)
            }
        }
    }

    class Factory(
        private val proxiesRepository: ProxiesRepository,
        private val proxyInterceptionRepository: ProxyInterceptionRepository,
        private val interceptedRequestsFormatter: InterceptedRequestsFormatter
    ) : ProxyMainPaneComponent.Factory {
        override fun create(
            param: String,
            componentContext: ComponentContext
        ): ProxyMainPaneComponent {
            return ProxyMainPaneComponentImpl(
                proxyId = param,
                proxiesRepository = proxiesRepository,
                proxyInterceptionRepository = proxyInterceptionRepository,
                interceptedRequestsFormatter = interceptedRequestsFormatter,
                componentContext = componentContext,
            )
        }
    }
}

private fun ProxyInstance.formatProxySwitchButton(): ProxyHeaderVo.ButtonVo {
    return when (processStatus.state) {
        ProcessState.PROCESS_STATE_ERROR,
        ProcessState.PROCESS_STATE_STOPPED -> ProxyHeaderVo.Defaults.proxySwitchButton.copy(isEnabled = true)
        ProcessState.PROCESS_STATE_STARTING -> ProxyHeaderVo.Defaults.proxySwitchButton.copy(
            text = "Start", isEnabled = false
        )
        ProcessState.PROCESS_STATE_RUNNING -> ProxyHeaderVo.Defaults.proxySwitchButton.copy(
            text = "Stop", isEnabled = true
        )
        ProcessState.PROCESS_STATE_STOPPING -> ProxyHeaderVo.Defaults.proxySwitchButton.copy(
            text = "Stop", isEnabled = false
        )

        ProcessState.PROCESS_STATE_UNSPECIFIED,
        ProcessState.UNRECOGNIZED -> ProxyHeaderVo.Defaults.proxySwitchButton.copy(isEnabled = false)
    }
}

private fun ProxyInstance.formatStreamingButton(): ProxyHeaderVo.ButtonVo {
    if (processStatus.state != ProcessState.PROCESS_STATE_RUNNING) {
        return ProxyHeaderVo.Defaults.streamingButton.copy(isEnabled = false)
    }

    return when (functionalityStatus.state) {
        ProxyState.PROXY_STATE_ACTIVE -> ProxyHeaderVo.Defaults.streamingButton.copy(
            icon = if (functionalityStatus.interceptionEnabled) {
                ProxyHeaderVo.ButtonIcon.Pause
            } else {
                ProxyHeaderVo.ButtonIcon.Play
            },
            isEnabled = true,
        )
        else -> ProxyHeaderVo.Defaults.streamingButton.copy(isEnabled = false)
    }
}

// TODO
private fun ProxyInstance.formatFilterButton(): ProxyHeaderVo.ButtonVo {
    return ProxyHeaderVo.Defaults.filterButton
}

// TODO
private fun ProxyInstance.formatRewriteButton(): ProxyHeaderVo.ButtonVo {
    return ProxyHeaderVo.Defaults.rewriteButton
}

//TODO
private fun ProxyInstance.formatSnapToLatestButton(): ProxyHeaderVo.SnapToLatestButtonVo {
    return ProxyHeaderVo.SnapToLatestButtonVo(state = ProxyHeaderVo.SnapToLatestButtonVo.State.Snapped)
}

private fun ProxyParams.formatProxyParams(): String {
    val proxyType = when (type) {
        ProxyType.PROXY_TYPE_HTTP -> "HTTPS"
        ProxyType.PROXY_TYPE_SOCKS5 -> "Socks5"
        ProxyType.PROXY_TYPE_UPSTREAM -> "Upstream"
        ProxyType.PROXY_TYPE_TRANSPARENT -> "Transparent"
        ProxyType.PROXY_TYPE_REVERSE -> "Reverse"
        ProxyType.PROXY_TYPE_UNSPECIFIED,
        ProxyType.UNRECOGNIZED -> ""
    }
    return "$proxyType on $port"
}

private fun ProcessStatus?.formatProcessId(): String {
    return if (this?.hasProcessId() == true) {
        "PID: $processId"
    } else {
        ""
    }
}

private fun ProcessStatus.formatStatus(): ProxyHeaderVo.ProxyStatus {
    return when (state) {
        ProcessState.PROCESS_STATE_STOPPED -> ProxyHeaderVo.ProxyStatus.Idle
        ProcessState.PROCESS_STATE_STARTING -> ProxyHeaderVo.ProxyStatus.Starting
        ProcessState.PROCESS_STATE_RUNNING -> ProxyHeaderVo.ProxyStatus.Running
        ProcessState.PROCESS_STATE_STOPPING -> ProxyHeaderVo.ProxyStatus.Stopping

        ProcessState.PROCESS_STATE_ERROR,
        ProcessState.PROCESS_STATE_UNSPECIFIED,
        ProcessState.UNRECOGNIZED -> ProxyHeaderVo.ProxyStatus.Error
    }
}
