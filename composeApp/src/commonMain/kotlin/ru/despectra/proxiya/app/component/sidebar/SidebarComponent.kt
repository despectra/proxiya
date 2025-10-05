package ru.despectra.proxiya.app.component.sidebar

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.Flow

interface SidebarComponent {

    val sidebarItems: Flow<List<SidebarItem>>
    val selectedSidebarItem: Flow<SidebarItem>

    fun onSelectItem(item: SidebarItem)

    interface Factory {
        fun create(context: ComponentContext): SidebarComponent
    }
}