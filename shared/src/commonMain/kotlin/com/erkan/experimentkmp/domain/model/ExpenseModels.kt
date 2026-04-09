package com.erkan.experimentkmp.domain.model

enum class ExpensePeriod(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year"),
}

enum class ExpenseEntryType {
    EXPENSE,
    INCOME,
}

data class ExpenseCategoryOption(
    val name: String,
    val accentHex: String,
)

data class AccountSummary(
    val balance: Double,
    val income: Double,
    val expense: Double,
)

data class ExpenseChartEntry(
    val label: String,
    val amount: Double,
)

data class ExpenseCategory(
    val name: String,
    val spent: Double,
    val share: Double,
    val accentHex: String,
)

data class ExpenseTransaction(
    val id: Long,
    val title: String,
    val subtitle: String,
    val dateLabel: String,
    val category: String,
    val amount: Double,
    val accentHex: String,
    val isIncome: Boolean = false,
)

data class ExpenseDashboard(
    val period: ExpensePeriod,
    val summary: AccountSummary,
    val chartEntries: List<ExpenseChartEntry>,
    val categories: List<ExpenseCategory>,
)

data class NewExpenseEntry(
    val title: String,
    val amount: Double,
    val category: String,
    val note: String,
    val type: ExpenseEntryType,
    val createdAtEpochMillis: Long,
)
