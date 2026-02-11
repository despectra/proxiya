package ru.despectra.proxiya.app.component.pane.proxy.main

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import ru.despectra.proxiya.app.ui.kit.WaterfallTable.Data
import ru.despectra.proxiya.app.ui.theme.ProxiyaCustomColors
import ru.despectra.proxiya.app.ui.vo.ProxyInterceptionWaterfallVo
import ru.despectra.proxiya.app.ui.vo.RequestStatusVo

class WaterfallTableFormatter {
    fun format(interceptionWaterfall: ProxyInterceptionWaterfallVo): Data {
        return Data(
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
                Data.ColumnId.Datetime to Data.Column(name = "Time", weight = 0.05f), // TODO string resources
                Data.ColumnId.HttpMethod to Data.Column(name = "Method", weight = 0.1f),
                Data.ColumnId.Host to Data.Column(name = "Host", weight = 0.1f),
                Data.ColumnId.UrlPath to Data.Column(name = "Path", weight = 0.6f),
                Data.ColumnId.Status to Data.Column(name = "Status", weight = 0.05f),
                Data.ColumnId.Size to Data.Column(name = "Size", weight = 0.05f),
                Data.ColumnId.Timing to Data.Column(name = "Timing", weight = 0.05f)
            ),
            rows = interceptionWaterfall.interceptedRequests.mapIndexed { index, request ->
                Data.Row(
                    id = request.id,
                    index = index,
                    cells = mapOf(
                        Data.ColumnId.Datetime to Data.Cell(
                            text = request.time,
                            cellAppearance = Data.CellAppearance(),
                        ),
                        Data.ColumnId.HttpMethod to Data.Cell(
                            text = request.httpMethod,
                            cellAppearance = Data.CellAppearance(),
                        ),
                        Data.ColumnId.Host to Data.Cell(
                            text = request.host,
                            cellAppearance = Data.CellAppearance(),
                        ),
                        Data.ColumnId.UrlPath to Data.Cell(
                            text = request.urlPath,
                            cellAppearance = Data.CellAppearance(),
                        ),
                        Data.ColumnId.Status to Data.Cell(
                            text = when (val status = request.status) {
                                RequestStatusVo.Ongoing -> "..."
                                is RequestStatusVo.Completed -> status.httpStatusCode
                                is RequestStatusVo.Failed -> status.errorMessage
                            },
                            cellAppearance = Data.CellAppearance(
                                textColorProvider = {
                                    when (val status = request.status) {
                                        RequestStatusVo.Ongoing -> MaterialTheme.colorScheme.onSurface
                                        is RequestStatusVo.Completed -> when (status.kind) {
                                            RequestStatusVo.StatusKind.Success -> ProxiyaCustomColors.success
                                            RequestStatusVo.StatusKind.ClientError -> ProxiyaCustomColors.warning
                                            RequestStatusVo.StatusKind.ServerError -> ProxiyaCustomColors.statusError
                                        }
                                        is RequestStatusVo.Failed -> ProxiyaCustomColors.statusError
                                    }
                                }
                            ),
                        ),
                        Data.ColumnId.Size to Data.Cell(
                            text = buildString {
                                // TODO format final string in VO formatter
                                append(request.measuredParams.requestSize)
                                if (request.measuredParams.responseSize.isNotBlank()) {
                                    append("/")
                                    append(request.measuredParams.responseSize)
                                }
                            },
                            cellAppearance = Data.CellAppearance(),
                        ),
                        Data.ColumnId.Timing to Data.Cell(
                            text = request.measuredParams.timing,
                            cellAppearance = Data.CellAppearance(),
                        ),
                    ),
                    rowAppearance = Data.RowAppearance(
                        backgroundColorProvider = {
                            if (index % 2 == 0) {
                                Color.Transparent
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            }
                        }
                    )
                )
            }.reversed()
        )
    }
}