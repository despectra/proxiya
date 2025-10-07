package ru.despectra.proxiya.app.component.pane.proxy.create

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.StateFlow
import ru.despectra.proxiya.app.component.pane.PaneComponent
import ru.despectra.proxiya.app.data.proxy.ProxyType
import ru.despectra.proxiya.app.ui.viewmodel.CreateProxyFormVo

interface CreateProxyComponent : PaneComponent {
    val formState: StateFlow<CreateProxyFormVo>
    val submitResultState: StateFlow<CreateProxyFormVo.SubmitResult>

    fun updateName(name: String)
    fun updateProxyType(proxyType: ProxyType)
    fun updatePort(port: String)
    fun updateAdditionalParams(additionalParams: String)
    fun submit()

    interface Factory {
        fun create(componentContext: ComponentContext): CreateProxyComponent
    }
}