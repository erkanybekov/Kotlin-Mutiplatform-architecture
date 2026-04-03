package com.erkan.experimentkmp.logging

import kotlinx.coroutines.flow.StateFlow

interface AppLogger {
    val entries: StateFlow<List<LogEntry>>

    fun append(
        level: String,
        category: String,
        message: String,
        details: String? = null,
    )

    fun clear()
}
