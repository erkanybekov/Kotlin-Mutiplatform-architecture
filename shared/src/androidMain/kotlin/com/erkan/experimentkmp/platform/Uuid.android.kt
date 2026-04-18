package com.erkan.experimentkmp.platform

import java.util.UUID

actual fun randomUuidString(): String = UUID.randomUUID().toString()
