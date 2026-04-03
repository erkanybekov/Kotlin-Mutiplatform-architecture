package com.erkan.experimentkmp.data.remote

import com.erkan.experimentkmp.data.remote.model.PostDto
import com.erkan.experimentkmp.data.remote.model.ProductsResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class JsonPlaceholderPostsApi(
    private val httpClient: HttpClient,
) : PostsApi {
    override suspend fun getPosts(): List<PostDto> {
        return httpClient
            .get("https://dummyjson.com/products?limit=30")
            .body<ProductsResponseDto>()
            .products
    }
}
