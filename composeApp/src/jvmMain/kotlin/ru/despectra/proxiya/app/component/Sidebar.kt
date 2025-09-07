package ru.despectra.proxiya.app.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
        SidebarItem(
            title = "SOCKS5 :8890",
            isSelected = currentSelectedPage.value == MainContentPage.SelectedProxyPage("SOCKS5 :8890"),
            onClick = { appNavViewModel.selectPage(MainContentPage.SelectedProxyPage("SOCKS5 :8890")) }
        )
        SidebarItem(
            title = "HTTPS :8090",
            isSelected = currentSelectedPage.value == MainContentPage.SelectedProxyPage("HTTPS :8090"),
            onClick = { appNavViewModel.selectPage(MainContentPage.SelectedProxyPage("HTTPS :8090")) }
        )
    }
}

@Composable
private fun SidebarItem(
    modifier: Modifier = Modifier,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                color = if (isSelected) {
                    MaterialTheme.colors.primarySurface
                } else {
                    MaterialTheme.colors.surface
                }
            ),
        onClick = onClick
    ) {
        Text(
            color = if (isSelected) {
                MaterialTheme.colors.onPrimary
            } else {
                MaterialTheme.colors.onSurface
            },
            text = title,
            textAlign = TextAlign.Start,
        )
    }
}