package com.erkan.experimentkmp.presentation.dashboard

import com.erkan.experimentkmp.domain.model.ExpenseCategoryOption
import com.erkan.experimentkmp.domain.model.ExpenseDashboard
import com.erkan.experimentkmp.domain.model.ExpensePeriod
import com.erkan.experimentkmp.domain.model.ExpenseTransaction
import kotlin.math.absoluteValue
import kotlin.math.roundToLong

data class ExpenseDashboardUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val balanceLabel: String = "${'$'}0.00",
    val incomeLabel: String = "${'$'}0.00",
    val expenseLabel: String = "${'$'}0.00",
    val summaryCards: List<SummaryCardUi> = defaultSummaryCards(ExpensePeriod.MONTH),
    val selectedPeriod: ExpensePeriod = ExpensePeriod.MONTH,
    val selectedPeriodLabel: String = ExpensePeriod.MONTH.label,
    val availableCategories: List<CategoryOptionUi> = emptyList(),
    val chartPoints: List<ChartPointUi> = emptyList(),
    val categories: List<CategoryBreakdownUi> = emptyList(),
    val recentTransactions: List<TransactionItemUi> = emptyList(),
    val isEmpty: Boolean = true,
    val errorMessage: String? = null,
)

data class SummaryCardUi(
    val title: String,
    val amountLabel: String,
    val caption: String,
    val accentHex: String,
)

data class CategoryBreakdownUi(
    val name: String,
    val amountLabel: String,
    val shareLabel: String,
    val progress: Float,
    val accentHex: String,
)

data class CategoryOptionUi(
    val name: String,
    val accentHex: String,
)

data class ChartPointUi(
    val label: String,
    val amount: Double,
)

data class TransactionItemUi(
    val id: Long,
    val title: String,
    val subtitle: String,
    val dateLabel: String,
    val category: String,
    val amountLabel: String,
    val accentHex: String,
    val isIncome: Boolean,
)

fun ExpenseDashboard.toUiState(
    transactions: List<ExpenseTransaction>,
    availableCategories: List<ExpenseCategoryOption>,
): ExpenseDashboardUiState {
    val summary = summary
    return ExpenseDashboardUiState(
        isLoading = false,
        isSaving = false,
        balanceLabel = formatCurrency(summary.balance),
        incomeLabel = formatCurrency(summary.income),
        expenseLabel = formatCurrency(summary.expense),
        summaryCards = defaultSummaryCards(period, summary.income, summary.expense),
        selectedPeriod = period,
        selectedPeriodLabel = period.label,
        availableCategories = availableCategories.map { category ->
            CategoryOptionUi(
                name = category.name,
                accentHex = category.accentHex,
            )
        },
        chartPoints = chartEntries.map { point ->
            ChartPointUi(
                label = point.label,
                amount = point.amount,
            )
        },
        categories = categories.map { category ->
            CategoryBreakdownUi(
                name = category.name,
                amountLabel = formatCurrency(category.spent),
                shareLabel = "${(category.share * 100).roundToInt()}%",
                progress = category.share.coerceIn(0.0, 1.0).toFloat(),
                accentHex = category.accentHex,
            )
        },
        recentTransactions = transactions.map { transaction ->
            TransactionItemUi(
                id = transaction.id,
                title = transaction.title,
                subtitle = transaction.subtitle,
                dateLabel = transaction.dateLabel,
                category = transaction.category,
                amountLabel = formatSignedCurrency(transaction.amount),
                accentHex = transaction.accentHex,
                isIncome = transaction.isIncome,
            )
        },
        isEmpty = transactions.isEmpty(),
        errorMessage = null,
    )
}

private fun defaultSummaryCards(
    period: ExpensePeriod,
    income: Double = 0.0,
    expense: Double = 0.0,
): List<SummaryCardUi> = listOf(
    SummaryCardUi(
        title = "Income",
        amountLabel = formatCurrency(income),
        caption = "${period.label} cash in",
        accentHex = "#29D4A5",
    ),
    SummaryCardUi(
        title = "Spent",
        amountLabel = formatCurrency(expense),
        caption = "${period.label} total out",
        accentHex = "#FF8A5B",
    ),
)

private fun formatCurrency(amount: Double): String = "$" + formatAbsoluteAmount(amount)

private fun formatSignedCurrency(amount: Double): String {
    val prefix = if (amount >= 0) "+" else "-"
    return prefix + "$" + formatAbsoluteAmount(amount)
}

private fun formatAbsoluteAmount(amount: Double): String {
    val totalCents = (amount.absoluteValue * 100).roundToLong()
    val whole = totalCents / 100
    val cents = totalCents % 100
    val groupedWhole = whole
        .toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
    return "$groupedWhole.${cents.toString().padStart(2, '0')}"
}

private fun Double.roundToInt(): Int = roundToLong().toInt()
