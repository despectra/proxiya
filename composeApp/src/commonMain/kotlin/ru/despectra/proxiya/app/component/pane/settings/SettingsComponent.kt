package ru.despectra.proxiya.app.component.pane.settings

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.StateFlow
import ru.despectra.proxiya.app.data.settings.AppSettings
import ru.despectra.proxiya.app.data.settings.ThemeMode

interface SettingsComponent {

    val appSettings: StateFlow<AppSettings>

    fun updateTheme(theme: ThemeMode)
    fun updateAutoStartProxies(autoStartProxies: Boolean)

    interface Factory {
        fun create(componentContext: ComponentContext): SettingsComponent
    }
}