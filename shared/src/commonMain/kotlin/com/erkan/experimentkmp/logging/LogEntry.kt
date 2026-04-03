package com.erkan.experimentkmp.logging

data class LogEntry(
    val id: Long,
    val timestampEpochMillis: Long,
    val level: String,
    val category: String,
    val message: String,
    val details: String? = null,
)
