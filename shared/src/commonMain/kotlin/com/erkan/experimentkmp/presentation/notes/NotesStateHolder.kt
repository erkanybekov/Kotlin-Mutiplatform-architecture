package com.erkan.experimentkmp.presentation.notes

import com.erkan.experimentkmp.domain.usecase.AddNoteUseCase
import com.erkan.experimentkmp.domain.usecase.GetNotesUseCase
import com.erkan.experimentkmp.domain.usecase.ToggleNoteCompletionUseCase
import com.erkan.experimentkmp.presentation.shared.ObservationHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotesStateHolder(
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val toggleNoteCompletionUseCase: ToggleNoteCompletionUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _state = MutableStateFlow(NotesUiState())
    val state: StateFlow<NotesUiState> = _state.asStateFlow()

    val currentState: NotesUiState
        get() = state.value

    fun load() {
        if (state.value.notes.isNotEmpty() || state.value.isLoading) return
        refresh()
    }

    fun refresh() {
        scope.launch {
            _state.value = state.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                getNotesUseCase().map { it.toUi() }
            }.onSuccess { notes ->
                _state.value = NotesUiState(
                    isLoading = false,
                    notes = notes,
                )
            }.onFailure { error ->
                _state.value = state.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Could not load notes.",
                )
            }
        }
    }

    fun addNote(title: String, body: String) {
        if (title.isBlank() || body.isBlank()) {
            _state.value = state.value.copy(errorMessage = "Both title and body are required.")
            return
        }

        scope.launch {
            _state.value = state.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                addNoteUseCase(title, body).map { it.toUi() }
            }.onSuccess { notes ->
                _state.value = NotesUiState(
                    isLoading = false,
                    notes = notes,
                )
            }.onFailure { error ->
                _state.value = state.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Could not save note.",
                )
            }
        }
    }

    fun toggleNote(id: Long) {
        scope.launch {
            runCatching {
                toggleNoteCompletionUseCase(id).map { it.toUi() }
            }.onSuccess { notes ->
                _state.value = state.value.copy(
                    notes = notes,
                    errorMessage = null,
                )
            }.onFailure { error ->
                _state.value = state.value.copy(
                    errorMessage = error.message ?: "Could not update note.",
                )
            }
        }
    }

    fun watch(block: (NotesUiState) -> Unit): ObservationHandle {
        block(state.value)
        val job: Job = scope.launch {
            state.collectLatest(block)
        }
        return object : ObservationHandle {
            override fun dispose() {
                job.cancel()
            }
        }
    }
}
