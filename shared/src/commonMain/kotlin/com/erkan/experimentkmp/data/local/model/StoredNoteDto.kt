package com.erkan.experimentkmp.data.local.model

import com.erkan.experimentkmp.domain.model.Note
import kotlinx.serialization.Serializable

@Serializable
data class StoredNoteDto(
    val id: Long,
    val title: String,
    val body: String,
    val isDone: Boolean,
) {
    fun toDomain(): Note = Note(
        id = id,
        title = title,
        body = body,
        isDone = isDone,
    )

    companion object {
        fun fromDomain(note: Note): StoredNoteDto = StoredNoteDto(
            id = note.id,
            title = note.title,
            body = note.body,
            isDone = note.isDone,
        )
    }
}
