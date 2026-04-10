package com.erkan.experimentkmp.android.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erkan.experimentkmp.android.MyApplicationTheme
import com.erkan.experimentkmp.android.uikit.ExpenseBalanceCard
import com.erkan.experimentkmp.android.uikit.ExpenseCategoryDropdown
import com.erkan.experimentkmp.android.uikit.ExpenseCategorySummaryCard
import com.erkan.experimentkmp.android.uikit.ExpenseEmptyStateCard
import com.erkan.experimentkmp.android.uikit.ExpenseInputField
import com.erkan.experimentkmp.android.uikit.ExpenseLineChart
import com.erkan.experimentkmp.android.uikit.ExpensePeriodSelector
import com.erkan.experimentkmp.android.uikit.ExpensePrimaryButton
import com.erkan.experimentkmp.android.uikit.ExpenseSectionHeader
import com.erkan.experimentkmp.android.uikit.ExpenseTransactionRow
import com.erkan.experimentkmp.android.uikit.ExpenseTypeSelector
import com.erkan.experimentkmp.domain.model.ExpensePeriod
import com.erkan.experimentkmp.presentation.dashboard.CategoryBreakdownUi
import com.erkan.experimentkmp.presentation.dashboard.CategoryOptionUi
import com.erkan.experimentkmp.presentation.dashboard.ChartPointUi
import com.erkan.experimentkmp.presentation.dashboard.ExpenseDashboardUiState
import com.erkan.experimentkmp.presentation.dashboard.SummaryCardUi
import com.erkan.experimentkmp.presentation.dashboard.TransactionItemUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDashboardScreen(
    state: ExpenseDashboardUiState,
    onPeriodSelected: (ExpensePeriod) -> Unit,
    onSaveEntry: (String, String, String, String, Boolean) -> Unit,
    onDeleteEntry: (Long) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var isIncome by rememberSaveable { mutableStateOf(false) }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var titleError by rememberSaveable { mutableStateOf<String?>(null) }
    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryError by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(state.availableCategories) {
        if (selectedCategory.isBlank() && state.availableCategories.isNotEmpty()) {
            selectedCategory = state.availableCategories.first().name
        }
    }

    LaunchedEffect(state.saveSuccessCount) {
        if (state.saveSuccessCount > 0) {
            title = ""
            amount = ""
            note = ""
            isIncome = false
            selectedCategory = state.availableCategories.firstOrNull()?.name.orEmpty()
            titleError = null
            amountError = null
            categoryError = null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Expenses")
                        Text(
                            text = "Stored locally on this device",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ExpenseBalanceCard(
                    balanceLabel = state.balanceLabel,
                    summaryCards = state.summaryCards,
                )
            }

            state.errorMessage?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item {
                QuickEntrySection(
                    title = title,
                    amount = amount,
                    note = note,
                    isIncome = isIncome,
                    availableCategories = state.availableCategories,
                    selectedCategory = selectedCategory,
                    isSaving = state.isSaving,
                    titleError = titleError,
                    amountError = amountError,
                    categoryError = categoryError,
                    onTitleChange = {
                        title = it
                        titleError = validateTitle(it)
                    },
                    onAmountChange = {
                        amount = it
                        amountError = validateAmount(it)
                    },
                    onNoteChange = { note = it },
                    onIncomeToggle = {
                        isIncome = it
                        categoryError = null
                    },
                    onCategorySelected = {
                        selectedCategory = it
                        categoryError = null
                    },
                    onSave = {
                        titleError = validateTitle(title)
                        amountError = validateAmount(amount)
                        categoryError = validateCategory(
                            isIncome = isIncome,
                            selectedCategory = selectedCategory,
                        )

                        if (titleError == null && amountError == null && categoryError == null) {
                            onSaveEntry(
                                title,
                                amount,
                                if (isIncome) "" else selectedCategory,
                                note,
                                isIncome,
                            )
                        }
                    },
                )
            }

            item {
                AnalyticsSection(
                    selectedPeriod = state.selectedPeriod,
                    chartPoints = state.chartPoints,
                    onPeriodSelected = onPeriodSelected,
                )
            }

            item {
                CategoriesSection(categories = state.categories)
            }

            item {
                TransactionsSection(
                    transactions = state.recentTransactions,
                    onDeleteEntry = onDeleteEntry,
                )
            }
        }
    }
}

@Composable
private fun QuickEntrySection(
    title: String,
    amount: String,
    note: String,
    isIncome: Boolean,
    availableCategories: List<CategoryOptionUi>,
    selectedCategory: String,
    isSaving: Boolean,
    titleError: String?,
    amountError: String?,
    categoryError: String?,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onIncomeToggle: (Boolean) -> Unit,
    onCategorySelected: (String) -> Unit,
    onSave: () -> Unit,
) {
    Card(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ExpenseSectionHeader(
                title = "New entry",
                supportingText = "Add income or an expense to the local database.",
            )

            ExpenseTypeSelector(
                isIncome = isIncome,
                onToggle = onIncomeToggle,
            )

            ExpenseInputField(
                value = title,
                onValueChange = onTitleChange,
                label = "Title",
                errorText = titleError,
            )

            ExpenseInputField(
                value = amount,
                onValueChange = onAmountChange,
                label = "Amount",
                errorText = amountError,
            )

            ExpenseInputField(
                value = note,
                onValueChange = onNoteChange,
                label = "Note",
                singleLine = false,
            )

            if (!isIncome && availableCategories.isNotEmpty()) {
                ExpenseCategoryDropdown(
                    categories = availableCategories,
                    selectedCategory = selectedCategory,
                    errorText = categoryError,
                    onCategorySelected = onCategorySelected,
                )
            }

            ExpensePrimaryButton(
                title = if (isIncome) "Save income" else "Save expense",
                isLoading = isSaving,
                onClick = onSave,
            )
        }
    }
}

private fun validateTitle(value: String): String? =
    if (value.trim().isEmpty()) "Title is required." else null

private fun validateAmount(value: String): String? {
    val amount = value.trim().replace(',', '.').toDoubleOrNull()
    return when {
        value.trim().isEmpty() -> "Amount is required."
        amount == null || amount <= 0.0 -> "Enter a valid amount."
        else -> null
    }
}

private fun validateCategory(
    isIncome: Boolean,
    selectedCategory: String,
): String? = if (!isIncome && selectedCategory.isBlank()) "Choose a category." else null

@Composable
private fun AnalyticsSection(
    selectedPeriod: ExpensePeriod,
    chartPoints: List<ChartPointUi>,
    onPeriodSelected: (ExpensePeriod) -> Unit,
) {
    Card(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ExpenseSectionHeader(
                title = "Analytics",
                supportingText = "Spending breakdown for the selected period.",
            )
            ExpensePeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = onPeriodSelected,
            )
            if (chartPoints.any { it.amount > 0.0 }) {
                ExpenseLineChart(points = chartPoints)
            } else {
                ExpenseEmptyStateCard(
                    title = "No chart data yet",
                    message = "Add a few expenses and the chart will start reflecting them.",
                )
            }
        }
    }
}

@Composable
private fun CategoriesSection(
    categories: List<CategoryBreakdownUi>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ExpenseSectionHeader(
            title = "Categories",
            supportingText = "Totals are derived from saved expense entries.",
        )
        if (categories.isEmpty()) {
            ExpenseEmptyStateCard(
                title = "No expense categories yet",
                message = "Category totals appear after you save expense entries.",
            )
        } else {
            categories.forEach { category ->
                ExpenseCategorySummaryCard(category = category)
            }
        }
    }
}

@Composable
private fun TransactionsSection(
    transactions: List<TransactionItemUi>,
    onDeleteEntry: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ExpenseSectionHeader(
            title = "Recent transactions",
            supportingText = "Most recent entries are shown first.",
        )
        if (transactions.isEmpty()) {
            ExpenseEmptyStateCard(
                title = "No entries yet",
                message = "Your latest expenses and income will appear here after the first save.",
            )
        } else {
            transactions.forEach { transaction ->
                ExpenseTransactionRow(
                    transaction = transaction,
                    onDelete = { onDeleteEntry(transaction.id) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun ExpenseDashboardPreview() {
    MyApplicationTheme {
        ExpenseDashboardScreen(
            state = ExpenseDashboardUiState(
                balanceLabel = "${'$'}12,480.86",
                incomeLabel = "${'$'}8,420.00",
                expenseLabel = "${'$'}3,245.42",
                summaryCards = listOf(
                    SummaryCardUi("Income", "${'$'}8,420.00", "Month cash in", "#29D4A5"),
                    SummaryCardUi("Spent", "${'$'}3,245.42", "Month total out", "#FF8A5B"),
                ),
                availableCategories = listOf(
                    CategoryOptionUi("Food", "#FF8A5B"),
                    CategoryOptionUi("Shopping", "#FFBE4D"),
                    CategoryOptionUi("Transport", "#47D1B0"),
                ),
                chartPoints = listOf(
                    ChartPointUi("Mon", 210.0),
                    ChartPointUi("Tue", 380.0),
                    ChartPointUi("Wed", 240.0),
                    ChartPointUi("Thu", 510.0),
                    ChartPointUi("Fri", 360.0),
                    ChartPointUi("Sat", 660.0),
                    ChartPointUi("Sun", 420.0),
                ),
                categories = listOf(
                    CategoryBreakdownUi("Food", "${'$'}1,280.20", "39%", 0.39f, "#FF8A5B"),
                    CategoryBreakdownUi("Shopping", "${'$'}940.10", "29%", 0.29f, "#FFBE4D"),
                ),
                recentTransactions = listOf(
                    TransactionItemUi(1, "Whole Foods", "Fresh groceries", "Apr 09", "Food", "-${'$'}84.60", "#FF8A5B", false),
                    TransactionItemUi(2, "Salary Deposit", "Primary income", "Apr 05", "Income", "+${'$'}2,950.00", "#29D4A5", true),
                ),
                isEmpty = false,
            ),
            onPeriodSelected = {},
            onSaveEntry = { _, _, _, _, _ -> },
            onDeleteEntry = {},
        )
    }
}
