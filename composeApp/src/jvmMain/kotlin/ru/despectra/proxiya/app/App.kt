package ru.despectra.proxiya.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import ru.despectra.proxiya.app.ui.component.Sidebar
import ru.despectra.proxiya.app.viewmodel.AppNavigationViewModel
import ru.despectra.proxiya.app.viewmodel.MainContentPage

@Composable
@Preview
fun App() {
    val appNavigationViewModel = koinViewModel<AppNavigationViewModel>()
    
    MaterialTheme {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
        ) {
            Sidebar(
                appNavViewModel = appNavigationViewModel,
                modifier = Modifier
                    .widthIn(min = 180.dp, max = 240.dp)
                    .fillMaxHeight()
            )

            Divider(
                modifier = Modifier.fillMaxHeight().width(1.dp),
            )

            MainContentArea(
                appNavigationViewModel = appNavigationViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun MainContentArea(
    appNavigationViewModel: AppNavigationViewModel,
    modifier: Modifier = Modifier
) {
    val currentPage = appNavigationViewModel.currentPage.collectAsState()
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (val page = currentPage.value) {
            is MainContentPage.CreateProxyPage -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Создание нового прокси сервера",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Здесь будет форма для создания прокси",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            is MainContentPage.SelectedProxyPage -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Прокси сервер: ${page.proxyId}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Здесь будет информация о выбранном прокси",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}