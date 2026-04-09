package com.erkan.experimentkmp.network

import com.erkan.experimentkmp.logging.AppLogger
import com.erkan.experimentkmp.logging.currentEpochMillis
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.AttributeKey
import kotlinx.serialization.json.Json

expect fun createPlatformHttpClient(appLogger: AppLogger): HttpClient

internal fun HttpClientConfig<*>.configureSharedHttpClient(
    appLogger: AppLogger,
) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            },
        )
    }

    install(InAppHttpLoggingPlugin(appLogger))

    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, request ->
            val durationMillis = request.elapsedSinceRequestStartMillis()
            appLogger.append(
                level = "ERROR",
                category = "http",
                message = "<-- ERROR ${request.method.value} ${request.logPath()}${durationSuffix(durationMillis)}",
                details = cause.message ?: cause::class.simpleName ?: "unknown error",
            )
        }
    }
}

private fun InAppHttpLoggingPlugin(appLogger: AppLogger) = createClientPlugin("InAppHttpLogging") {
    onRequest { request, _ ->
        request.attributes.put(RequestStartedAtKey, currentEpochMillis())
        appLogger.append(
            level = "DEBUG",
            category = "http",
            message = "--> ${request.method.value} ${request.logPath()}",
        )
    }

    onResponse { response ->
        val durationMillis = (response.responseTime.timestamp - response.requestTime.timestamp)
            .coerceAtLeast(0L)

        appLogger.append(
            level = "INFO",
            category = "http",
            message = "<-- ${response.status.value} ${response.request.method.value} ${response.logPath()}${durationSuffix(durationMillis)}",
        )
    }
}

private val RequestStartedAtKey = AttributeKey<Long>("requestStartedAt")

private fun HttpRequest.elapsedSinceRequestStartMillis(): Long? =
    attributes.getOrNull(RequestStartedAtKey)
        ?.let { startedAt -> (currentEpochMillis() - startedAt).coerceAtLeast(0L) }

private fun HttpRequestBuilder.elapsedSinceRequestStartMillis(): Long? =
    attributes.getOrNull(RequestStartedAtKey)
        ?.let { startedAt -> (currentEpochMillis() - startedAt).coerceAtLeast(0L) }

private fun HttpResponse.logPath(): String = request.url.toString().toLogPath()

private fun HttpRequest.logPath(): String = url.toString().toLogPath()

private fun HttpRequestBuilder.logPath(): String = url.buildString().toLogPath()

private fun String.toLogPath(): String {
    val withoutScheme = substringAfter("://", missingDelimiterValue = this)
    val hostAndPath = withoutScheme.substringAfter('/', missingDelimiterValue = "")
    return if (hostAndPath.isBlank()) "/" else "/$hostAndPath"
}

private fun durationSuffix(durationMillis: Long?): String =
    durationMillis?.let { " (${it} ms)" }.orEmpty()
