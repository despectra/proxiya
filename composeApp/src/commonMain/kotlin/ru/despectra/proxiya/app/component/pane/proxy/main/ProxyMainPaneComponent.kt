package ru.despectra.proxiya.app.component.pane.proxy.main

import kotlinx.coroutines.flow.StateFlow
import ru.despectra.proxiya.app.component.ComponentFactory1
import ru.despectra.proxiya.app.ui.vo.ProxyHeaderVo

interface ProxyMainPaneComponent {

    val proxyHeader: StateFlow<ProxyHeaderVo>

    fun toggleProxy()

    fun toggleInterception()

    interface Factory : ComponentFactory1<ProxyMainPaneComponent, String>
}