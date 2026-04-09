package com.erkan.experimentkmp.network

import com.erkan.experimentkmp.logging.AppLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun createPlatformHttpClient(appLogger: AppLogger): HttpClient = HttpClient(OkHttp) {
    configureSharedHttpClient(appLogger = appLogger)
}
