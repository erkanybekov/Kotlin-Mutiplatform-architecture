package com.erkan.experimentkmp.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun expenseDatabaseModule(storageDirectoryPath: String): Module = module {
    single<ExpensesDatabase> {
        createExpensesDatabase(
            builder = getExpenseDatabaseBuilder(get()),
        )
    }
    single<ExpenseDao> { get<ExpensesDatabase>().expenseDao() }
}

private fun getExpenseDatabaseBuilder(
    context: Context,
): RoomDatabase.Builder<ExpensesDatabase> {
    val appContext = context.applicationContext
    val databaseFile = appContext.getDatabasePath("expenses.db")
    return Room.databaseBuilder<ExpensesDatabase>(
        context = appContext,
        name = databaseFile.absolutePath,
    )
}
