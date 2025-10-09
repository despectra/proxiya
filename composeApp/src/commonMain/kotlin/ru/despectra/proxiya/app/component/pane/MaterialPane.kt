package ru.despectra.proxiya.app.component.pane

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MaterialPane(
    modifier: Modifier = Modifier,
    isolated: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = PANE_MARGIN.dp,
                top = PANE_MARGIN.dp,
                bottom = PANE_MARGIN.dp,
                end = PANE_MARGIN.dp.onlyOnIsolated(isolated)
            )
            .shadow(
                elevation = 1.dp,
                clip = true,
                shape = RoundedCornerShape(
                    topStart = PANE_CORNER_RADUIS.dp,
                    bottomStart = PANE_CORNER_RADUIS.dp,
                    topEnd = PANE_CORNER_RADUIS.dp.onlyOnIsolated(isolated),
                    bottomEnd = PANE_CORNER_RADUIS.dp.onlyOnIsolated(isolated),
                ),
                ambientColor = DefaultShadowColor.copy(alpha = 0.3f),
                spotColor = DefaultShadowColor.copy(alpha = 0.2f)
            )
            .background(color = MaterialTheme.colorScheme.surface),
        content = content,
    )
}

private fun Dp.onlyOnIsolated(isolated: Boolean): Dp {
    return if (isolated) this else 0.dp
}

private const val PANE_CORNER_RADUIS = 12
private const val PANE_MARGIN = 8