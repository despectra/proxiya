package ru.despectra.proxiya.app.ui.kit.proxy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.despectra.proxiya.app.data.settings.ThemeMode
import ru.despectra.proxiya.app.ui.theme.ProxiyaTheme
import ru.despectra.proxiya.app.ui.vo.ProxyHeaderVo

object ProxyHeaderPreviews {
    val runningProxy = ProxyHeaderVo(
        name = "Android",
        description = "Main proxy for Android devices",
        proxyParameters = "HTTPS on 8080",
        proxyStatus = ProxyHeaderVo.ProxyStatus.Running,
        proxySwitchButton = ProxyHeaderVo.ButtonVo(
            isEnabled = true,
            text = "Stop",
            icon = ProxyHeaderVo.ButtonIcon.PowerIndicator,
        ),
        processId = "39902",
        streamingButton = ProxyHeaderVo.ButtonVo(
            isEnabled = true,
            text = "Streaming",
            icon = ProxyHeaderVo.ButtonIcon.Pause,
        ),
        filterButton = ProxyHeaderVo.ButtonVo(
            isEnabled = true,
            text = "Filter..",
            icon = ProxyHeaderVo.ButtonIcon.Filter,
        ),
        rewriteButton = ProxyHeaderVo.ButtonVo(
            isEnabled = true,
            text = "Rewrite..",
            icon = ProxyHeaderVo.ButtonIcon.Rewrite,
        )
    )
}

@Preview
@Composable
fun ProxyHeaderPreview() {
    ProxiyaTheme(themeMode = ThemeMode.LIGHT) {
        Box(modifier = Modifier.padding(all = 8.dp)) {
            ProxyHeader(
                proxyHeaderVo = ProxyHeaderPreviews.runningProxy,
                onProxySwitchClick = {},
                onProxyStreamingClick = {},
                onFilterClick = {},
                onRewriteClick = {},
            )
        }
    }
}