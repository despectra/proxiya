package ru.despectra.proxiya.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.despectra.proxiya.app.data.proxy.ProxiesRepository
import ru.despectra.proxiya.app.data.proxy.ProxyType
import java.util.logging.Logger

class CreateProxyViewModel(
    private val proxiesRepository: ProxiesRepository,
) : ViewModel() {

    private val logger = Logger.getLogger(CreateProxyViewModel::class.java.name)

    private val _formState = MutableStateFlow(CreateProxyFormVo.init)
    val formState = _formState.asStateFlow()

    private val _submitResult: MutableStateFlow<CreateProxyFormVo.SubmitResult> =
        MutableStateFlow(CreateProxyFormVo.SubmitResult.Init)
    val submitResult = _submitResult.asStateFlow()

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
        viewModelScope.launch {
            _submitResult.value = CreateProxyFormVo.SubmitResult.InProgress

            val formState = _formState.value.validated().getOrElse {
                _submitResult.value = CreateProxyFormVo.SubmitResult.Error(it.message ?: "Unknown error")
                return@launch
            }
            try {
                proxiesRepository.createProxy(
                    name = formState.name,
                    proxyType = formState.proxyType,
                    port = formState.port.toInt(),
                    additionalParams = formState.additionalParams,
                )
            } catch (e: Exception) {
                _submitResult.value = CreateProxyFormVo.SubmitResult.Error(e.message ?: "Unknown error")
                logger.log(java.util.logging.Level.SEVERE, "Failed to create proxy", e)
                return@launch
            }

            _submitResult.value = CreateProxyFormVo.SubmitResult.Success
            _formState.value = CreateProxyFormVo.init
        }
    }

    private fun CreateProxyFormVo.validated(): Result<CreateProxyFormVo> {
        if (name.isBlank()) {
            return Result.failure(Exception("Name must not be empty"))
        }

        if (port.isBlank()) {
            return Result.failure(Exception("Port must not be empty"))
        }

        if (port.toIntOrNull() == null) {
            return Result.failure(Exception("Port must be a number"))
        }

        return Result.success(this)
    }
}