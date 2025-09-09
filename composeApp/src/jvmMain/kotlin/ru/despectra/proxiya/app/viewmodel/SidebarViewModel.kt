package ru.despectra.proxiya.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.despectra.proxiya.app.data.ProxiesRepository
import ru.despectra.proxiya.proto.control.ProxyControl
import ru.despectra.proxiya.proto.control.ProxyManagementServiceOuterClass.ProxiesListState

class SidebarViewModel(
    private val proxiesRepository: ProxiesRepository,
) : ViewModel() {

    private val _proxiesFlow = MutableStateFlow<List<ProxyItemVo>>(emptyList())
    val proxiesFlow = _proxiesFlow

    private val _selectedProxyId = MutableStateFlow("")
    val selectedProxyId = _selectedProxyId

    fun initialize() {
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

    private fun ProxiesListState.toViewObject(): List<ProxyItemVo> {
        return this.proxyInfoList.map { proxyInfo ->
            ProxyItemVo(
                id = proxyInfo.proxyId,
                name = proxyInfo.proxyName,
                type = when (proxyInfo.proxyType) {
                    ProxyControl.ProxyType.PROXY_HTTPS -> "HTTPS"
                    ProxyControl.ProxyType.PROXY_SOCKS_5 -> "Socks5"
                    ProxyControl.ProxyType.UNRECOGNIZED -> "Unknown proxy type"
                },
                port = proxyInfo.port.toString(),
                status = when (proxyInfo.status) {
                    ProxyControl.ProxyStatus.PROXY_STATUS_UNKNOWN,
                    ProxyControl.ProxyStatus.PROXY_STATUS_STOPPED -> ProxyItemVo.Status.IDLE

                    ProxyControl.ProxyStatus.PROXY_STATUS_RUNNING -> ProxyItemVo.Status.ACTIVE

                    ProxyControl.ProxyStatus.PROXY_STATUS_STARTING,
                    ProxyControl.ProxyStatus.PROXY_STATUS_STOPPING -> ProxyItemVo.Status.TRANSIENT

                    ProxyControl.ProxyStatus.PROXY_STATUS_ERROR,
                    ProxyControl.ProxyStatus.UNRECOGNIZED -> ProxyItemVo.Status.ERROR
                }
            )
        }
    }
}