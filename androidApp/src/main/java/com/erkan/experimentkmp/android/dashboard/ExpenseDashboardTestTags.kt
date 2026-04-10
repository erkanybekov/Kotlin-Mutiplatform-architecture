package com.erkan.experimentkmp.android.dashboard

import com.erkan.experimentkmp.domain.model.ExpensePeriod

object ExpenseDashboardTestTags {
    const val Screen = "expense_dashboard_screen"
    const val ErrorCard = "expense_dashboard_error_card"
    const val QuickEntrySection = "expense_dashboard_quick_entry_section"
    const val TitleInput = "expense_dashboard_title_input"
    const val AmountInput = "expense_dashboard_amount_input"
    const val NoteInput = "expense_dashboard_note_input"
    const val CategoryInput = "expense_dashboard_category_input"
    const val ExpenseTypeChip = "expense_dashboard_type_expense"
    const val IncomeTypeChip = "expense_dashboard_type_income"
    const val SaveButton = "expense_dashboard_save_button"
    const val AnalyticsSection = "expense_dashboard_analytics_section"
    const val CategoriesSection = "expense_dashboard_categories_section"
    const val TransactionsSection = "expense_dashboard_transactions_section"
    const val EmptyTransactionsCard = "expense_dashboard_transactions_empty"

    fun periodChip(period: ExpensePeriod): String =
        "expense_dashboard_period_${period.name.lowercase()}"

    fun transactionRow(id: Long): String = "expense_dashboard_transaction_$id"

    fun transactionDeleteButton(id: Long): String = "expense_dashboard_transaction_delete_$id"
}
