package ru.despectra.proxiya.app.ui.kit

import androidx.compose.runtime.toMutableStateMap
import kotlin.math.min

internal class TableColumnResizeState<ColumnId : Any>(
    initialWeights: List<Pair<ColumnId, Float>>,
) {
    private val weights = initialWeights
        .onEach { (columnId, weight) ->
            require(weight.isFinite() && weight > 0f) {
                "Column $columnId must have a finite positive weight, but was $weight"
            }
        }
        .toMutableStateMap()

    fun weightOf(columnId: ColumnId): Float {
        return requireNotNull(weights[columnId]) {
            "Missing width for column $columnId"
        }
    }

    fun resize(
        firstColumnId: ColumnId,
        secondColumnId: ColumnId,
        deltaPx: Float,
        containerWidthPx: Float,
        minColumnWidthPx: Float,
    ) {
        if (containerWidthPx <= 0f || !deltaPx.isFinite()) {
            return
        }

        val resizedWeights = resizeAdjacentColumnWeights(
            firstWeight = weightOf(firstColumnId),
            secondWeight = weightOf(secondColumnId),
            deltaPx = deltaPx,
            containerWidthPx = containerWidthPx,
            totalWeight = weights.values.sum(),
            minColumnWidthPx = minColumnWidthPx,
        )
        weights[firstColumnId] = resizedWeights.first
        weights[secondColumnId] = resizedWeights.second
    }
}

internal data class AdjacentColumnWeights(
    val first: Float,
    val second: Float,
)

internal fun resizeAdjacentColumnWeights(
    firstWeight: Float,
    secondWeight: Float,
    deltaPx: Float,
    containerWidthPx: Float,
    totalWeight: Float,
    minColumnWidthPx: Float,
): AdjacentColumnWeights {
    require(firstWeight.isFinite() && firstWeight > 0f)
    require(secondWeight.isFinite() && secondWeight > 0f)
    require(deltaPx.isFinite())
    require(containerWidthPx.isFinite() && containerWidthPx > 0f)
    require(totalWeight.isFinite() && totalWeight > 0f)
    require(minColumnWidthPx.isFinite() && minColumnWidthPx >= 0f)

    val pairWeight = firstWeight + secondWeight
    val requestedMinWeight = minColumnWidthPx / containerWidthPx * totalWeight
    val effectiveMinWeight = min(requestedMinWeight, pairWeight / 2f)
    val maxFirstWeight = pairWeight - effectiveMinWeight
    val deltaWeight = deltaPx / containerWidthPx * totalWeight
    val resizedFirstWeight = if (effectiveMinWeight >= maxFirstWeight) {
        pairWeight / 2f
    } else {
        (firstWeight + deltaWeight).coerceIn(effectiveMinWeight, maxFirstWeight)
    }

    return AdjacentColumnWeights(
        first = resizedFirstWeight,
        second = pairWeight - resizedFirstWeight,
    )
}
