package ru.despectra.proxiya.app.component.sidebar

import ru.despectra.proxiya.app.ui.viewmodel.ProxyItemVo

sealed interface SidebarItem {
    val id: String get() = javaClass.simpleName

    data class ProxyItem(val vo: ProxyItemVo): SidebarItem {
        override val id: String = "ProxyItem#${vo.id}"
    }
    data object CreateNewProxyItem : SidebarItem
    data object SettingsItem : SidebarItem
    data object SpringSpacerItem : SidebarItem
}