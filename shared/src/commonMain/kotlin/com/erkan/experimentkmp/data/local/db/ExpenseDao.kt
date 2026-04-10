package com.erkan.experimentkmp.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(entry: ExpenseEntity)

    @Query("DELETE FROM expense_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM expense_entries ORDER BY createdAtEpochMillis DESC")
    suspend fun getAll(): List<ExpenseEntity>

    @Query("SELECT * FROM expense_entries ORDER BY createdAtEpochMillis DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<ExpenseEntity>
}
