package com.erkan.experimentkmp.di

import com.erkan.experimentkmp.data.local.db.expenseDatabaseModule
import com.erkan.experimentkmp.data.repository.RoomExpensesRepository
import com.erkan.experimentkmp.domain.repository.ExpensesRepository
import com.erkan.experimentkmp.domain.usecase.AddExpenseEntryUseCase
import com.erkan.experimentkmp.domain.usecase.GetExpenseDashboardUseCase
import com.erkan.experimentkmp.domain.usecase.GetExpenseCategoriesUseCase
import com.erkan.experimentkmp.domain.usecase.GetRecentTransactionsUseCase
import com.erkan.experimentkmp.logging.AppLogger
import com.erkan.experimentkmp.logging.InMemoryAppLogger
import com.erkan.experimentkmp.network.createPlatformHttpClient
import com.erkan.experimentkmp.platform.Platform
import com.erkan.experimentkmp.platform.getPlatform
import com.erkan.experimentkmp.presentation.dashboard.ExpenseDashboardStateHolder
import com.erkan.experimentkmp.presentation.logs.LogsStateHolder
import io.ktor.client.HttpClient
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

@Suppress("UNUSED_PARAMETER")
fun sharedModules(
    storageDirectoryPath: String,
    platformProvider: () -> Platform = ::getPlatform,
): List<Module> = listOf(
    module {
    single<Platform> { platformProvider() }
    single<AppLogger> { InMemoryAppLogger() }
    single<HttpClient> { createPlatformHttpClient(get()) }
    single<ExpensesRepository> { RoomExpensesRepository(get()) }
    single { GetExpenseDashboardUseCase(get()) }
    single { GetExpenseCategoriesUseCase(get()) }
    single { GetRecentTransactionsUseCase(get()) }
    single { AddExpenseEntryUseCase(get()) }
    single { LogsStateHolder(get()) }
    single { ExpenseDashboardStateHolder(get(), get(), get(), get()) }
    },
    expenseDatabaseModule(storageDirectoryPath),
)

fun initKoin(
    storageDirectoryPath: String,
    appDeclaration: KoinAppDeclaration = {},
): Koin {
    KoinPlatformTools.defaultContext().getOrNull()?.let { return it }

    return startKoin {
        appDeclaration()
        modules(sharedModules(storageDirectoryPath = storageDirectoryPath))
    }.koin
}

class SharedAppGraph private constructor(
    private val koin: Koin,
) {
    constructor(storageDirectoryPath: String) : this(
        KoinPlatformTools.defaultContext().getOrNull() ?: initKoin(storageDirectoryPath),
    )

    fun expenseDashboardStateHolder(): ExpenseDashboardStateHolder = koin.get()
    fun logsStateHolder(): LogsStateHolder = koin.get()
}
