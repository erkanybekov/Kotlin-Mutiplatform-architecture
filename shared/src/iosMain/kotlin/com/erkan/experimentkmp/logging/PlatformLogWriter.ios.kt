package com.erkan.experimentkmp.logging

internal actual fun writePlatformLog(entry: LogEntry) {
    val payload = buildString {
        append("[${entry.level}] ")
        append(entry.category)
        append(": ")
        append(entry.message)
        entry.details?.takeIf { it.isNotBlank() }?.let { detail ->
            append('\n')
            append(detail)
        }
    }
    println(payload)
}
