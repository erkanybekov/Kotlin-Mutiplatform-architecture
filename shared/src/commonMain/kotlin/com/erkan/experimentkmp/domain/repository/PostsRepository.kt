package com.erkan.experimentkmp.domain.repository

import com.erkan.experimentkmp.domain.model.Post

interface PostsRepository {
    suspend fun getPosts(): List<Post>
}
