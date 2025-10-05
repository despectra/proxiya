package ru.despectra.proxiya.app.component.sidebar

import ru.despectra.proxiya.app.ui.viewmodel.ProxyItemVo

sealed interface SidebarItem {
    data class ProxyItem(val vo: ProxyItemVo): SidebarItem
    data object CreateNewProxyItem : SidebarItem
    data object SettingsItem : SidebarItem
    data object SpringSpacerItem : SidebarItem
}