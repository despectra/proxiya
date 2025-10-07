package ru.despectra.proxiya.app.component.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.despectra.proxiya.app.component.pane.proxy.create.CreateProxyForm
import ru.despectra.proxiya.app.component.sidebar.Sidebar
import ru.despectra.proxiya.app.ui.theme.ProxiyaTheme

@Composable
fun ProxiyaApp(component: AppComponent) {

    val appTheme by component.appTheme.collectAsState()
    val sidebarComponent = component.sidebarComponent
    val contentPane by component.contentPane.subscribeAsState()

    ProxiyaTheme(themeMode = appTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                .safeContentPadding()
        ) {
            Sidebar(
                component = sidebarComponent,
                modifier = Modifier
                    .width(240.dp)
                    .fillMaxHeight()
                    .align(Alignment.TopStart)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopStart)
                    .padding(start = (240 - 16).dp)
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                contentPane.child?.instance?.let {
                    when (it) {
                        is AppComponent.PaneChild.CreateProxyChild -> CreateProxyForm(it.component)
                    }
                }
            }
        }
    }
}