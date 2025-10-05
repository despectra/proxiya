package ru.despectra.proxiya.app.component.root

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.StateFlow
import ru.despectra.proxiya.app.component.sidebar.SidebarComponent
import ru.despectra.proxiya.app.data.settings.ThemeMode

interface AppComponent {

    val appTheme: StateFlow<ThemeMode>
    val sidebarComponent: SidebarComponent

    interface Factory {
        fun create(context: ComponentContext): AppComponent
    }
}