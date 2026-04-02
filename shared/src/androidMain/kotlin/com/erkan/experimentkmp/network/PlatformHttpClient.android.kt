package com.erkan.experimentkmp.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun createPlatformHttpClient(): HttpClient = HttpClient(OkHttp) {
    configureSharedHttpClient(engineName = "okhttp")
}

actual object NetworkLogger {
    actual fun log(message: String) {
        Log.d("SharedNetwork", message)
    }
}
