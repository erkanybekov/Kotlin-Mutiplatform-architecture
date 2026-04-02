package com.erkan.experimentkmp.domain.usecase

import com.erkan.experimentkmp.domain.model.Note
import com.erkan.experimentkmp.domain.repository.NotesRepository

class GetNotesUseCase(
    private val notesRepository: NotesRepository,
) {
    suspend operator fun invoke(): List<Note> = notesRepository.getNotes()
}
