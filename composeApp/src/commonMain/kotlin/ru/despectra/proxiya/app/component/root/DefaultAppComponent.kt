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
import kotlinx.serialization.Serializable
import ru.despectra.proxiya.app.component.pane.proxy.create.CreateProxyComponent
import ru.despectra.proxiya.app.component.root.AppComponent.PaneChild
import ru.despectra.proxiya.app.component.sidebar.SidebarComponent
import ru.despectra.proxiya.app.component.sidebar.SidebarItem
import ru.despectra.proxiya.app.data.settings.AppSettingsRepository
import ru.despectra.proxiya.app.data.settings.ThemeMode

class DefaultAppComponent(
    private val componentContext: ComponentContext,
    private val appSettingsRepository: AppSettingsRepository,
    sidebarComponentFactory: SidebarComponent.Factory,
    private val createProxyComponentFactory: CreateProxyComponent.Factory,
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
            childFactory = ::createPaneChild,
        )

    init {
        sidebarComponent.selectedSidebarItem
            .onEach { sidebarItem ->
                val paneConfig = when (sidebarItem) {
                    SidebarItem.CreateNewProxyItem -> PaneConfig.CreateProxyForm
                    is SidebarItem.ProxyItem -> null
                    SidebarItem.SettingsItem -> null
                    SidebarItem.SpringSpacerItem -> null
                } ?: return@onEach

                paneNavigation.activate(paneConfig)
            }
            .launchIn(scope)
    }

    private fun createPaneChild(
        config: PaneConfig,
        childContext: ComponentContext,
    ): PaneChild {
        return when(config) {
            is PaneConfig.CreateProxyForm -> {
                PaneChild.CreateProxyChild(
                    createProxyComponentFactory.create(childContext)
                )
            }
        }
    }

    @Serializable
    private sealed class PaneConfig {

        @Serializable
        data object CreateProxyForm : PaneConfig()
    }

    class Factory(
        private val appSettingsRepository: AppSettingsRepository,
        private val sidebarComponentFactory: SidebarComponent.Factory,
        private val createProxyComponentFactory: CreateProxyComponent.Factory,
    ) : AppComponent.Factory {
        override fun create(context: ComponentContext): AppComponent {
            return DefaultAppComponent(
                context,
                appSettingsRepository,
                sidebarComponentFactory,
                createProxyComponentFactory,
            )
        }
    }
}