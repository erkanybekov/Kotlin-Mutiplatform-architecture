package com.erkan.experimentkmp.data.remote.model

import com.erkan.experimentkmp.domain.model.Post
import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val id: Long,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val thumbnail: String,
) {
    fun toDomain(): Post = Post(
        id = id,
        title = title,
        body = description,
        price = price,
        category = category,
        imageUrl = thumbnail,
    )
}
