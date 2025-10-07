package ru.despectra.proxiya.app.component.pane.proxy.create

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.despectra.proxiya.app.data.proxy.ProxiesRepository
import ru.despectra.proxiya.app.data.proxy.ProxyType
import ru.despectra.proxiya.app.ui.viewmodel.CreateProxyFormVo
import java.util.logging.Level
import java.util.logging.Logger

class DefaultCreateProxyComponent(
    private val componentContext: ComponentContext,
    private val proxiesRepository: ProxiesRepository,
) : CreateProxyComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val logger = Logger.getLogger(javaClass.simpleName)

    private val _formState = MutableStateFlow(CreateProxyFormVo.init)
    private val _submitResult = MutableStateFlow<CreateProxyFormVo.SubmitResult>(
        CreateProxyFormVo.SubmitResult.Init
    )

    override val formState: StateFlow<CreateProxyFormVo> = _formState.asStateFlow()
    override val submitResultState: StateFlow<CreateProxyFormVo.SubmitResult> = _submitResult.asStateFlow()

    override fun updateName(name: String) {
        _formState.value = _formState.value.copy(name = name)
    }

    override fun updateProxyType(proxyType: ProxyType) {
        _formState.value = _formState.value.copy(proxyType = proxyType)
    }

    override fun updatePort(port: String) {
        _formState.value = _formState.value.copy(port = port.filter { it.isDigit() })
    }

    override fun updateAdditionalParams(additionalParams: String) {
        _formState.value = _formState.value.copy(additionalParams = additionalParams)
    }

    override fun submit() {
        scope.launch {
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
                logger.log(Level.SEVERE, "Failed to create proxy", e)
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

    class Factory(
        private val proxiesRepository: ProxiesRepository,
    ) : CreateProxyComponent.Factory {
        override fun create(componentContext: ComponentContext): CreateProxyComponent {
            return DefaultCreateProxyComponent(
                componentContext, proxiesRepository
            )
        }
    }
}