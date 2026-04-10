package com.erkan.experimentkmp.domain.usecase

import com.erkan.experimentkmp.domain.model.ExpenseCategoryOption
import com.erkan.experimentkmp.domain.repository.ExpensesRepository

class GetExpenseCategoriesUseCase(
    private val repository: ExpensesRepository,
) {
    operator fun invoke(): List<ExpenseCategoryOption> = repository.getAvailableCategories()
}
