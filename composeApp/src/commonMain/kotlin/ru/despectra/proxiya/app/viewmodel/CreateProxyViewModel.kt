package ru.despectra.proxiya.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.despectra.proxiya.app.data.proxy.ProxyType

class CreateProxyViewModel : ViewModel() {

    private val _formState = MutableStateFlow(CreateProxyFormVo.init)
    val formState = _formState.asStateFlow()

    fun updateName(name: String) {
        _formState.value = _formState.value.copy(name = name)
    }

    fun updateProxyType(proxyType: ProxyType) {
        _formState.value = _formState.value.copy(proxyType = proxyType)
    }

    fun updatePort(port: String) {
        _formState.value = _formState.value.copy(port = port.filter { it.isDigit() })
    }

    fun updateAdditionalParams(additionalParams: String) {
        _formState.value = _formState.value.copy(additionalParams = additionalParams)
    }

    fun submit() {
        // TODO
    }
}