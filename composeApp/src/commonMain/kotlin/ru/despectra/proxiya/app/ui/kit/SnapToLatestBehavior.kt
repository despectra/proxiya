package ru.despectra.proxiya.app.ui.kit

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import ru.despectra.proxiya.app.ui.kit.SnapToLatestBehavior.Snapping

@Composable
fun SnapToLatestBehavior(
    lazyListState: LazyListState,
    snapping: State<Snapping>,
    onSnapStateChanged: (snapped: Boolean) -> Unit,
) {
    val firstVisibleItemIndex by derivedStateOf { lazyListState.firstVisibleItemIndex }
    val firstIsNotVisible by derivedStateOf { firstVisibleItemIndex > 0 }
    val isScrollInProgress = lazyListState.isScrollInProgress
    LaunchedEffect(lazyListState, isScrollInProgress) {
        snapshotFlow { firstIsNotVisible to snapping.value }
            .distinctUntilChanged()
            .collect { (firstNotVisible, snapping) ->
                if (firstNotVisible && !isScrollInProgress && snapping == Snapping.AlwaysToIndex(0)) {
                    lazyListState.scrollToItem(0)
                }

                if (firstNotVisible && isScrollInProgress && snapping == Snapping.AlwaysToIndex(0)) {
                    onSnapStateChanged(false)
                }

                if (!firstNotVisible && !isScrollInProgress && snapping == Snapping.FreeScroll) {
                    onSnapStateChanged(true)
                }
            }
    }
}

object SnapToLatestBehavior {
    sealed interface Snapping {
        data object FreeScroll : Snapping
        data class AlwaysToIndex(val index: Int) : Snapping
    }
}
