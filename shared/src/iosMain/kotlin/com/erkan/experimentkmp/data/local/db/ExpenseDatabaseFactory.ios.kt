package com.erkan.experimentkmp.data.local.db

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.URLByAppendingPathComponent
import platform.Foundation.NSDocumentDirectory

actual fun expenseDatabaseModule(storageDirectoryPath: String): Module = module {
    single<ExpensesDatabase> {
        createExpensesDatabase(
            builder = getExpenseDatabaseBuilder(storageDirectoryPath),
        )
    }
    single<ExpenseDao> { get<ExpensesDatabase>().expenseDao() }
}

private fun getExpenseDatabaseBuilder(
    storageDirectoryPath: String,
): RoomDatabase.Builder<ExpensesDatabase> {
    val databasePath = databasePath(storageDirectoryPath)
    return Room.databaseBuilder<ExpensesDatabase>(
        name = databasePath,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun databasePath(storageDirectoryPath: String): String {
    if (storageDirectoryPath.isNotBlank()) {
        return "$storageDirectoryPath/expenses.db"
    }

    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val databaseUrl = documentDirectory?.URLByAppendingPathComponent("expenses.db")
    return requireNotNull(databaseUrl?.path)
}
