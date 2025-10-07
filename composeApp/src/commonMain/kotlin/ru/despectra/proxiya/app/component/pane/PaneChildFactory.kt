package ru.despectra.proxiya.app.component.pane

import com.arkivanov.decompose.ComponentContext
import ru.despectra.proxiya.app.component.pane.proxy.create.CreateProxyComponent
import ru.despectra.proxiya.app.component.pane.settings.SettingsComponent

class PaneChildFactory(
    private val createProxyComponentFactory: CreateProxyComponent.Factory,
    private val settingsComponentFactory: SettingsComponent.Factory,
) {
    fun create(
        config: PaneConfig,
        componentContext: ComponentContext
    ): PaneChild = when (config) {
        PaneConfig.CreateProxyForm -> PaneChild.CreateProxyFormChild(
            createProxyComponentFactory.create(componentContext)
        )
        PaneConfig.SettingsPane -> PaneChild.SettingsPaneChild(
            settingsComponentFactory.create(componentContext)
        )
    }
}