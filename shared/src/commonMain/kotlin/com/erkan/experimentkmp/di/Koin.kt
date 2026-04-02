package com.erkan.experimentkmp.di

import com.erkan.experimentkmp.data.local.NotesLocalDataSource
import com.erkan.experimentkmp.data.remote.JsonPlaceholderPostsApi
import com.erkan.experimentkmp.data.remote.PostsApi
import com.erkan.experimentkmp.data.repository.DefaultNotesRepository
import com.erkan.experimentkmp.data.repository.DefaultPostsRepository
import com.erkan.experimentkmp.domain.repository.NotesRepository
import com.erkan.experimentkmp.domain.repository.PostsRepository
import com.erkan.experimentkmp.domain.usecase.AddNoteUseCase
import com.erkan.experimentkmp.domain.usecase.GetNotesUseCase
import com.erkan.experimentkmp.domain.usecase.GetPostsUseCase
import com.erkan.experimentkmp.domain.usecase.ToggleNoteCompletionUseCase
import com.erkan.experimentkmp.network.createPlatformHttpClient
import com.erkan.experimentkmp.platform.Platform
import com.erkan.experimentkmp.platform.getPlatform
import com.erkan.experimentkmp.presentation.notes.NotesStateHolder
import com.erkan.experimentkmp.presentation.posts.PostsStateHolder
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

fun sharedModule(
    storageDirectoryPath: String,
    platformProvider: () -> Platform = ::getPlatform,
): Module = module {
    single<Platform> { platformProvider() }
    single<HttpClient> { createPlatformHttpClient() }
    single {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = false
        }
    }
    single { NotesLocalDataSource(storageDirectoryPath = storageDirectoryPath, json = get()) }
    single<PostsApi> { JsonPlaceholderPostsApi(get()) }
    single<PostsRepository> { DefaultPostsRepository(get()) }
    single<NotesRepository> { DefaultNotesRepository(get()) }
    single { GetPostsUseCase(get()) }
    single { GetNotesUseCase(get()) }
    single { AddNoteUseCase(get()) }
    single { ToggleNoteCompletionUseCase(get()) }
    single { PostsStateHolder(get()) }
    single { NotesStateHolder(get(), get(), get()) }
}

fun initKoin(
    storageDirectoryPath: String,
    appDeclaration: KoinAppDeclaration = {},
): Koin {
    KoinPlatformTools.defaultContext().getOrNull()?.let { return it }

    return startKoin {
        appDeclaration()
        modules(sharedModule(storageDirectoryPath = storageDirectoryPath))
    }.koin
}

class SharedAppGraph private constructor(
    private val koin: Koin,
) {
    constructor(storageDirectoryPath: String) : this(
        KoinPlatformTools.defaultContext().getOrNull() ?: initKoin(storageDirectoryPath),
    )

    fun postsStateHolder(): PostsStateHolder = koin.get()
    fun notesStateHolder(): NotesStateHolder = koin.get()
}
