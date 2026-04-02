package com.erkan.experimentkmp.domain.usecase

import com.erkan.experimentkmp.domain.model.Note
import com.erkan.experimentkmp.domain.repository.NotesRepository

class ToggleNoteCompletionUseCase(
    private val notesRepository: NotesRepository,
) {
    suspend operator fun invoke(id: Long): List<Note> = notesRepository.toggleNote(id)
}
