package ru.despectra.proxiya.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.despectra.proxiya.app.data.proxy.ProxiesRepository
import ru.despectra.proxiya.proto.proxy.ProcessState
import ru.despectra.proxiya.proto.proxy.paramsOrNull
import ru.despectra.proxiya.proto.service.control.ProxiesList
import java.util.logging.Level
import java.util.logging.Logger

class ProxyMainViewModel(
    proxyId: String,
    private val proxiesRepository: ProxiesRepository,
) : ViewModel() {

    private val logger = Logger.getLogger(ProxyMainViewModel::class.java.name)

    private val _selectedProxyId = MutableStateFlow(proxyId)

    private val _proxyState = MutableStateFlow<ProxyState>(ProxyState.Loading)
    val proxyState = _proxyState.asStateFlow()

    private val _isStarting = MutableStateFlow(false)
    val isStarting = _isStarting.asStateFlow()

    init {
        subscribeToProxiesStream()
    }

    fun updateProxyId(newProxyId: String) {
        if (_selectedProxyId.value != newProxyId) {
            _proxyState.value = ProxyState.Loading
            _selectedProxyId.value = newProxyId
        }
    }

    private fun subscribeToProxiesStream() {
        viewModelScope.launch {
            try {
                combine(
                    proxiesRepository.proxiesFlow(),
                    _selectedProxyId
                ) { proxiesListState, currentProxyId ->
                    updateProxyStateFromStream(proxiesListState, currentProxyId)
                }
                .catch { e ->
                    logger.log(Level.SEVERE, "Error in proxies stream", e)
                    _proxyState.value = ProxyState.Error(e.message ?: "Stream error")
                }
                .collect { }
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Failed to subscribe to proxies stream", e)
                _proxyState.value = ProxyState.Error(e.message ?: "Subscription error")
            }
        }
    }

    private fun updateProxyStateFromStream(proxiesList: ProxiesList, currentProxyId: String) {
        try {
            logger.info("Updating proxy state from stream: proxy ID = $currentProxyId")
            // Ищем прокси по ID в списке
            val proxyInfo = proxiesList.proxyInstanceList.find { it.proxyId == currentProxyId }
            
            if (proxyInfo != null) {
                _proxyState.value = ProxyState.Loaded(
                    name = proxyInfo.params.name,
                    type = proxyInfo.paramsOrNull?.type?.name ?: "unknown",
                    port = proxyInfo.params.port.toString(),
                    status = when (proxyInfo.processStatus.state) {
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
            } else {
                _proxyState.value = ProxyState.Error("Proxy with ID $currentProxyId not found")
            }
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to update proxy state from stream", e)
            _proxyState.value = ProxyState.Error(e.message ?: "State update error")
        }
    }

    fun startProxy() {
        viewModelScope.launch {
            _isStarting.value = true
            try {
                proxiesRepository.startProxy(_selectedProxyId.value)
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Failed to start proxy", e)
                _proxyState.value = ProxyState.Error(e.message ?: "Failed to start proxy")
            } finally {
                _isStarting.value = false
            }
        }
    }

    fun stopProxy() {
        viewModelScope.launch {
            try {
                proxiesRepository.stopProxy(_selectedProxyId.value)
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Failed to stop proxy", e)
                _proxyState.value = ProxyState.Error(e.message ?: "Failed to stop proxy")
            }
        }
    }

    fun refresh() {
        // Состояние обновляется автоматически через stream
        // Можно добавить принудительное обновление если нужно
    }
}

sealed class ProxyState {
    object Loading : ProxyState()
    data class Loaded(
        val name: String,
        val type: String,
        val port: String,
        val status: ProxyItemVo.Status,
        val processId: String?
    ) : ProxyState()
    data class Error(val message: String) : ProxyState()
}
