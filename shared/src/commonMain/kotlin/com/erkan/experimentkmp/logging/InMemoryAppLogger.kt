package com.erkan.experimentkmp.logging

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAppLogger(
    private val maxEntries: Int = 200,
) : AppLogger {
    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    override val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

    private var nextId = 1L

    override fun append(
        level: String,
        category: String,
        message: String,
        details: String?,
    ) {
        val newEntry = LogEntry(
            id = nextId++,
            timestampEpochMillis = currentEpochMillis(),
            level = level,
            category = category,
            message = message,
            details = details,
        )

        writePlatformLog(newEntry)

        _entries.value = buildList {
            add(newEntry)
            addAll(_entries.value.take(maxEntries - 1))
        }
    }

    override fun clear() {
        _entries.value = emptyList()
    }
}
