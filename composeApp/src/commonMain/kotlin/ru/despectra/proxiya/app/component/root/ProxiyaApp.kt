package ru.despectra.proxiya.app.component.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.despectra.proxiya.app.component.sidebar.Sidebar
import ru.despectra.proxiya.app.ui.theme.ProxiyaTheme

@Composable
fun ProxiyaApp(component: AppComponent) {

    val appTheme by component.appTheme.collectAsState()
    val sidebarComponent = component.sidebarComponent

    ProxiyaTheme(themeMode = appTheme) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
        ) {
            Sidebar(
                component = sidebarComponent,
                modifier = Modifier
                    .widthIn(min = 180.dp, max = 240.dp)
                    .fillMaxHeight()
            )
        }
    }
}