package com.erkan.experimentkmp.domain.model

data class Post(
    val id: Long,
    val title: String,
    val body: String,
    val price: Double,
    val category: String,
    val imageUrl: String,
)
