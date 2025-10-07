package ru.despectra.proxiya.app.component.pane.proxy.main

import kotlinx.coroutines.flow.StateFlow
import ru.despectra.proxiya.app.component.ComponentFactory1
import ru.despectra.proxiya.app.ui.viewmodel.ProxyState

interface ProxyMainPaneComponent {

    val proxyState: StateFlow<ProxyState>
    val isProxyInTransientState: StateFlow<Boolean>

    fun startProxy()
    fun stopProxy()

    interface Factory : ComponentFactory1<ProxyMainPaneComponent, String>
}