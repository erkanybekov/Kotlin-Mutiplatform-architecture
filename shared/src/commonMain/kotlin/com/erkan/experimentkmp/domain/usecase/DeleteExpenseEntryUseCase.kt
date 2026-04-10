package com.erkan.experimentkmp.domain.usecase

import com.erkan.experimentkmp.domain.repository.ExpensesRepository

class DeleteExpenseEntryUseCase(
    private val repository: ExpensesRepository,
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteEntry(id)
    }
}
