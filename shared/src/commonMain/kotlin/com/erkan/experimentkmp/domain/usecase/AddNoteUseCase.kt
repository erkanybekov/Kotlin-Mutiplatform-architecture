package com.erkan.experimentkmp.domain.usecase

import com.erkan.experimentkmp.domain.model.Note
import com.erkan.experimentkmp.domain.repository.NotesRepository

class AddNoteUseCase(
    private val notesRepository: NotesRepository,
) {
    suspend operator fun invoke(title: String, body: String): List<Note> {
        return notesRepository.addNote(title = title, body = body)
    }
}
