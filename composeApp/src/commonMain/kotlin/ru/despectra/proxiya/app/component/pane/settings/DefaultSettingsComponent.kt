package ru.despectra.proxiya.app.component.pane.settings

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.StateFlow
import ru.despectra.proxiya.app.data.settings.AppSettings
import ru.despectra.proxiya.app.data.settings.AppSettingsRepository
import ru.despectra.proxiya.app.data.settings.ThemeMode

class DefaultSettingsComponent(
    private val appSettingsRepository: AppSettingsRepository,
    private val componentContext: ComponentContext,
) : SettingsComponent, ComponentContext by componentContext {

    override val appSettings: StateFlow<AppSettings> = appSettingsRepository.settingsFlow

    override fun updateTheme(theme: ThemeMode) {
        appSettingsRepository.updateTheme(theme)
    }

    override fun updateAutoStartProxies(autoStartProxies: Boolean) {
        appSettingsRepository.updateAutoStartProxies(autoStartProxies)
    }

    class Factory(
        private val appSettingsRepository: AppSettingsRepository,
    ) : SettingsComponent.Factory {
        override fun create(componentContext: ComponentContext): SettingsComponent {
            return DefaultSettingsComponent(
                appSettingsRepository,
                componentContext,
            )
        }
    }
}