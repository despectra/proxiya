package ru.despectra.proxiya.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.despectra.proxiya.app.data.proxy.ProxiesRepository
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

}