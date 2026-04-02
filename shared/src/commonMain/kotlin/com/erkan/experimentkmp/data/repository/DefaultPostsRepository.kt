package com.erkan.experimentkmp.data.repository

import com.erkan.experimentkmp.data.remote.PostsApi
import com.erkan.experimentkmp.domain.model.Post
import com.erkan.experimentkmp.domain.repository.PostsRepository

class DefaultPostsRepository(
    private val postsApi: PostsApi,
) : PostsRepository {
    override suspend fun getPosts(): List<Post> {
        return postsApi.getPosts().map { it.toDomain() }
    }
}
