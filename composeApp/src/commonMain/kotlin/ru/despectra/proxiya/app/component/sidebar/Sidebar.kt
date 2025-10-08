package ru.despectra.proxiya.app.component.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.despectra.proxiya.app.ui.theme.ProxiyaCustomColors
import ru.despectra.proxiya.app.ui.vo.ProxyItemVo

@Composable
fun Sidebar(component: SidebarComponent, modifier: Modifier = Modifier) {

    val sidebarItems by component.sidebarItems.collectAsState(initial = emptyList())
    val selectedItem by component.selectedSidebarItem.collectAsState(SidebarItem.CreateNewProxyItem)

    Column(
        modifier = modifier
    ) {
        sidebarItems.forEach { item ->
            val isSelected = selectedItem.id == item.id
            val defaultOnClick = {
                component.onSelectItem(item)
            }

            when (item) {
                SidebarItem.CreateNewProxyItem -> SidebarItem(
                    title = "Create proxy",
                    isSelected = isSelected,
                    onClick = defaultOnClick
                )
                is SidebarItem.ProxyItem -> SidebarProxyItem(
                    proxyItemVo = item.vo,
                    isSelected = isSelected,
                    onClick = defaultOnClick
                )
                SidebarItem.SettingsItem -> SidebarItem(
                    title = "Settings",
                    isSelected = isSelected,
                    onClick = defaultOnClick
                )
                SidebarItem.SpringSpacerItem -> Spacer(
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SidebarProxyItem(
    modifier: Modifier = Modifier,
    proxyItemVo: ProxyItemVo,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp) // Увеличиваем высоту для двух строк
            .background(
                color = itemBackground(isSelected)
            )
            .clickable {
                onClick()
            },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(all = 8.dp)
                .size(8.dp)
                .background(
                    color = when (proxyItemVo.status) {
                        ProxyItemVo.Status.IDLE -> Color.Gray
                        ProxyItemVo.Status.TRANSIENT -> Color.Blue
                        ProxyItemVo.Status.ACTIVE -> Color.Green
                        ProxyItemVo.Status.ERROR -> Color.Red
                    },
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Первая строка - имя прокси основным текстом
            Text(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                text = proxyItemVo.name,
                style = MaterialTheme.typography.bodyLarge
            )

            // Вторая строка - тип прокси и порт вторичным текстом
            Text(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                },
                text = "${proxyItemVo.type} :${proxyItemVo.port}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SidebarItem(
    modifier: Modifier = Modifier,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable {
                onClick()
            }
            .background(
                color = itemBackground(isSelected)
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onBackground
            },
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun itemBackground(isSelected: Boolean): Color {
    return if (isSelected) {
        ProxiyaCustomColors.sidebarSelectedBackground
    } else {
        Color.Transparent
    }
}