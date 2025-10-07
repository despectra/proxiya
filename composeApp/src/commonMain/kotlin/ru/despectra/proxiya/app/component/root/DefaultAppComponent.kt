package ru.despectra.proxiya.app.component.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import ru.despectra.proxiya.app.component.pane.PaneChild
import ru.despectra.proxiya.app.component.pane.PaneChildFactory
import ru.despectra.proxiya.app.component.pane.PaneConfig
import ru.despectra.proxiya.app.component.sidebar.SidebarComponent
import ru.despectra.proxiya.app.component.sidebar.SidebarItem
import ru.despectra.proxiya.app.data.settings.AppSettingsRepository
import ru.despectra.proxiya.app.data.settings.ThemeMode

class DefaultAppComponent(
    private val componentContext: ComponentContext,
    private val appSettingsRepository: AppSettingsRepository,
    sidebarComponentFactory: SidebarComponent.Factory,
    private val paneChildFactory: PaneChildFactory,
) : AppComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val paneNavigation = SlotNavigation<PaneConfig>()

    override val appTheme: StateFlow<ThemeMode>
        get() = appSettingsRepository.settingsFlow
            .map { it.theme }
            .stateIn(scope, started = SharingStarted.Lazily, initialValue = ThemeMode.SYSTEM)

    override val sidebarComponent: SidebarComponent = sidebarComponentFactory.create(
        context = childContext(key = "Sidebar")
    )

    override val contentPane: Value<ChildSlot<*, PaneChild>> =
        childSlot(
            source = paneNavigation,
            serializer = PaneConfig.serializer(),
            initialConfiguration = { PaneConfig.CreateProxyForm },
            handleBackButton = false,
            key = "ContentPane",
            childFactory = paneChildFactory::create,
        )

    init {
        sidebarComponent.selectedSidebarItem
            .onEach { sidebarItem ->
                val paneConfig = when (sidebarItem) {
                    SidebarItem.CreateNewProxyItem -> PaneConfig.CreateProxyForm
                    SidebarItem.SettingsItem -> PaneConfig.SettingsPane
                    is SidebarItem.ProxyItem -> null
                    SidebarItem.SpringSpacerItem -> null
                } ?: return@onEach

                paneNavigation.activate(paneConfig)
            }
            .launchIn(scope)
    }

    class Factory(
        private val appSettingsRepository: AppSettingsRepository,
        private val sidebarComponentFactory: SidebarComponent.Factory,
        private val paneChildFactory: PaneChildFactory,
    ) : AppComponent.Factory {
        override fun create(context: ComponentContext): AppComponent {
            return DefaultAppComponent(
                context,
                appSettingsRepository,
                sidebarComponentFactory,
                paneChildFactory,
            )
        }
    }
}