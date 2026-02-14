package ru.despectra.proxiya.app.ui.kit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowBarToUp
import ru.despectra.proxiya.app.ui.vo.ProxyHeaderVo

@Composable
fun SnapToLatestButton(
    vo: ProxyHeaderVo.SnapToLatestButtonVo,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    modifier: Modifier = Modifier,
) {
    val cornersRadius = when (vo.state) {
        ProxyHeaderVo.SnapToLatestButtonVo.State.Snapped,
        ProxyHeaderVo.SnapToLatestButtonVo.State.LockedSnap -> 12.dp
        ProxyHeaderVo.SnapToLatestButtonVo.State.FreeScrolling -> 20.dp
    }
    val animatedCornersRadius by animateDpAsState(
        targetValue = cornersRadius,
        animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing)
    )

    val backgroundColor = when (vo.state) {
        ProxyHeaderVo.SnapToLatestButtonVo.State.Snapped -> Color.Transparent
        ProxyHeaderVo.SnapToLatestButtonVo.State.LockedSnap -> MaterialTheme.colorScheme.primaryContainer
        ProxyHeaderVo.SnapToLatestButtonVo.State.FreeScrolling -> MaterialTheme.colorScheme.primary
    }
    val strokeColor = when (vo.state) {
        ProxyHeaderVo.SnapToLatestButtonVo.State.Snapped -> MaterialTheme.colorScheme.outlineVariant
        ProxyHeaderVo.SnapToLatestButtonVo.State.LockedSnap,
        ProxyHeaderVo.SnapToLatestButtonVo.State.FreeScrolling -> Color.Transparent
    }
    val animatedBackgroundColor by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing)
    )
    val animatedStrokeColor by animateColorAsState(
        targetValue = strokeColor,
        animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing)
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .graphicsLayer {
                shape = RoundedCornerShape(size = animatedCornersRadius)
                clip = true
            }
            .drawBehind {
                drawRect(color = animatedBackgroundColor)
                drawRoundRect(
                    color = animatedStrokeColor,
                    cornerRadius = CornerRadius(
                        x = animatedCornersRadius.toPx(),
                        y = animatedCornersRadius.toPx(),
                    ),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
            .clickable(
                enabled = true,
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = TablerIcons.ArrowBarToUp,
            contentDescription = "Snap to latest button",
            tint = when (vo.state) {
                ProxyHeaderVo.SnapToLatestButtonVo.State.Snapped,
                ProxyHeaderVo.SnapToLatestButtonVo.State.LockedSnap -> MaterialTheme.colorScheme.onSurfaceVariant
                ProxyHeaderVo.SnapToLatestButtonVo.State.FreeScrolling -> MaterialTheme.colorScheme.onPrimary
            },
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview
@Composable
private fun SnapToLatestButtonPreview() {
    SnapToLatestButton(
        vo = ProxyHeaderVo.SnapToLatestButtonVo(state = ProxyHeaderVo.SnapToLatestButtonVo.State.Snapped),
        onClick = {},
    )
}