package ru.despectra.proxiya.app.component.pane.settings

import kotlinx.coroutines.flow.StateFlow
import ru.despectra.proxiya.app.component.ComponentFactory0
import ru.despectra.proxiya.app.data.settings.AppSettings
import ru.despectra.proxiya.app.data.settings.ThemeMode

interface SettingsComponent {

    val appSettings: StateFlow<AppSettings>

    fun updateTheme(theme: ThemeMode)
    fun updateAutoStartProxies(autoStartProxies: Boolean)

    interface Factory : ComponentFactory0<SettingsComponent>
}