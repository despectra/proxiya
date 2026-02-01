package ru.despectra.proxiya.app.ui.kit

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.despectra.proxiya.app.ui.kit.WaterfallTable.Data

@Composable
fun WaterfallTable(
    data: Data,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
        ) {
            for ((index, columnId) in data.columnsOrdered.withIndex()) {
                val columnData = data.columnsData[columnId] ?: continue
                val dividerColor = MaterialTheme.colorScheme.outlineVariant
                ColumnHeader(
                    text = columnData.name,
                    modifier = Modifier
                        .weight(data.columnWeight(columnId))
                        .bottomDivider(dividerColor)
                        .cellBasePadding(index)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(data.rows, key = { it.index }) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(color = row.rowAppearance.background)
                ) {
                    for ((index, columnId) in data.columnsOrdered.withIndex()) {
                        val cellData = row.cells[columnId] ?: continue
                        Cell(
                            text = cellData.text,
                            backgroundColor = cellData.cellAppearance.overlayColor,
                            modifier = Modifier
                                .weight(data.columnWeight(columnId))
                                .cellBasePadding(index)
                        )
                    }
                }
            }
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

private fun Modifier.cellBasePadding(cellIndex: Int): Modifier {
    return then(
        if (cellIndex == 0) {
            Modifier.padding(start = 16.dp)
        } else {
            Modifier
        }
    )
}

@Composable
private fun Cell(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(backgroundColor)
            .padding(end = 4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
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
            val background: Color,
        )

        data class CellAppearance(
            val overlayColor: Color = Color.Transparent,
        )

        fun columnWeight(columnId: ColumnId): Float {
            return columnsData[columnId]?.weight ?: return 0.0f
        }
    }
}

@Preview
@Composable
fun PreviewWaterfallTable() {
    val tableData = Data(
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
        rows = List(20) { index ->
            Data.Row(
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
                rowAppearance = Data.RowAppearance(
                    background = if (index % 2 == 0) {
                        Color.Transparent
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    },
                )
            )
        },
    )

    WaterfallTable(data = tableData)
}