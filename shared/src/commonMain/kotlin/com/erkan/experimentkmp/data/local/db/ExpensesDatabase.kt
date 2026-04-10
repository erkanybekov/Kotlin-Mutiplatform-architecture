package com.erkan.experimentkmp.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [ExpenseEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(ExpensesDatabaseConstructor::class)
abstract class ExpensesDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}

@Suppress("KotlinNoActualForExpect")
expect object ExpensesDatabaseConstructor : RoomDatabaseConstructor<ExpensesDatabase> {
    override fun initialize(): ExpensesDatabase
}
