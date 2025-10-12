package ru.despectra.proxiya.app.component.pane.proxy.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.despectra.proxiya.app.component.pane.MaterialPane
import ru.despectra.proxiya.app.data.settings.ThemeMode
import ru.despectra.proxiya.app.ui.kit.proxy.ProxyHeader
import ru.despectra.proxiya.app.ui.kit.proxy.ProxyHeaderPreviews
import ru.despectra.proxiya.app.ui.theme.ProxiyaTheme
import ru.despectra.proxiya.app.ui.vo.ProxyHeaderVo

@Composable
fun ProxyMainPane(component: ProxyMainPaneComponent) {
    val proxyHeaderVo by component.proxyHeader.collectAsState()

    MaterialPane(isolated = false) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            ProxyHeader(
                proxyHeaderVo = proxyHeaderVo,
                onProxySwitchClick = {
                    component.toggleProxy()
                },
                onProxyStreamingClick = {
                    // TODO
                },
                onFilterClick = {
                    // TODO
                },
                onRewriteClick = {
                    // TODO
                }
            )
        }
    }
}

@Preview
@Composable
fun ProxyMainScreenPreview() {
    ProxiyaTheme(themeMode = ThemeMode.DARK) {
        ProxyMainPane(
            component = object : ProxyMainPaneComponent {

                override val proxyHeader: StateFlow<ProxyHeaderVo>
                    get() = MutableStateFlow(ProxyHeaderPreviews.runningProxy)

                override fun toggleProxy() {
                }
            }
        )
    }
}