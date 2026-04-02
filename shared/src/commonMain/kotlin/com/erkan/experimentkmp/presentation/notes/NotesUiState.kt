package com.erkan.experimentkmp.presentation.notes

data class NotesUiState(
    val isLoading: Boolean = false,
    val notes: List<NoteItemUi> = emptyList(),
    val errorMessage: String? = null,
)
