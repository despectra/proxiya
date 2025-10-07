package ru.despectra.proxiya.app.component.root

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import ru.despectra.proxiya.app.component.ComponentFactory0
import ru.despectra.proxiya.app.component.pane.PaneChild
import ru.despectra.proxiya.app.component.sidebar.SidebarComponent
import ru.despectra.proxiya.app.data.settings.ThemeMode

interface AppComponent {

    val appTheme: StateFlow<ThemeMode>
    val sidebarComponent: SidebarComponent
    val contentPane: Value<ChildSlot<*, PaneChild>>

    interface Factory : ComponentFactory0<AppComponent>
}