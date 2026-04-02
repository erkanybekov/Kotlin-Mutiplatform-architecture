package com.erkan.experimentkmp.presentation.posts

import com.erkan.experimentkmp.domain.usecase.GetPostsUseCase
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

class PostsStateHolder(
    private val getPostsUseCase: GetPostsUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _state = MutableStateFlow(PostsUiState())
    val state: StateFlow<PostsUiState> = _state.asStateFlow()

    val currentState: PostsUiState
        get() = state.value

    fun load() {
        if (state.value.posts.isNotEmpty() || state.value.isLoading) return
        refresh()
    }

    fun refresh() {
        scope.launch {
            _state.value = state.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                getPostsUseCase().take(12).map { it.toUi() }
            }.onSuccess { posts ->
                _state.value = PostsUiState(
                    isLoading = false,
                    posts = posts,
                    errorMessage = null,
                )
            }.onFailure { error ->
                _state.value = state.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Could not load posts.",
                )
            }
        }
    }

    fun watch(block: (PostsUiState) -> Unit): ObservationHandle {
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
