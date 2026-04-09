package com.erkan.experimentkmp.android.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erkan.experimentkmp.android.MyApplicationTheme
import com.erkan.experimentkmp.android.uikit.ExpenseBackgroundBrush
import com.erkan.experimentkmp.android.uikit.ExpenseCategoryCard
import com.erkan.experimentkmp.android.uikit.ExpenseCategorySelector
import com.erkan.experimentkmp.android.uikit.ExpenseEmptyCard
import com.erkan.experimentkmp.android.uikit.ExpenseGlow
import com.erkan.experimentkmp.android.uikit.ExpenseHeroCard
import com.erkan.experimentkmp.android.uikit.ExpenseInputField
import com.erkan.experimentkmp.android.uikit.ExpenseLineChart
import com.erkan.experimentkmp.android.uikit.ExpensePalette
import com.erkan.experimentkmp.android.uikit.ExpensePeriodSwitcher
import com.erkan.experimentkmp.android.uikit.ExpensePrimaryButton
import com.erkan.experimentkmp.android.uikit.ExpenseSectionTitle
import com.erkan.experimentkmp.android.uikit.ExpenseTransactionCard
import com.erkan.experimentkmp.android.uikit.ExpenseTypeSwitcher
import com.erkan.experimentkmp.domain.model.ExpensePeriod
import com.erkan.experimentkmp.presentation.dashboard.CategoryBreakdownUi
import com.erkan.experimentkmp.presentation.dashboard.CategoryOptionUi
import com.erkan.experimentkmp.presentation.dashboard.ChartPointUi
import com.erkan.experimentkmp.presentation.dashboard.ExpenseDashboardUiState
import com.erkan.experimentkmp.presentation.dashboard.SummaryCardUi
import com.erkan.experimentkmp.presentation.dashboard.TransactionItemUi

@Composable
fun ExpenseDashboardScreen(
    state: ExpenseDashboardUiState,
    onPeriodSelected: (ExpensePeriod) -> Unit,
    onSaveEntry: (String, String, String, String, Boolean) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var isIncome by rememberSaveable { mutableStateOf(false) }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var wasSaving by remember { mutableStateOf(false) }

    LaunchedEffect(state.availableCategories) {
        if (selectedCategory.isBlank() && state.availableCategories.isNotEmpty()) {
            selectedCategory = state.availableCategories.first().name
        }
    }

    LaunchedEffect(state.isSaving, state.errorMessage, state.recentTransactions.size) {
        if (wasSaving && !state.isSaving && state.errorMessage == null) {
            title = ""
            amount = ""
            note = ""
            isIncome = false
            selectedCategory = state.availableCategories.firstOrNull()?.name.orEmpty()
        }
        wasSaving = state.isSaving
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ExpenseBackgroundBrush),
    ) {
        ExpenseGlow(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 96.dp, y = (-52).dp),
            color = ExpensePalette.AccentWarm,
        )
        ExpenseGlow(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-110).dp, y = 120.dp),
            color = ExpensePalette.AccentIndigo,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = WindowInsets.statusBars.asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    DashboardHeader()

                    state.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    ExpenseHeroCard(
                        balanceLabel = state.balanceLabel,
                        summaryCards = state.summaryCards,
                    )

                    QuickEntrySection(
                        title = title,
                        amount = amount,
                        note = note,
                        isIncome = isIncome,
                        availableCategories = state.availableCategories,
                        selectedCategory = selectedCategory,
                        isSaving = state.isSaving,
                        onTitleChange = { title = it },
                        onAmountChange = { amount = it },
                        onNoteChange = { note = it },
                        onIncomeToggle = { isIncome = it },
                        onCategorySelected = { selectedCategory = it },
                        onSave = {
                            onSaveEntry(
                                title,
                                amount,
                                if (isIncome) "" else selectedCategory,
                                note,
                                isIncome,
                            )
                        },
                    )

                    ExpensePeriodSwitcher(
                        selectedPeriod = state.selectedPeriod,
                        onPeriodSelected = onPeriodSelected,
                    )

                    AnalyticsSection(chartPoints = state.chartPoints)

                    CategoriesSection(categories = state.categories)

                    TransactionsSection(transactions = state.recentTransactions)
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Personal ledger",
            style = MaterialTheme.typography.labelLarge,
            color = ExpensePalette.TextSecondary,
        )
        Text(
            text = "Expense Flow",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = "Track real entries locally. No seeded data, no sync required.",
            style = MaterialTheme.typography.bodyMedium,
            color = ExpensePalette.TextMuted,
        )
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
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onIncomeToggle: (Boolean) -> Unit,
    onCategorySelected: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ExpensePalette.SurfaceStrong, shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ExpenseSectionTitle("New entry")
        Text(
            text = "Save an expense or income directly to your local database.",
            style = MaterialTheme.typography.bodyMedium,
            color = ExpensePalette.TextMuted,
        )

        ExpenseTypeSwitcher(
            isIncome = isIncome,
            onToggle = onIncomeToggle,
        )

        ExpenseInputField(
            value = title,
            onValueChange = onTitleChange,
            label = "Title",
        )

        ExpenseInputField(
            value = amount,
            onValueChange = onAmountChange,
            label = "Amount",
        )

        ExpenseInputField(
            value = note,
            onValueChange = onNoteChange,
            label = "Note (optional)",
            singleLine = false,
        )

        if (!isIncome && availableCategories.isNotEmpty()) {
            ExpenseCategorySelector(
                categories = availableCategories,
                selectedCategory = selectedCategory,
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

@Composable
private fun AnalyticsSection(
    chartPoints: List<ChartPointUi>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ExpensePalette.SurfaceStrong, shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ExpenseSectionTitle("Analytics")
                Text("Spending over the selected period", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF7382A3))
            }
        }
        if (chartPoints.any { it.amount > 0.0 }) {
            ExpenseLineChart(points = chartPoints)
        } else {
            ExpenseEmptyCard(
                title = "No chart data yet",
                message = "Add a few expenses and this section will show your spending pattern.",
            )
        }
    }
}

@Composable
private fun CategoriesSection(
    categories: List<CategoryBreakdownUi>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ExpenseSectionTitle("Categories")
        if (categories.isEmpty()) {
            ExpenseEmptyCard(
                title = "No expense categories yet",
                message = "Category totals appear after you save expense entries.",
            )
        } else {
            categories.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { category ->
                        ExpenseCategoryCard(
                            modifier = Modifier.weight(1f),
                            category = category,
                        )
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionsSection(
    transactions: List<TransactionItemUi>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ExpenseSectionTitle("Recent transactions")
        if (transactions.isEmpty()) {
            ExpenseEmptyCard(
                title = "No entries yet",
                message = "Your latest expenses and income will appear here after the first save.",
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                transactions.forEach { transaction ->
                    ExpenseTransactionCard(transaction = transaction)
                }
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
        )
    }
}
