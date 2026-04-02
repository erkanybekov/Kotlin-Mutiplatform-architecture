package com.erkan.experimentkmp.presentation.notes

import com.erkan.experimentkmp.domain.model.Note

data class NoteItemUi(
    val id: Long,
    val title: String,
    val body: String,
    val isDone: Boolean,
)

fun Note.toUi(): NoteItemUi = NoteItemUi(
    id = id,
    title = title,
    body = body,
    isDone = isDone,
)
