package ru.despectra.proxiya.app.ui.component

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
import org.koin.compose.viewmodel.koinViewModel
import ru.despectra.proxiya.app.ui.viewmodel.AppNavigationViewModel
import ru.despectra.proxiya.app.ui.viewmodel.MainContentPage
import ru.despectra.proxiya.app.ui.viewmodel.ProxyItemVo
import ru.despectra.proxiya.app.ui.viewmodel.SidebarViewModel

@Composable
fun Sidebar(appNavViewModel: AppNavigationViewModel, modifier: Modifier = Modifier) {
    val currentSelectedPage = appNavViewModel.currentPage.collectAsState()
    val sidebarViewModel = koinViewModel<SidebarViewModel>()

    val proxies by sidebarViewModel.proxiesFlow.collectAsState()
    val selectedProxyId by sidebarViewModel.selectedProxyId.collectAsState()

    Column(
        modifier = modifier
    ) {
        SidebarItem(
            title = "Create proxy",
            isSelected = currentSelectedPage.value == MainContentPage.CreateProxyPage,
            onClick = {
                appNavViewModel.selectPage(MainContentPage.CreateProxyPage)
                sidebarViewModel.clearSelectedProxy()
            }
        )

        proxies.forEach { proxy ->
            SidebarProxyItem(
                proxyItemVo = proxy,
                isSelected = selectedProxyId == proxy.id,
                onClick = {
                    sidebarViewModel.selectProxy(proxy.id)
                    appNavViewModel.selectPage(MainContentPage.SelectedProxyPage(proxy.id))
                }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        SidebarItem(
            title = "Settings",
            isSelected = currentSelectedPage.value == MainContentPage.SettingsPage,
            onClick = {
                appNavViewModel.selectPage(MainContentPage.SettingsPage)
                sidebarViewModel.clearSelectedProxy()
            }
        )
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
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                }
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
                    MaterialTheme.colorScheme.onSurface
                },
                text = proxyItemVo.name,
                style = MaterialTheme.typography.bodyLarge
            )
            
            // Вторая строка - тип прокси и порт вторичным текстом
            Text(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                }
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}