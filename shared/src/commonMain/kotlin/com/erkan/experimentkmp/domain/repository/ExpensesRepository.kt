package com.erkan.experimentkmp.domain.repository

import com.erkan.experimentkmp.domain.model.ExpenseDashboard
import com.erkan.experimentkmp.domain.model.ExpenseCategoryOption
import com.erkan.experimentkmp.domain.model.ExpensePeriod
import com.erkan.experimentkmp.domain.model.ExpenseTransaction
import com.erkan.experimentkmp.domain.model.NewExpenseEntry

interface ExpensesRepository {
    suspend fun getDashboard(period: ExpensePeriod): ExpenseDashboard

    suspend fun getRecentTransactions(limit: Int = 8): List<ExpenseTransaction>

    suspend fun addEntry(entry: NewExpenseEntry)

    fun getAvailableCategories(): List<ExpenseCategoryOption>
}
