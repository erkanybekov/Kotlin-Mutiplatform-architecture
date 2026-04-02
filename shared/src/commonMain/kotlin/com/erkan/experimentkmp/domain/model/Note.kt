package com.erkan.experimentkmp.domain.model

data class Note(
    val id: Long,
    val title: String,
    val body: String,
    val isDone: Boolean,
)
