package ru.despectra.proxiya.app.component.sidebar

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.feathericons.PlusCircle
import compose.icons.feathericons.Settings
import ru.despectra.proxiya.app.ui.vo.ProxyItemVo

sealed interface SidebarItem {
    val id: String get() = javaClass.simpleName
    val title: String
    val subtitle: String?
    val icon: Icon?

    data class ProxyItem(val vo: ProxyItemVo): SidebarItem {
        override val id: String = "ProxyItem#${vo.id}"

        override val title = vo.name
        override val subtitle: String = "${vo.type} on ${vo.port}"
        override val icon: Icon = ProxyIcon(vo)
    }

    data object CreateNewProxyItem : SidebarItem {
        override val title = "Create proxy"
        override val subtitle = null
        override val icon = ImageVectorIcon(FeatherIcons.PlusCircle)
    }

    data object SettingsItem : SidebarItem {
        override val title = "Settings"
        override val subtitle = null
        override val icon = ImageVectorIcon(FeatherIcons.Settings)
    }

    data object SpringSpacerItem : SidebarItem {
        override val title = ""
        override val subtitle = null
        override val icon = null
    }

    sealed interface Icon
    data class ImageVectorIcon(val icon: ImageVector) : Icon
    data class ProxyIcon(val vo: ProxyItemVo) : Icon
}