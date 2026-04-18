package com.erkan.experimentkmp.platform

import platform.Foundation.NSUUID

actual fun randomUuidString(): String = NSUUID().UUIDString()
