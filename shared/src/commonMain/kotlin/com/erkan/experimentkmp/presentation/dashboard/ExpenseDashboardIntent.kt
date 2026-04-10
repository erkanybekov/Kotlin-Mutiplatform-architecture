package com.erkan.experimentkmp.presentation.dashboard

import com.erkan.experimentkmp.domain.model.ExpensePeriod

sealed class ExpenseDashboardIntent {
    data object Load : ExpenseDashboardIntent()

    data object Refresh : ExpenseDashboardIntent()

    data object SubmitEntry : ExpenseDashboardIntent()

    data object DismissError : ExpenseDashboardIntent()

    data class SelectPeriod(val period: ExpensePeriod) : ExpenseDashboardIntent()

    data class UpdateTitle(val value: String) : ExpenseDashboardIntent()

    data class UpdateAmount(val value: String) : ExpenseDashboardIntent()

    data class UpdateNote(val value: String) : ExpenseDashboardIntent()

    data class UpdateEntryType(val isIncome: Boolean) : ExpenseDashboardIntent()

    data class SelectCategory(val category: String) : ExpenseDashboardIntent()

    data class DeleteEntry(val id: Long) : ExpenseDashboardIntent()
}
