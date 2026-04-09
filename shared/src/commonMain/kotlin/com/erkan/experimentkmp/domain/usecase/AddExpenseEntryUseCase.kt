package com.erkan.experimentkmp.domain.usecase

import com.erkan.experimentkmp.domain.model.ExpenseCategoryCatalog
import com.erkan.experimentkmp.domain.model.ExpenseEntryType
import com.erkan.experimentkmp.domain.model.NewExpenseEntry
import com.erkan.experimentkmp.domain.repository.ExpensesRepository
import com.erkan.experimentkmp.logging.currentEpochMillis

class AddExpenseEntryUseCase(
    private val repository: ExpensesRepository,
) {
    suspend operator fun invoke(
        title: String,
        amountText: String,
        category: String,
        note: String,
        isIncome: Boolean,
    ) {
        val normalizedTitle = title.trim()
        require(normalizedTitle.isNotEmpty()) { "Title is required." }

        val normalizedAmount = amountText
            .trim()
            .replace(',', '.')
            .toDoubleOrNull()
            ?.takeIf { it > 0.0 }
            ?: throw IllegalArgumentException("Enter a valid amount.")

        val type = if (isIncome) ExpenseEntryType.INCOME else ExpenseEntryType.EXPENSE
        val resolvedCategory = when (type) {
            ExpenseEntryType.INCOME -> ExpenseCategoryCatalog.income.name
            ExpenseEntryType.EXPENSE -> category.trim()
        }

        require(resolvedCategory in repository.getAvailableCategories().map { it.name }) {
            "Choose a category."
        }

        repository.addEntry(
            NewExpenseEntry(
                title = normalizedTitle,
                amount = normalizedAmount,
                category = resolvedCategory,
                note = note.trim(),
                type = type,
                createdAtEpochMillis = currentEpochMillis(),
            ),
        )
    }
}
