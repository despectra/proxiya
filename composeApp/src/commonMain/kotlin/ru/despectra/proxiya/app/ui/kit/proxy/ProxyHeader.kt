package ru.despectra.proxiya.app.ui.kit.proxy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Filter
import compose.icons.feathericons.MoreVertical
import compose.icons.feathericons.Pause
import compose.icons.feathericons.PenTool
import compose.icons.feathericons.Play
import compose.icons.feathericons.Power
import ru.despectra.proxiya.app.ui.theme.ProxiyaCustomColors
import ru.despectra.proxiya.app.ui.vo.ProxyHeaderVo

@Composable
fun ProxyHeader(
    proxyHeaderVo: ProxyHeaderVo,
    onProxySwitchClick: () -> Unit,
    onProxyStreamingClick: () -> Unit,
    onFilterClick: () -> Unit,
    onRewriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .heightIn(max = 56.dp)
                    .padding(start = 16.dp)
            ) {
                ProxyInfoBlock(proxyHeaderVo)
                Spacer(modifier = Modifier.weight(1f))
                ProxyStatusBlock(proxyHeaderVo, onProxySwitchClick)
                IconButton(
                    onClick = { }
                ) {
                    Icon(
                        imageVector = FeatherIcons.MoreVertical,
                        contentDescription = "More actions on proxy"
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp),
            ) {
                ProxyHeaderButton(proxyHeaderVo.streamingButton, onProxyStreamingClick)
                Spacer(Modifier.width(24.dp))
                ProxyHeaderButton(proxyHeaderVo.filterButton, onFilterClick)
                Spacer(Modifier.width(8.dp))
                ProxyHeaderButton(proxyHeaderVo.rewriteButton, onRewriteClick)
            }
        }
    }
}

@Composable
private fun RowScope.ProxyInfoBlock(
    proxyHeaderVo: ProxyHeaderVo
) {
    StatusCircle(proxyHeaderVo.proxyStatus, Modifier.padding(top = 18.dp, end = 8.dp))
    Text(
        text = proxyHeaderVo.name,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.alignByBaseline().padding(top = 4.dp)
    )
    Spacer(Modifier.width(12.dp))
    Text(
        text = proxyHeaderVo.proxyParameters,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        modifier = Modifier.alignByBaseline()
    )
}

@Composable
private fun RowScope.ProxyStatusBlock(
    proxyHeaderVo: ProxyHeaderVo,
    onProxySwitchClick: () -> Unit,
) {
    val status = proxyHeaderVo.proxyStatus
    if (status == ProxyHeaderVo.ProxyStatus.Running) {
        Text(
            text = proxyHeaderVo.processId,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.alignByBaseline(),
        )
    } else {
        Text(
            text = status.displayName(),
            style = MaterialTheme.typography.titleSmall,
            color = status.color(),
            modifier = Modifier.alignByBaseline(),
        )
    }

    Spacer(Modifier.width(16.dp))
    ProxyHeaderButton(proxyHeaderVo.proxySwitchButton, onProxySwitchClick)
}

@Composable
private fun ProxyHeaderButton(
    buttonVo: ProxyHeaderVo.ButtonVo,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        enabled = buttonVo.isEnabled,
    ) {
        IconForButton(
            image = buttonVo.icon.toImage(),
            contentDescription = "",
            isEnabled = buttonVo.isEnabled,
        )
        Spacer(Modifier.width(8.dp))
        Text(buttonVo.text)
    }
}

@Composable
private fun IconForButton(
    image: ImageVector,
    contentDescription: String,
    isEnabled: Boolean,
) {
    Icon(
        modifier = Modifier.size(20.dp),
        imageVector = image,
        contentDescription = contentDescription,
        tint = if (isEnabled) {
            ButtonDefaults.outlinedButtonColors()
                .contentColor
        } else {
            ButtonDefaults.outlinedButtonColors()
                .disabledContentColor
        }
    )
}

@Composable
private fun StatusCircle(
    proxyStatus: ProxyHeaderVo.ProxyStatus,
    modifier: Modifier = Modifier,
) {
    val color = proxyStatus.color()
    Canvas(modifier = modifier.size(8.dp, 8.dp)) {
        drawCircle(color)
    }
}
private fun ProxyHeaderVo.ProxyStatus.displayName(): String {
    return when (this) {
        ProxyHeaderVo.ProxyStatus.Idle -> "stopped"
        ProxyHeaderVo.ProxyStatus.Starting -> "starting.."
        ProxyHeaderVo.ProxyStatus.Running -> "running"
        ProxyHeaderVo.ProxyStatus.Stopping -> "stopping.."
        ProxyHeaderVo.ProxyStatus.Error -> "error"
    }
}

@Composable
private fun ProxyHeaderVo.ProxyStatus.color(): Color {
    return when (this) {
        ProxyHeaderVo.ProxyStatus.Idle -> ProxiyaCustomColors.statusIdle
        ProxyHeaderVo.ProxyStatus.Starting -> ProxiyaCustomColors.statusStarting
        ProxyHeaderVo.ProxyStatus.Running -> ProxiyaCustomColors.statusActive
        ProxyHeaderVo.ProxyStatus.Stopping -> ProxiyaCustomColors.statusStopping
        ProxyHeaderVo.ProxyStatus.Error -> ProxiyaCustomColors.statusError
    }
}

private fun ProxyHeaderVo.ButtonIcon.toImage(): ImageVector {
    return when (this) {
        ProxyHeaderVo.ButtonIcon.PowerIndicator -> FeatherIcons.Power
        ProxyHeaderVo.ButtonIcon.Play -> FeatherIcons.Play
        ProxyHeaderVo.ButtonIcon.Pause -> FeatherIcons.Pause
        ProxyHeaderVo.ButtonIcon.Filter -> FeatherIcons.Filter
        ProxyHeaderVo.ButtonIcon.Rewrite -> FeatherIcons.PenTool
    }
}