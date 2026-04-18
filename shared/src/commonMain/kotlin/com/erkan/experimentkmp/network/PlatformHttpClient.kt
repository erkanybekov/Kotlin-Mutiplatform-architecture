package com.erkan.experimentkmp.network

import com.erkan.experimentkmp.logging.AppLogger
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json

expect fun createPlatformHttpClient(appLogger: AppLogger): HttpClient

internal fun HttpClientConfig<*>.configureSharedHttpClient(
    appLogger: AppLogger,
) {
    install(ContentNegotiation) {
        json(AppJson)
    }

    install(WebSockets) {
        pingIntervalMillis = 20_000
    }

    install(Logging) {
        logger = AppLoggerHttpLogger(appLogger)
        level = LogLevel.ALL
        sanitizeHeader { header ->
            header.equals(HttpHeaders.Authorization, ignoreCase = true) ||
                header.equals(HttpHeaders.Cookie, ignoreCase = true) ||
                header.equals(HttpHeaders.SetCookie, ignoreCase = true)
        }
    }

    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, request ->
            appLogger.append(
                level = "ERROR",
                category = "network",
                message = "<-- ERROR ${request.method.value} ${request.logPath()}",
                details = cause.message ?: cause::class.simpleName ?: "unknown error",
            )
        }
    }
}

private class AppLoggerHttpLogger(
    private val appLogger: AppLogger,
) : Logger {
    override fun log(message: String) {
        message.toHttpLogPayload()?.let { payload ->
            appLogger.append(
                level = payload.level,
                category = "network",
                message = payload.message,
                details = payload.details,
            )
        }
    }
}

private data class HttpLogPayload(
    val level: String,
    val message: String,
    val details: String?,
)

private fun HttpRequest.logPath(): String = url.toString().toLogPath()

private fun HttpRequestBuilder.logPath(): String = url.buildString().toLogPath()

private fun String.toHttpLogPayload(): HttpLogPayload? {
    val normalized = trim().takeIf { it.isNotBlank() } ?: return null
    val lines = normalized.lines()
    val firstLine = lines.firstOrNull()?.trim().orEmpty()
    val details = lines.drop(1).joinToString("\n").trim().takeIf { it.isNotBlank() }

    return when {
        firstLine.startsWith("-->") -> {
            val method = firstLine.removePrefix("-->").trim().substringBefore(' ').ifBlank { "HTTP" }
            val path = firstLine.extractFirstUrl()?.toLogPath() ?: "/"
            HttpLogPayload(
                level = "DEBUG",
                message = "--> $method $path",
                details = details,
            )
        }

        firstLine.startsWith("<--") -> {
            val responseText = firstLine.removePrefix("<--").trim()
            val statusCode = responseText.substringBefore(' ').ifBlank { "HTTP" }
            val path = firstLine.extractFirstUrl()?.toLogPath()
                ?: (lines.extractValue("FROM:") ?: "/").toLogPath()
            HttpLogPayload(
                level = statusCode.toHttpLevel(),
                message = "<-- $statusCode $path",
                details = details,
            )
        }

        firstLine.startsWith("REQUEST:") -> {
            val method = lines.extractMethod()
            val path = firstLine.substringAfter("REQUEST:").trim().toLogPath()
            HttpLogPayload(
                level = "DEBUG",
                message = "--> $method $path",
                details = details,
            )
        }

        firstLine.startsWith("RESPONSE:") -> {
            val statusText = firstLine.substringAfter("RESPONSE:").trim()
            val statusCode = statusText.substringBefore(' ').ifBlank { statusText }
            val method = lines.extractMethod()
            val path = (lines.extractValue("FROM:") ?: "/").toLogPath()
            HttpLogPayload(
                level = statusCode.toHttpLevel(),
                message = "<-- $statusCode $method $path",
                details = details,
            )
        }

        else -> HttpLogPayload(
            level = "DEBUG",
            message = normalized.lineSequence().first(),
            details = details,
        )
    }
}

private fun List<String>.extractMethod(): String =
    extractValue("METHOD:")
        ?.let { rawMethod ->
            rawMethod.substringAfter("value=", missingDelimiterValue = rawMethod)
        }
        ?.substringBefore(')')
        ?.takeIf { it.isNotBlank() }
        ?: "HTTP"

private fun List<String>.extractValue(prefix: String): String? =
    firstOrNull { it.trim().startsWith(prefix) }
        ?.substringAfter(prefix)
        ?.trim()

private fun String.toLogPath(): String {
    val withoutScheme = substringAfter("://", missingDelimiterValue = this)
    val pathWithQuery = withoutScheme.substringAfter('/', missingDelimiterValue = "")
    val hostAndPath = pathWithQuery.substringBefore('?')
    return if (hostAndPath.isBlank()) "/" else "/$hostAndPath"
}

private fun String.extractFirstUrl(): String? =
    Regex("""https?://\S+""")
        .find(this)
        ?.value
        ?.substringBefore(')')

private fun String.toHttpLevel(): String = toIntOrNull()?.let { statusCode ->
    when {
        statusCode >= 500 -> "ERROR"
        statusCode >= 400 -> "WARN"
        else -> "INFO"
    }
} ?: "INFO"
