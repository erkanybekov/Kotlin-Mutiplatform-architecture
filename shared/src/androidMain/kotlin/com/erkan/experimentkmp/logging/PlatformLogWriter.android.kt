package com.erkan.experimentkmp.logging

import android.util.Log

private const val MaxLogcatChunkSize = 3_500

internal actual fun writePlatformLog(entry: LogEntry) {
    val tag = "ExpKmp/${entry.category.take(16)}"
    val priority = entry.level.toAndroidPriority()
    val payload = buildString {
        append(entry.message)
        entry.details?.takeIf { it.isNotBlank() }?.let { detail ->
            append('\n')
            append(detail)
        }
    }

    payload
        .chunked(MaxLogcatChunkSize)
        .forEachIndexed { index, chunk ->
            val message = if (payload.length > MaxLogcatChunkSize) {
                "[${index + 1}] $chunk"
            } else {
                chunk
            }

            runCatching {
                Log.println(priority, tag, message)
            }.getOrElse {
                println("${entry.level}/$tag: $message")
            }
        }
}

private fun String.toAndroidPriority(): Int = when (uppercase()) {
    "ERROR" -> Log.ERROR
    "WARN" -> Log.WARN
    "INFO" -> Log.INFO
    else -> Log.DEBUG
}
