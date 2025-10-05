package ru.despectra.proxiya.app.component.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.despectra.proxiya.app.component.sidebar.SidebarComponent
import ru.despectra.proxiya.app.data.settings.AppSettingsRepository
import ru.despectra.proxiya.app.data.settings.ThemeMode

class DefaultAppComponent(
    private val componentContext: ComponentContext,
    private val appSettingsRepository: AppSettingsRepository,
    sidebarComponentFactory: SidebarComponent.Factory,
) : AppComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()

    override val appTheme: StateFlow<ThemeMode>
        get() = appSettingsRepository.settingsFlow
            .map { it.theme }
            .stateIn(scope, started = SharingStarted.Lazily, initialValue = ThemeMode.SYSTEM)

    override val sidebarComponent: SidebarComponent = sidebarComponentFactory.create(
        context = childContext(key = "Sidebar")
    )

    class Factory(
        private val appSettingsRepository: AppSettingsRepository,
        private val sidebarComponentFactory: SidebarComponent.Factory,
    ) : AppComponent.Factory {
        override fun create(context: ComponentContext): AppComponent {
            return DefaultAppComponent(
                context,
                appSettingsRepository,
                sidebarComponentFactory,
            )
        }
    }
}