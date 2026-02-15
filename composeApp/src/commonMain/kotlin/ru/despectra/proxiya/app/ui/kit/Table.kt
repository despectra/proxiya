package ru.despectra.proxiya.app.ui.kit

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import ru.despectra.proxiya.app.ui.kit.WaterfallTable.Data
import kotlin.math.abs

private const val HEADER_RESIZE_ACTIVATION_THRESHOLD = 8
private const val COLUMN_MIN_WIDTH = 36

@Composable
fun WaterfallTable(
    data: Data,
    snapping: State<WaterfallTable.Snapping>,
    onSnapStateChanged: (snapped: Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isRowHovered by interactionSource.collectIsHoveredAsState()

    var hoveredColumnDragHandlePosition by remember {
        mutableStateOf<Int?>(null)
    }
    var isResizingColumns by remember { mutableStateOf(false) }

    LaunchedEffect(isRowHovered, isResizingColumns) {
        if (!isRowHovered && !isResizingColumns) {
            hoveredColumnDragHandlePosition = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (hoveredColumnDragHandlePosition != null) {
                    Modifier.pointerHoverIcon(ResizePointerIcon)
                } else {
                    Modifier
                }
            )
    ) {
        var currentlyResizingColumns = remember {
            listOf<Data.ColumnId>()
        }
        val columnsWeights = data.columnsOrdered.map { columnId ->
            val weight = data.columnsData[columnId]?.weight ?: 0.0f
            columnId to weight
        }

        val columnsWeightState = remember(columnsWeights) {
            columnsWeights.toMutableStateMap()
        }
        val dividerColor = MaterialTheme.colorScheme.outlineVariant
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .hoverable(interactionSource)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press && currentlyResizingColumns.size == 2) {
                                println("COLUMN RESIZING ON")
                                isResizingColumns = true
                            }
                            if (event.type == PointerEventType.Release && isResizingColumns) {
                                println("COLUMN RESIZING OFF")
                                isResizingColumns = false
                            }
                            if (event.type != PointerEventType.Move) {
                                continue
                            }
                            val pointerPosition = event.changes.first().position
                            val normalizedPointerPosition: Float = pointerPosition.x / size.width

                            if (isResizingColumns) {
                                val (firstResizableColumn, secondResizableColumn) = currentlyResizingColumns
                                val firstWeight = columnsWeightState[firstResizableColumn]!!
                                val secondWeight = columnsWeightState[secondResizableColumn]!!
                                val minWeight = COLUMN_MIN_WIDTH.dp.toPx() / size.width
                                val maxWeight = firstWeight + secondWeight

                                var accumulated = 0f
                                var updatedFirstWeight = firstWeight
                                var updatedSecondWeight = secondWeight
                                for (columnId in data.columnsOrdered) {
                                    val weight = columnsWeightState[columnId] ?: continue
                                    when (columnId) {
                                        firstResizableColumn -> {
                                            updatedFirstWeight = (normalizedPointerPosition - accumulated).coerceIn(
                                                minWeight,
                                                maxWeight - minWeight
                                            )
                                            updatedSecondWeight = maxWeight - updatedFirstWeight
                                            hoveredColumnDragHandlePosition =
                                                ((accumulated + updatedFirstWeight) * size.width).toInt()
                                            break
                                        }

                                        else -> {
                                            accumulated += weight
                                        }
                                    }
                                }

                                columnsWeightState[firstResizableColumn] = updatedFirstWeight
                                columnsWeightState[secondResizableColumn] = updatedSecondWeight
                                continue
                            }

                            var accumulatedWeight = 0f
                            var columnsToResize = listOf<Data.ColumnId>()
                            var dragHandlePosition: Int? = null

                            for ((index, columnId) in data.columnsOrdered.withIndex()) {
                                val columnWeight = columnsWeightState[columnId] ?: continue
                                if (accumulatedWeight + columnWeight < normalizedPointerPosition) {
                                    accumulatedWeight += columnWeight
                                    continue
                                }

                                println("Found hovered column: $columnId")
                                val hoveredColumnStart = accumulatedWeight * size.width
                                val hoveredColumnEnd = (accumulatedWeight + columnWeight) * size.width

                                val nextColumnId by lazy {
                                    data.columnsOrdered.getOrNull(index + 1)
                                }
                                val prevColumnId by lazy {
                                    data.columnsOrdered.getOrNull(index - 1)
                                }
                                val threshold = HEADER_RESIZE_ACTIVATION_THRESHOLD.dp.toPx()
                                fun pointerIsAround(columnBorderPosition: Float): Boolean {
                                    return abs(pointerPosition.x - columnBorderPosition) <= threshold
                                }

                                val isFirst = index == 0
                                val isLast = index == data.columnsOrdered.lastIndex

                                when {
                                    pointerIsAround(hoveredColumnEnd) && (isFirst || !isLast) -> {
                                        columnsToResize = listOfNotNull(columnId, nextColumnId)
                                        dragHandlePosition = hoveredColumnEnd.toInt()
                                    }

                                    pointerIsAround(hoveredColumnStart) && (!isFirst || isLast) -> {
                                        columnsToResize = listOfNotNull(prevColumnId, columnId)
                                        dragHandlePosition = hoveredColumnStart.toInt()
                                    }
                                }

                                println("Columns being resizing: ${columnsToResize.joinToString(", ")}")
                                println("Drag handle position: $hoveredColumnDragHandlePosition")
                                break
                            }
                            currentlyResizingColumns = columnsToResize.takeIf { it.size == 2 }.orEmpty()
                            hoveredColumnDragHandlePosition = dragHandlePosition
                        }
                    }
                }
                .then(
                    hoveredColumnDragHandlePosition?.let { handlePosition ->
                        Modifier.drawWithContent {
                            drawContent()
                            translate(
                                left = handlePosition - 2.dp.toPx(),
                                top = 4.dp.toPx()
                            ) {
                                drawRoundRect(
                                    color = dividerColor,
                                    topLeft = Offset(0f, 0f),
                                    size = Size(
                                        width = 4.dp.toPx(),
                                        height = size.height - 8.dp.toPx()
                                    ),
                                    cornerRadius = CornerRadius(x = 2.dp.toPx(), y = 2.dp.toPx()),
                                    alpha = 0.6f,
                                )
                            }
                        }
                    } ?: Modifier
                )
        ) {
            for ((index, columnId) in data.columnsOrdered.withIndex()) {
                val columnData = data.columnsData[columnId] ?: continue
                val isFirst = index == 0
                val isLast = index == data.columnsOrdered.lastIndex
                val columnWeight by derivedStateOf { columnsWeightState[columnId] ?: 0f }
                ColumnHeader(
                    text = columnData.name,
                    modifier = Modifier
                        .weight(columnWeight)
                        .bottomDivider(dividerColor)
                        .cellBasePadding(isFirst, isLast)
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            val lazyListState = rememberLazyListState()
            val firstVisibleItemIndex by derivedStateOf { lazyListState.firstVisibleItemIndex }
            val firstIsNotVisible by derivedStateOf { firstVisibleItemIndex > 0 }
            val isScrollInProgress = lazyListState.isScrollInProgress
            LaunchedEffect(lazyListState, isScrollInProgress) {
                snapshotFlow { firstIsNotVisible to snapping.value }
                    .distinctUntilChanged()
                    .collect { (firstNotVisible, snapping) ->
                        if (firstNotVisible && !isScrollInProgress && snapping == WaterfallTable.Snapping.AlwaysToIndex(0)) {
                            lazyListState.scrollToItem(0)
                        }

                        if (firstNotVisible && isScrollInProgress && snapping == WaterfallTable.Snapping.AlwaysToIndex(0)) {
                            onSnapStateChanged(false)
                        }

                        if (!firstNotVisible && !isScrollInProgress && snapping == WaterfallTable.Snapping.FreeScroll) {
                            onSnapStateChanged(true)
                        }
                    }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
            ) {
                items(data.rows, key = { it.id }) { row ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isHovered by interactionSource.collectIsHoveredAsState()

                    val hoverColorOverlay = MaterialTheme.colorScheme.surfaceVariant
                    val hoverColorBase = MaterialTheme.colorScheme.secondaryContainer
                    val defaultBackgroundColor = row.rowAppearance.backgroundColorProvider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .hoverable(interactionSource)
                            .drawBehind {
                                if (isHovered) {
                                    drawRect(
                                        hoverColorOverlay.copy(alpha = 0.7f)
                                            .compositeOver(hoverColorBase)
                                            .copy(alpha = 0.6f),
                                    )
                                } else {
                                    drawRect(defaultBackgroundColor)
                                }
                            }
                    ) {
                        for ((index, columnId) in data.columnsOrdered.withIndex()) {
                            val cellData = row.cells[columnId] ?: continue
                            val isFirst = index == 0
                            val isLast = index == data.columnsOrdered.lastIndex
                            val cellWeight by derivedStateOf { columnsWeightState[columnId] ?: 0f }
                            val cellAppearance = cellData.cellAppearance
                            Cell(
                                text = cellData.text,
                                textColor = cellAppearance.textColorProvider(),
                                backgroundColor = cellAppearance.backgroundColorProvider(),
                                modifier = Modifier
                                    .weight(cellWeight)
                                    .cellBasePadding(isFirst, isLast)
                            )
                        }
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                style = defaultScrollbarStyle().copy(
                    hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    unhoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                ),
                adapter = rememberScrollbarAdapter(scrollState = lazyListState),
            )
        }
    }
}

@Composable
private fun ColumnHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = text,
        )
    }
}

private fun Modifier.bottomDivider(dividerColor: Color): Modifier {
    return drawBehind {
        drawLine(
            color = dividerColor,
            start = Offset(
                x = 0f,
                y = size.height,
            ),
            end = Offset(
                x = size.width,
                y = size.height
            ),
            strokeWidth = 1.dp.toPx(),
        )
    }
}

private fun Modifier.cellBasePadding(isFirst: Boolean, isLast: Boolean): Modifier {
    return padding(
        start = if (isFirst) 16.dp else 4.dp,
        end = if (isLast) 16.dp else 4.dp,
    )
}

@Composable
private fun Cell(
    text: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = Color.Transparent,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(backgroundColor),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = text,
        )
    }
}

object WaterfallTable {
    data class Data(
        val columnsOrdered: List<ColumnId>,
        val columnsData: Map<ColumnId, Column>,
        val rows: List<Row>,
    ) {
        data class Column(
            val name: String,
            val weight: Float,
        )

        data class Row(
            val id: String,
            val index: Int,
            val cells: Map<ColumnId, Cell>,
            val rowAppearance: RowAppearance,
        )

        data class Cell(
            val text: String,
            val cellAppearance: CellAppearance,
        )

        enum class ColumnId {
            Datetime,
            HttpMethod,
            Host,
            UrlPath,
            Status,
            Size,
            Timing
        }

        data class RowAppearance(
            val backgroundColorProvider: @Composable () -> Color,
        )

        data class CellAppearance(
            val backgroundColorProvider: @Composable () -> Color = { Color.Transparent },
            val textColorProvider: @Composable () -> Color = { MaterialTheme.colorScheme.onSurface },
        )

        fun columnWeight(columnId: ColumnId): Float {
            return columnsData[columnId]?.weight ?: return 0.0f
        }
    }

    sealed interface Snapping {
        data object FreeScroll : Snapping
        data class AlwaysToIndex(val index: Int) : Snapping
    }

    internal val previewData: Data
        @Composable get() = Data(
            columnsOrdered = listOf(
                Data.ColumnId.Datetime,
                Data.ColumnId.HttpMethod,
                Data.ColumnId.Host,
                Data.ColumnId.UrlPath,
                Data.ColumnId.Status,
                Data.ColumnId.Size,
                Data.ColumnId.Timing,
            ),
            columnsData = mapOf(
                Data.ColumnId.Datetime to Data.Column(name = "Time", weight = 0.05f),
                Data.ColumnId.HttpMethod to Data.Column(name = "Method", weight = 0.1f),
                Data.ColumnId.Host to Data.Column(name = "Host", weight = 0.1f),
                Data.ColumnId.UrlPath to Data.Column(name = "URL path", weight = 0.6f),
                Data.ColumnId.Status to Data.Column(name = "Status", weight = 0.05f),
                Data.ColumnId.Size to Data.Column(name = "Size", weight = 0.05f),
                Data.ColumnId.Timing to Data.Column(name = "Timing", weight = 0.05f)
            ),
            rows = List(100) { index ->
                Data.Row(
                    id = "$index",
                    index = index,
                    cells = mapOf(
                        Data.ColumnId.Datetime to Data.Cell(
                            text = "12:04",
                            cellAppearance = Data.CellAppearance(),
                        ),
                        Data.ColumnId.HttpMethod to Data.Cell(
                            text = if (Math.random() > 0.5) "POST" else "GET",
                            cellAppearance = Data.CellAppearance(),
                        ),
                        Data.ColumnId.Host to Data.Cell(
                            text = "api.webpack.org",
                            cellAppearance = Data.CellAppearance(),
                        ),
                        Data.ColumnId.UrlPath to Data.Cell(
                            text = "/api/v2/packages/private/refresh?package_id=343252dfa900d8c",
                            cellAppearance = Data.CellAppearance(),
                        ),
                        Data.ColumnId.Status to Data.Cell(
                            text = "200",
                            cellAppearance = Data.CellAppearance(),
                        ),
                        Data.ColumnId.Size to Data.Cell(
                            text = "16.4 kB",
                            cellAppearance = Data.CellAppearance(),
                        ),
                        Data.ColumnId.Timing to Data.Cell(
                            text = "230 ms",
                            cellAppearance = Data.CellAppearance(),
                        )
                    ),
                    rowAppearance = Data.RowAppearance {
                        if (index % 2 == 0) {
                            Color.Transparent
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        }
                    }
                )
            },
        )
}

@Preview
@Composable
fun PreviewWaterfallTable() {
    val tableData = WaterfallTable.previewData
    WaterfallTable(
        data = tableData,
        snapping = mutableStateOf(WaterfallTable.Snapping.FreeScroll),
    )
}