package com.erkan.experimentkmp.domain.repository

import com.erkan.experimentkmp.domain.model.Note

interface NotesRepository {
    suspend fun getNotes(): List<Note>
    suspend fun addNote(title: String, body: String): List<Note>
    suspend fun toggleNote(id: Long): List<Note>
}
