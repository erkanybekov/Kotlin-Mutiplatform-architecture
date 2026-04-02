package com.erkan.experimentkmp.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import platform.Foundation.NSLog

actual fun createPlatformHttpClient(): HttpClient = HttpClient(Darwin) {
    configureSharedHttpClient(engineName = "darwin")
}

actual object NetworkLogger {
    actual fun log(message: String) {
        NSLog("%@", message)
    }
}
