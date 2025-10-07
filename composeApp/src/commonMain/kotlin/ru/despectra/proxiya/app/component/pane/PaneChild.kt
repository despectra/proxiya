package ru.despectra.proxiya.app.component.pane

import ru.despectra.proxiya.app.component.pane.proxy.create.CreateProxyComponent
import ru.despectra.proxiya.app.component.pane.settings.SettingsComponent

sealed class PaneChild {
    data class CreateProxyFormChild(val component: CreateProxyComponent) : PaneChild()
    data class SettingsPaneChild(val component: SettingsComponent) : PaneChild()
}