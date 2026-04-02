package com.erkan.experimentkmp.presentation.posts

import com.erkan.experimentkmp.domain.model.Post

data class PostItemUi(
    val id: Long,
    val title: String,
    val body: String,
)

fun Post.toUi(): PostItemUi = PostItemUi(
    id = id,
    title = title,
    body = body,
)
