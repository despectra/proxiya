package ru.despectra.proxiya.app.ui.kit.proxy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
    Column(
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .heightIn(max = 56.dp)
                .padding(start = 8.dp)
        ) {
            ProxyInfoBlock(proxyHeaderVo, onProxySwitchClick)
            Spacer(modifier = Modifier.weight(1f))

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

@Composable
private fun ProxySwitchButton(
    proxyHeaderVo: ProxyHeaderVo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = FeatherIcons.Power,
            contentDescription = "Proxy on/off button",
            tint = proxyHeaderVo.proxyStatus.color()
        )
    }
}

@Composable
private fun ProxyInfoBlock(
    proxyHeaderVo: ProxyHeaderVo,
    onProxySwitchClick: () -> Unit,
) {
    ProxySwitchButton(proxyHeaderVo, onProxySwitchClick)
    Column(
        modifier = Modifier.fillMaxHeight().padding(start = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = proxyHeaderVo.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (proxyHeaderVo.description != null) {
            Text(
                text = proxyHeaderVo.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
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