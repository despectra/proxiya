package ru.despectra.proxiya.app.component.sidebar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Server

@Composable
fun Sidebar(component: SidebarComponent, modifier: Modifier = Modifier) {

    val sidebarItems by component.sidebarItems.collectAsState(initial = emptyList())
    val selectedItem by component.selectedSidebarItem.collectAsState(SidebarItem.CreateNewProxyItem)

    Column(
        modifier = modifier.padding(top = 4.dp),
    ) {
        sidebarItems.forEach { item ->
            if (item is SidebarItem.SpringSpacerItem) {
                Spacer(modifier = Modifier.weight(1f))
            } else {
                val isSelected = selectedItem.id == item.id
                UnifiedSidebarItem(
                    modifier = Modifier.padding(top = 4.dp),
                    sidebarItem = item,
                    isSelected = isSelected,
                    onClick = {
                        component.onSelectItem(item)
                    }
                )
            }
        }
    }
}

@Composable
private fun UnifiedSidebarItem(
    modifier: Modifier = Modifier,
    sidebarItem: SidebarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .heightIn(max = 64.dp)
            .clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.Transparent
            },
            headlineColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onBackground
            },
            supportingColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            },
            leadingIconColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onBackground
            }
        ),
        headlineContent = {
            Text(
                text = sidebarItem.title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            sidebarItem.subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        leadingContent = {
            sidebarItem.icon?.let {
                when (it) {
                    is SidebarItem.ImageVectorIcon -> Icon(
                        modifier = Modifier,
                        imageVector = it.icon,
                        contentDescription = "icon", // TODO
                    )
                    is SidebarItem.ProxyIcon -> Icon(
                        modifier = Modifier,
                        imageVector = FeatherIcons.Server, // TODO
                        contentDescription = "proxy" // TODO
                    )
                }
            }
        },
    )
}