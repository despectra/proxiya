package ru.despectra.proxiya.app.ui.kit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import ru.despectra.proxiya.app.ui.vo.SnapToLatestButtonState

@Composable
fun SnapToLatestButton(
    state: State<SnapToLatestButtonState>,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    modifier: Modifier = Modifier,
) {
    val buttonState = state.value
    val cornersRadius = when (buttonState) {
        is SnapToLatestButtonState.Snapped -> 12.dp
        SnapToLatestButtonState.Unsnapped -> 20.dp
    }
    val animatedCornersRadius by animateDpAsState(
        targetValue = cornersRadius,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
    )

    val backgroundColor = when (buttonState) {
        is SnapToLatestButtonState.Snapped -> {
            if (buttonState.locked) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
        }
        SnapToLatestButtonState.Unsnapped -> MaterialTheme.colorScheme.primary
    }
    val strokeColor = when (buttonState) {
        is SnapToLatestButtonState.Snapped -> MaterialTheme.colorScheme.outlineVariant
        SnapToLatestButtonState.Unsnapped -> Color.Transparent
    }
    val animatedBackgroundColor by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
    )
    val animatedStrokeColor by animateColorAsState(
        targetValue = strokeColor,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
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
        val tintColor = when (buttonState) {
            is SnapToLatestButtonState.Snapped -> MaterialTheme.colorScheme.onSurfaceVariant
            SnapToLatestButtonState.Unsnapped -> MaterialTheme.colorScheme.onPrimary
        }
        Icon(
            imageVector = TablerIcons.ArrowBarToUp,
            contentDescription = "Snap to latest button",
            tint = tintColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview
@Composable
private fun SnapToLatestButtonPreview() {
    SnapToLatestButton(
        state = mutableStateOf(SnapToLatestButtonState.Snapped()),
        onClick = {},
    )
}