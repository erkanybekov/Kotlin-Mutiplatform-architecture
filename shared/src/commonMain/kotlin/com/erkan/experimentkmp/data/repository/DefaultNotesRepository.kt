package com.erkan.experimentkmp.data.repository

import com.erkan.experimentkmp.data.local.NotesLocalDataSource
import com.erkan.experimentkmp.domain.model.Note
import com.erkan.experimentkmp.domain.repository.NotesRepository
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class DefaultNotesRepository(
    private val notesLocalDataSource: NotesLocalDataSource,
) : NotesRepository {
    override suspend fun getNotes(): List<Note> {
        return notesLocalDataSource.readNotes().sortedByDescending { it.id }
    }

    override suspend fun addNote(title: String, body: String): List<Note> {
        val newNote = Note(
            id = nextId(),
            title = title.trim(),
            body = body.trim(),
            isDone = false,
        )
        val updated = listOf(newNote) + notesLocalDataSource.readNotes()
        notesLocalDataSource.writeNotes(updated)
        return updated.sortedByDescending { it.id }
    }

    override suspend fun toggleNote(id: Long): List<Note> {
        val updated = notesLocalDataSource.readNotes().map { note ->
            if (note.id == id) note.copy(isDone = !note.isDone) else note
        }
        notesLocalDataSource.writeNotes(updated)
        return updated.sortedByDescending { it.id }
    }

    @OptIn(ExperimentalTime::class)
    private fun nextId(): Long {
        return Clock.System.now().toEpochMilliseconds() + Random.nextLong(0, 999)
    }
}
