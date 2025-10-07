package ru.despectra.proxiya.app.component.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import ru.despectra.proxiya.app.component.pane.proxy.create.CreateProxyComponent
import ru.despectra.proxiya.app.component.sidebar.SidebarComponent
import ru.despectra.proxiya.app.data.settings.ThemeMode

interface AppComponent {

    val appTheme: StateFlow<ThemeMode>
    val sidebarComponent: SidebarComponent
    val contentPane: Value<ChildSlot<*, PaneChild>>

    sealed class PaneChild {
        data class CreateProxyChild(val component: CreateProxyComponent) : PaneChild()
    }

    interface Factory {
        fun create(context: ComponentContext): AppComponent
    }
}