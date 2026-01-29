package com.erkan.experimentkmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform