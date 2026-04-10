package com.erkan.experimentkmp.network

import com.erkan.experimentkmp.logging.AppLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createPlatformHttpClient(appLogger: AppLogger): HttpClient = HttpClient(Darwin) {
    configureSharedHttpClient(appLogger = appLogger)
}
