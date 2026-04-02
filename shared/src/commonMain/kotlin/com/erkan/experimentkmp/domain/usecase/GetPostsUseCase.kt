package com.erkan.experimentkmp.domain.usecase

import com.erkan.experimentkmp.domain.model.Post
import com.erkan.experimentkmp.domain.repository.PostsRepository

class GetPostsUseCase(
    private val postsRepository: PostsRepository,
) {
    suspend operator fun invoke(): List<Post> = postsRepository.getPosts()
}
