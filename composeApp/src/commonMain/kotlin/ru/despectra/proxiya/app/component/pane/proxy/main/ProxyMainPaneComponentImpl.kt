package ru.despectra.proxiya.app.component.pane.proxy.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.despectra.proxiya.app.data.proxy.ProxiesRepository
import ru.despectra.proxiya.app.ui.viewmodel.ProxyItemVo
import ru.despectra.proxiya.app.ui.viewmodel.ProxyState
import ru.despectra.proxiya.proto.proxy.ProcessState
import ru.despectra.proxiya.proto.proxy.paramsOrNull
import java.util.logging.Level
import java.util.logging.Logger

class ProxyMainPaneComponentImpl(
    private val proxyId: String,
    private val proxiesRepository: ProxiesRepository,
    private val componentContext: ComponentContext,
) : ProxyMainPaneComponent, ComponentContext by componentContext {

    private val logger = Logger.getLogger(javaClass.simpleName)
    private val scope = coroutineScope()

    private val _proxyState = MutableStateFlow<ProxyState>(ProxyState.Loading)
    private val _isTransientState = MutableStateFlow(false)

    override val proxyState: StateFlow<ProxyState> = _proxyState.asStateFlow()
    override val isProxyInTransientState: StateFlow<Boolean> = _isTransientState.asStateFlow()

    init {
        proxiesRepository.proxiesFlow()
            .map { proxiesList ->
                val proxyInstance = proxiesList.proxyInstanceList.find { it.proxyId == proxyId }
                proxyInstance?.run {
                    ProxyState.Loaded(
                        name = paramsOrNull?.name ?: " <Unknown proxy>",
                        type = paramsOrNull?.type?.name ?: "unknown",
                        port = paramsOrNull?.port.toString(),
                        status = when (processStatus.state) {
                            ProcessState.PROCESS_STATE_RUNNING -> ProxyItemVo.Status.ACTIVE

                            ProcessState.PROCESS_STATE_ERROR -> ProxyItemVo.Status.ERROR

                            ProcessState.PROCESS_STATE_STOPPING,
                            ProcessState.PROCESS_STATE_STARTING -> ProxyItemVo.Status.TRANSIENT

                            ProcessState.PROCESS_STATE_STOPPED -> ProxyItemVo.Status.IDLE

                            // TODO improve status VO
                            else -> ProxyItemVo.Status.ERROR
                        },
                        processId = null
                    )
                } ?: ProxyState.Error("Proxy with ID $proxyId not found")
            }
            .flowOn(Dispatchers.Default)
            .catch {
                logger.log(Level.SEVERE, it.message, it)
            }
            .onEach {
                _proxyState.value = it
            }
            .launchIn(scope)
    }

    override fun startProxy() {
        scope.launch {
            _isTransientState.value = true
            try {
                proxiesRepository.startProxy(proxyId)
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Failed to start proxy", e)
                _proxyState.value = ProxyState.Error(e.message ?: "Failed to start proxy")
            } finally {
                _isTransientState.value = false
            }
        }
    }

    override fun stopProxy() {
        scope.launch {
            try {
                proxiesRepository.stopProxy(proxyId)
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Failed to stop proxy", e)
                _proxyState.value = ProxyState.Error(e.message ?: "Failed to stop proxy")
            }
        }
    }

    class Factory(
        private val proxiesRepository: ProxiesRepository,
    ) : ProxyMainPaneComponent.Factory {
        override fun create(
            param: String,
            componentContext: ComponentContext
        ): ProxyMainPaneComponent {
            return ProxyMainPaneComponentImpl(
                proxyId = param,
                proxiesRepository = proxiesRepository,
                componentContext = componentContext,
            )
        }
    }
}