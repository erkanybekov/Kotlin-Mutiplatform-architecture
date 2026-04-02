package com.erkan.experimentkmp.presentation.posts

data class PostsUiState(
    val isLoading: Boolean = false,
    val posts: List<PostItemUi> = emptyList(),
    val errorMessage: String? = null,
)
