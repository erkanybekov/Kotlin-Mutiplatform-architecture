package com.erkan.experimentkmp.presentation.logs

import com.erkan.experimentkmp.logging.LogEntry

data class LogsUiState(
    val entries: List<LogEntry> = emptyList(),
)
