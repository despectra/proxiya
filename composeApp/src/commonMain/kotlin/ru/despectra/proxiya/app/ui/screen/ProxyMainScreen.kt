package ru.despectra.proxiya.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Play
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.despectra.proxiya.app.data.settings.ThemeMode
import ru.despectra.proxiya.app.ui.theme.ProxiyaTheme
import ru.despectra.proxiya.app.ui.theme.SpecialColors

@Composable
fun ProxyMainScreen(modifier: Modifier = Modifier, proxyId: String) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Header()
        Content()
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = "Proxy name",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "HTTP on port 8080",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                ProxyStatusBadge(
                    proxyStatus = true,
                    proxyProcessId = "18024",
                    //proxyProcessId = null,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            shape = RoundedCornerShape(12.dp),
            onClick = {},
        ) {
            Icon(
                imageVector = FeatherIcons.Play,
                contentDescription = "Start proxy",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(
                text = "Start",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ProxyStatusBadge(
    modifier: Modifier = Modifier,
    proxyStatus: Boolean, // TODO real status
    proxyProcessId: String? = null,
) {
    Box(
        modifier = modifier
            .height(24.dp)
            .run {
                if (proxyStatus) {
                    clip(RoundedCornerShape(12.dp))
                } else {
                    border(
                        width = 1.dp,
                        color = SpecialColors.proxyStatusInactive,
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }
            .background(
                color = if (proxyStatus) {
                    SpecialColors.proxyStatusRunning
                } else {
                    Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = if (proxyStatus && proxyProcessId != null) "PID: $proxyProcessId" else "Inactive",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (proxyStatus) 1f else 0.7f),
        )
    }
}

@Composable
private fun Content(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceDim)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

    }
}

@Preview
@Composable
fun ProxyMainScreenPreview() {
    ProxiyaTheme(themeMode = ThemeMode.DARK) {
        ProxyMainScreen(proxyId = "1")
    }
}