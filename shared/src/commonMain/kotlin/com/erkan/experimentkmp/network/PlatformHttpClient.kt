package com.erkan.experimentkmp.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun createPlatformHttpClient(): HttpClient

internal fun HttpClientConfig<*>.configureSharedHttpClient(engineName: String) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            },
        )
    }

    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                NetworkLogger.log("[$engineName] $message")
            }
        }
        level = LogLevel.BODY
    }

    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, request ->
            NetworkLogger.log(
                buildString {
                    append("[$engineName] HTTP failure for ")
                    append(request.method.value)
                    append(" ")
                    append(request.url)
                    append(": ")
                    append(cause.message ?: cause::class.simpleName ?: "unknown error")
                },
            )
        }
    }
}
