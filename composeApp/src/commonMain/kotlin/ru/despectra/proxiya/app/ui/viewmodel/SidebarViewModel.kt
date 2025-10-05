package ru.despectra.proxiya.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.despectra.proxiya.app.data.proxy.ProxiesRepository
import ru.despectra.proxiya.proto.proxy.ProcessState
import ru.despectra.proxiya.proto.proxy.ProxyType
import ru.despectra.proxiya.proto.service.control.ProxiesList
import java.util.logging.Logger

class SidebarViewModel(
    private val proxiesRepository: ProxiesRepository,
) : ViewModel() {

    private val logger = Logger.getLogger(SidebarViewModel::class.java.name)

    private val _proxiesFlow = MutableStateFlow<List<ProxyItemVo>>(emptyList())
    val proxiesFlow = _proxiesFlow

    private val _selectedProxyId = MutableStateFlow("")
    val selectedProxyId = _selectedProxyId

    init {
        viewModelScope.launch {
            proxiesRepository.proxiesFlow()
                .map { it.toViewObject() }
                .flowOn(Dispatchers.Default).collect {
                    _proxiesFlow.value = it
                }
        }
    }

    fun selectProxy(proxyId: String) {
        _selectedProxyId.value = proxyId
    }

    fun clearSelectedProxy() {
        _selectedProxyId.value = ""
    }

    private fun ProxiesList.toViewObject(): List<ProxyItemVo> {
        return this.proxyInstanceList.map { proxyInfo ->
            val params = proxyInfo.params
            logger.info("Proxy info: id = ${proxyInfo.proxyId}, " +
                    "name = ${params.name}, " +
                    "process state = ${proxyInfo.processStatus.state}, " +
                    "proxy state = ${proxyInfo.functionalityStatus.state}")
            ProxyItemVo(
                id = proxyInfo.proxyId,
                name = params.name,
                type = when (params.type) {
                    ProxyType.PROXY_TYPE_HTTP -> "HTTPS"
                    ProxyType.PROXY_TYPE_SOCKS5 -> "SOCKS5"
                    // TODO other types
                    else -> "Unsupported type"
                },
                port = params.port.toString(),
                status = when (proxyInfo.processStatus.state) {
                    ProcessState.PROCESS_STATE_STOPPED -> ProxyItemVo.Status.IDLE

                    ProcessState.PROCESS_STATE_STARTING,
                    ProcessState.PROCESS_STATE_STOPPING -> ProxyItemVo.Status.TRANSIENT

                    ProcessState.PROCESS_STATE_RUNNING -> ProxyItemVo.Status.ACTIVE

                    // TODO other types + ProxyState
                    else -> ProxyItemVo.Status.ERROR
                }
            )
        }
    }
}