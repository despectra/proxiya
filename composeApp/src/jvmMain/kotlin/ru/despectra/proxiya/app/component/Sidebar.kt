package ru.despectra.proxiya.app.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.despectra.proxiya.app.viewmodel.AppNavigationViewModel
import ru.despectra.proxiya.app.viewmodel.MainContentPage

@Composable
fun Sidebar(appNavViewModel: AppNavigationViewModel, modifier: Modifier = Modifier) {
    val currentSelectedPage = appNavViewModel.currentPage.collectAsState()

    Column(
        modifier = modifier
    ) {
        SidebarItem(
            title = "Create proxy",
            isSelected = currentSelectedPage.value == MainContentPage.CreateProxyPage,
            onClick = { appNavViewModel.selectPage(MainContentPage.CreateProxyPage) }
        )

        // TODO fetch proxies from controller gRPC interface
        SidebarProxyItem(
            proxyName = "Apphost Proxy",
            proxyType = "SOCKS5",
            port = 8890,
            isSelected = currentSelectedPage.value == MainContentPage.SelectedProxyPage("SOCKS5 :8890"),
            onClick = { appNavViewModel.selectPage(MainContentPage.SelectedProxyPage("SOCKS5 :8890")) }
        )
        SidebarProxyItem(
            proxyName = "iOS Device Proxy",
            proxyType = "HTTPS",
            port = 8090,
            isSelected = currentSelectedPage.value == MainContentPage.SelectedProxyPage("HTTPS :8090"),
            onClick = { appNavViewModel.selectPage(MainContentPage.SelectedProxyPage("HTTPS :8090")) }
        )
    }
}

@Composable
private fun SidebarProxyItem(
    modifier: Modifier = Modifier,
    proxyName: String,
    proxyType: String,
    port: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp) // Увеличиваем высоту для двух строк
            .background(
                color = if (isSelected) {
                    MaterialTheme.colors.primarySurface
                } else {
                    MaterialTheme.colors.surface
                }
            )
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Первая строка - имя прокси основным текстом
            Text(
                color = if (isSelected) {
                    MaterialTheme.colors.onPrimary
                } else {
                    MaterialTheme.colors.onSurface
                },
                text = proxyName,
                style = MaterialTheme.typography.body1
            )
            
            // Вторая строка - тип прокси и порт вторичным текстом
            Text(
                color = if (isSelected) {
                    MaterialTheme.colors.onPrimary.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                },
                text = "$proxyType :$port",
                style = MaterialTheme.typography.caption
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
            .background(
                color = if (isSelected) {
                    MaterialTheme.colors.primarySurface
                } else {
                    MaterialTheme.colors.surface
                }
            )
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = if (isSelected) {
                MaterialTheme.colors.onPrimary
            } else {
                MaterialTheme.colors.onSurface
            },
            text = title,
            style = MaterialTheme.typography.body1,
        )
    }
}