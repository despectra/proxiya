package ru.despectra.proxiya.app.component.pane.proxy.create

import kotlinx.coroutines.flow.StateFlow
import ru.despectra.proxiya.app.component.ComponentFactory0
import ru.despectra.proxiya.app.data.proxy.ProxyType
import ru.despectra.proxiya.app.ui.vo.CreateProxyFormVo

interface CreateProxyComponent {
    val formState: StateFlow<CreateProxyFormVo>
    val submitResultState: StateFlow<CreateProxyFormVo.SubmitResult>

    fun updateName(name: String)
    fun updateProxyType(proxyType: ProxyType)
    fun updatePort(port: String)
    fun updateAdditionalParams(additionalParams: String)
    fun submit()

    interface Factory : ComponentFactory0<CreateProxyComponent>
}