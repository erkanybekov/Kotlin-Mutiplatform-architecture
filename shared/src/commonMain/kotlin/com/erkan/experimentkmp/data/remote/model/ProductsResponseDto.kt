package com.erkan.experimentkmp.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductsResponseDto(
    val products: List<PostDto>,
)
