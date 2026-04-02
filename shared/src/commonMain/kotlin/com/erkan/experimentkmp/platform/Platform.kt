package com.erkan.experimentkmp.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
