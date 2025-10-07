package ru.despectra.proxiya.app.component.pane

import kotlinx.serialization.Serializable

@Serializable
sealed class PaneConfig {

    @Serializable
    data object CreateProxyForm : PaneConfig()

    @Serializable
    data object SettingsPane : PaneConfig()

    @Serializable
    data class ProxyMainPane(val proxyId: String) : PaneConfig()
}