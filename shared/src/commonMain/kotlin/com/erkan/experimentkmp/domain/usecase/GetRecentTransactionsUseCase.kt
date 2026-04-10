package com.erkan.experimentkmp.domain.usecase

import com.erkan.experimentkmp.domain.model.ExpenseTransaction
import com.erkan.experimentkmp.domain.repository.ExpensesRepository

class GetRecentTransactionsUseCase(
    private val repository: ExpensesRepository,
) {
    suspend operator fun invoke(limit: Int = 8): List<ExpenseTransaction> =
        repository.getRecentTransactions(limit)
}
