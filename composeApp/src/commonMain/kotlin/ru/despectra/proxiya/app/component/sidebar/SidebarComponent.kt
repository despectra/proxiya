package ru.despectra.proxiya.app.component.sidebar

import kotlinx.coroutines.flow.Flow
import ru.despectra.proxiya.app.component.ComponentFactory0

interface SidebarComponent {

    val sidebarItems: Flow<List<SidebarItem>>
    val selectedSidebarItem: Flow<SidebarItem>

    fun onSelectItem(item: SidebarItem)

    interface Factory : ComponentFactory0<SidebarComponent>
}