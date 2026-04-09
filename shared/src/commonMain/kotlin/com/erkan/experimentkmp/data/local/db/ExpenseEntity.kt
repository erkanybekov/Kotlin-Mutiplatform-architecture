package com.erkan.experimentkmp.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_entries")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String,
    val amount: Double,
    val category: String,
    val type: String,
    val createdAtEpochMillis: Long,
)
