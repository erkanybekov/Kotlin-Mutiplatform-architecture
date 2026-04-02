package com.erkan.experimentkmp.data.remote

import com.erkan.experimentkmp.data.remote.model.PostDto

interface PostsApi {
    suspend fun getPosts(): List<PostDto>
}
