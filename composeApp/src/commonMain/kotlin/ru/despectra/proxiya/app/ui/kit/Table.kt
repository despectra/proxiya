package ru.despectra.proxiya.app.ui.kit

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.despectra.proxiya.app.ui.kit.WaterfallTable.Data

private val ColumnMinWidth = 36.dp
private val ResizeHandleHitAreaWidth = 16.dp
private val ResizeHandleIndicatorWidth = 4.dp
private val ResizeHandleVerticalPadding = 4.dp

private data class ResizableColumn(
    val id: Data.ColumnId,
    val data: Data.Column,
)

@Composable
fun WaterfallTable(
    data: Data,
    snapping: State<SnapToLatestBehavior.Snapping>,
    onSnapStateChanged: (snapped: Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val columns = data.columnsOrdered.mapNotNull { columnId ->
        data.columnsData[columnId]?.let { columnData ->
            ResizableColumn(columnId, columnData)
        }
    }
    val initialColumnWeights = columns.map { column ->
        column.id to column.data.weight
    }
    val columnWidths = remember(initialColumnWeights) {
        TableColumnResizeState(initialColumnWeights)
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        val dividerColor = MaterialTheme.colorScheme.outlineVariant
        ResizableTableHeader(
            columns = columns,
            columnWidths = columnWidths,
            dividerColor = dividerColor,
        )

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            val lazyListState = rememberLazyListState()
            SnapToLatestBehavior(
                lazyListState = lazyListState,
                snapping = snapping,
                onSnapStateChanged = onSnapStateChanged,
            )
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
                        for ((index, column) in columns.withIndex()) {
                            val cellData = row.cells[column.id] ?: continue
                            val isFirst = index == 0
                            val isLast = index == columns.lastIndex
                            val cellAppearance = cellData.cellAppearance
                            Cell(
                                text = cellData.text,
                                textColor = cellAppearance.textColorProvider(),
                                backgroundColor = cellAppearance.backgroundColorProvider(),
                                modifier = Modifier
                                    .weight(columnWidths.weightOf(column.id))
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
private fun ResizableTableHeader(
    columns: List<ResizableColumn>,
    columnWidths: TableColumnResizeState<Data.ColumnId>,
    dividerColor: Color,
) {
    var headerWidthPx by remember { mutableStateOf(0f) }
    val minColumnWidthPx = with(LocalDensity.current) { ColumnMinWidth.toPx() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .onSizeChanged { size -> headerWidthPx = size.width.toFloat() }
            .bottomDivider(dividerColor)
    ) {
        for ((index, column) in columns.withIndex()) {
            Box(
                modifier = Modifier
                    .weight(columnWidths.weightOf(column.id))
                    .fillMaxHeight()
            ) {
                ColumnHeader(
                    text = column.data.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .cellBasePadding(
                            isFirst = index == 0,
                            isLast = index == columns.lastIndex,
                        )
                )

                columns.getOrNull(index + 1)?.let { nextColumn ->
                    ColumnResizeHandle(
                        dividerColor = dividerColor,
                        onDragDelta = { deltaPx ->
                            columnWidths.resize(
                                firstColumnId = column.id,
                                secondColumnId = nextColumn.id,
                                deltaPx = deltaPx,
                                containerWidthPx = headerWidthPx,
                                minColumnWidthPx = minColumnWidthPx,
                            )
                        },
                        modifier = Modifier.align(Alignment.CenterEnd),
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnResizeHandle(
    dividerColor: Color,
    onDragDelta: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val dragState = rememberDraggableState(onDelta = onDragDelta)

    Box(
        modifier = modifier
            .width(ResizeHandleHitAreaWidth)
            .fillMaxHeight()
            .hoverable(interactionSource)
            .pointerHoverIcon(ResizePointerIcon)
            .draggable(
                state = dragState,
                orientation = Orientation.Horizontal,
                interactionSource = interactionSource,
                startDragImmediately = true,
            )
            .drawBehind {
                if (isHovered || isDragged) {
                    val indicatorWidthPx = ResizeHandleIndicatorWidth.toPx()
                    val verticalPaddingPx = ResizeHandleVerticalPadding.toPx()
                    drawRoundRect(
                        color = dividerColor,
                        topLeft = Offset(
                            x = size.width - indicatorWidthPx / 2,
                            y = verticalPaddingPx,
                        ),
                        size = Size(
                            width = indicatorWidthPx,
                            height = size.height - verticalPaddingPx * 2,
                        ),
                        cornerRadius = CornerRadius(
                            x = indicatorWidthPx / 2,
                            y = indicatorWidthPx / 2,
                        ),
                        alpha = 0.6f,
                    )
                }
            }
    )
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
        snapping = mutableStateOf(SnapToLatestBehavior.Snapping.FreeScroll),
    )
}
