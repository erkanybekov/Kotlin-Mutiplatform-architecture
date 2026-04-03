package com.erkan.experimentkmp.network

import com.erkan.experimentkmp.logging.AppLogger
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun createPlatformHttpClient(appLogger: AppLogger): HttpClient

internal fun HttpClientConfig<*>.configureSharedHttpClient(
    engineName: String,
    appLogger: AppLogger,
) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            },
        )
    }

    install(Logging) {
        level = LogLevel.INFO
        sanitizeHeader { header -> header == HttpHeaders.Authorization }
        logger = object : Logger {
            override fun log(message: String) {
                message
                    .lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .forEach { line ->
                        appLogger.append(
                            level = line.toLogLevel(),
                            category = "http",
                            message = line,
                            details = "engine=$engineName",
                        )
                    }
            }
        }
    }

    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, request ->
            appLogger.append(
                level = "ERROR",
                category = "http",
                message = "HTTP failure: ${request.method.value} ${request.url}",
                details = cause.message ?: cause::class.simpleName ?: "unknown error",
            )
        }
    }
}

private fun String.toLogLevel(): String = when {
    startsWith("RESPONSE:") -> "INFO"
    startsWith("REQUEST:") -> "DEBUG"
    startsWith("BODY") -> "DEBUG"
    startsWith("METHOD:") -> "DEBUG"
    startsWith("HEADERS") -> "DEBUG"
    startsWith("COMMON HEADERS") -> "DEBUG"
    startsWith("FROM:") -> "DEBUG"
    else -> "TRACE"
}
