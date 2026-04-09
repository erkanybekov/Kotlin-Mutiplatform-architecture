package com.erkan.experimentkmp.data.local.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module

fun createExpensesDatabase(
    builder: RoomDatabase.Builder<ExpensesDatabase>,
): ExpensesDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.Default)
    .build()

expect fun expenseDatabaseModule(storageDirectoryPath: String): Module
