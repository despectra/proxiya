package ru.despectra.proxiya.app.component.pane.proxy.main

import ru.despectra.proxiya.app.ui.vo.InterceptedRequestVo
import ru.despectra.proxiya.app.ui.vo.RequestMeasuredParams
import ru.despectra.proxiya.app.ui.vo.RequestStatusVo
import ru.despectra.proxiya.proto.data.http.HttpRequest
import ru.despectra.proxiya.proto.data.http.HttpUrl
import ru.despectra.proxiya.proto.data.http.contentOrNull
import ru.despectra.proxiya.proto.data.http.urlOrNull
import ru.despectra.proxiya.proto.data.interception.InterceptedRequestNotification
import ru.despectra.proxiya.proto.data.interception.InterceptedResponseNotification
import ru.despectra.proxiya.proto.data.interception.httpRequestOrNull
import ru.despectra.proxiya.proto.data.interception.timestampOrNull
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.logging.Level
import java.util.logging.Logger

class InterceptedRequestsFormatter {

    fun format(
        request: InterceptedRequestNotification,
        associatedResponse: InterceptedResponseNotification? = null,
    ): InterceptedRequestVo? {
        val requestId = if (request.hasRequestId()) request.requestId else let {
            logger.log(Level.WARNING, "request has no requestId")
            return null
        }
        val payload = request.httpRequestOrNull ?: let {
            logger.log(Level.WARNING, "request $requestId has no payload")
            return null
        }

        val dateTime = request.timestampOrNull?.let {
            LocalDateTime.ofEpochSecond(
                it.seconds,
                it.nanos,
                ZoneOffset.UTC,
            )
        } ?: LocalDateTime.now(ZoneOffset.UTC)

        val url = payload.urlOrNull
        val host = formatUrl(url)
        val status = formatStatus(associatedResponse)
        val measuredParams = formatMeasuredParams(associatedResponse, dateTime, payload)

        return InterceptedRequestVo(
            id = requestId,
            timestamp = dateTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
            date = dateTime.format(dateFormatter),
            time = dateTime.format(timeFormatter),
            httpMethod = payload.method.orEmpty(),
            host = host,
            urlPath = url?.path.orEmpty(),
            status = status,
            measuredParams = measuredParams,
        )
    }

    private fun formatUrl(url: HttpUrl?): String = url?.let {
        val scheme = it.scheme.takeIf { it.isNotBlank() }
        val host = it.host.takeIf { it.isNotBlank() }
        val port = if (it.hasPort()) it.port else null
        val hostAndPort = listOfNotNull(host, "$port").joinToString(":")
        listOfNotNull(scheme, hostAndPort).joinToString("://")
    }.orEmpty()

    private fun formatStatus(associatedResponse: InterceptedResponseNotification?): RequestStatusVo = when {
        associatedResponse == null -> RequestStatusVo.Ongoing
        associatedResponse.httpResponse != null -> {
            val responsePayload = associatedResponse.httpResponse
            val statusCode = responsePayload.statusCode
            val kind = when (statusCode) {
                in 100..399 -> RequestStatusVo.StatusKind.Success
                in 400..499 -> RequestStatusVo.StatusKind.ClientError
                else -> RequestStatusVo.StatusKind.ServerError
            }
            RequestStatusVo.Completed(
                httpStatusCode = associatedResponse.httpResponse.statusCode.toString(),
                kind = kind
            )
        }

        else -> {
            RequestStatusVo.Failed(errorMessage = "INT.ERR") // TODO
        }
    }

    private fun formatMeasuredParams(
        associatedResponse: InterceptedResponseNotification?,
        dateTime: LocalDateTime?,
        payload: HttpRequest
    ): RequestMeasuredParams = RequestMeasuredParams(
        timing = associatedResponse?.timestampOrNull?.let {
            val responseDateTime = LocalDateTime.ofEpochSecond(
                it.seconds,
                it.nanos,
                ZoneOffset.UTC
            )
            val timingMs = ChronoUnit.MILLIS.between(dateTime, responseDateTime)
            if (timingMs >= 1000) {
                val seconds = timingMs / 1000f
                decimalFormat.format(seconds) + " s"
            } else {
                "$timingMs ms"
            }
        }.orEmpty(),
        requestSize = payload.contentOrNull?.contentLength?.let {
            decimalFormat.format(it.toFloat() / 1024f) + " kB"
        }.orEmpty(),
        responseSize = associatedResponse?.httpResponse?.contentOrNull?.contentLength?.let {
            decimalFormat.format(it.toFloat() / 1024f) + " kB"
        }.orEmpty()
    )

    companion object {
        private val logger = Logger.getLogger(InterceptedRequestsFormatter::class.java.name)

        private val dateFormatter = DateTimeFormatterBuilder()
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('.')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('.')
            .appendValue(ChronoField.YEAR, 4)
            .toFormatter()

        private val timeFormatter = DateTimeFormatterBuilder()
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .toFormatter()

        private val decimalFormat = DecimalFormat("#.##")
    }

}