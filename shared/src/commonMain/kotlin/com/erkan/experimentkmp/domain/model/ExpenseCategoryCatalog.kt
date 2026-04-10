package com.erkan.experimentkmp.domain.model

object ExpenseCategoryCatalog {
    val income = ExpenseCategoryOption(
        name = "Income",
        accentHex = "#29D4A5",
    )

    val fixedExpenseCategories: List<ExpenseCategoryOption> = listOf(
        ExpenseCategoryOption(name = "Food", accentHex = "#FF8A5B"),
        ExpenseCategoryOption(name = "Shopping", accentHex = "#FFBE4D"),
        ExpenseCategoryOption(name = "Transport", accentHex = "#47D1B0"),
        ExpenseCategoryOption(name = "Bills", accentHex = "#7C83FF"),
        ExpenseCategoryOption(name = "Entertainment", accentHex = "#F36DFF"),
    )

    fun allSelectable(): List<ExpenseCategoryOption> = fixedExpenseCategories

    fun accentHexFor(category: String, isIncome: Boolean): String {
        if (isIncome) return income.accentHex
        return fixedExpenseCategories
            .firstOrNull { it.name == category }
            ?.accentHex
            ?: "#94A3C7"
    }
}
