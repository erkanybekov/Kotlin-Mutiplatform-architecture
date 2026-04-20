package com.erkan.experimentkmp.di

import com.erkan.experimentkmp.data.auth.AuthSessionStore
import com.erkan.experimentkmp.data.auth.IosAuthSessionStore
import org.koin.dsl.module

internal actual fun platformSessionModule() = module {
    single<AuthSessionStore> { IosAuthSessionStore() }
}
