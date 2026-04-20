package com.erkan.experimentkmp.di

import com.erkan.experimentkmp.data.auth.AndroidAuthSessionStore
import com.erkan.experimentkmp.data.auth.AuthSessionStore
import org.koin.dsl.module

internal actual fun platformSessionModule() = module {
    single<AuthSessionStore> { AndroidAuthSessionStore(get()) }
}
