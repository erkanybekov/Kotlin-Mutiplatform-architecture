package com.erkan.experimentkmp.logging

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.time

@OptIn(ExperimentalForeignApi::class)
actual fun currentEpochMillis(): Long = time(null).toLong() * 1000
