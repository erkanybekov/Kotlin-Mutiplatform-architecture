package com.erkan.experimentkmp.domain.usecase

import com.erkan.experimentkmp.domain.model.ExpenseDashboard
import com.erkan.experimentkmp.domain.model.ExpensePeriod
import com.erkan.experimentkmp.domain.repository.ExpensesRepository

class GetExpenseDashboardUseCase(
    private val repository: ExpensesRepository,
) {
    suspend operator fun invoke(period: ExpensePeriod): ExpenseDashboard =
        repository.getDashboard(period)
}
