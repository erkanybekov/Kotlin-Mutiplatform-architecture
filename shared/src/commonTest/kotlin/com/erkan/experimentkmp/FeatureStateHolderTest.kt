package com.erkan.experimentkmp

import com.erkan.experimentkmp.domain.model.AccountSummary
import com.erkan.experimentkmp.domain.model.ExpenseCategory
import com.erkan.experimentkmp.domain.model.ExpenseCategoryCatalog
import com.erkan.experimentkmp.domain.model.ExpenseCategoryOption
import com.erkan.experimentkmp.domain.model.ExpenseChartEntry
import com.erkan.experimentkmp.domain.model.ExpenseDashboard
import com.erkan.experimentkmp.domain.model.ExpenseEntryType
import com.erkan.experimentkmp.domain.model.ExpensePeriod
import com.erkan.experimentkmp.domain.model.ExpenseTransaction
import com.erkan.experimentkmp.domain.model.NewExpenseEntry
import com.erkan.experimentkmp.domain.repository.ExpensesRepository
import com.erkan.experimentkmp.domain.usecase.AddExpenseEntryUseCase
import com.erkan.experimentkmp.domain.usecase.DeleteExpenseEntryUseCase
import com.erkan.experimentkmp.domain.usecase.GetExpenseCategoriesUseCase
import com.erkan.experimentkmp.domain.usecase.GetExpenseDashboardUseCase
import com.erkan.experimentkmp.domain.usecase.GetRecentTransactionsUseCase
import com.erkan.experimentkmp.logging.InMemoryAppLogger
import com.erkan.experimentkmp.network.configureSharedHttpClient
import com.erkan.experimentkmp.presentation.dashboard.ExpenseDashboardIntent
import com.erkan.experimentkmp.presentation.dashboard.ExpenseDashboardStateHolder
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeatureStateHolderTest {
    @Test
    fun expenseDashboardStateHolderLoadsEmptyDashboard() = runTest {
        val repository = FakeExpensesRepository()
        val stateHolder = stateHolder(repository, this)

        stateHolder.onIntent(ExpenseDashboardIntent.Load)
        delay(10)

        assertEquals("${'$'}0.00", stateHolder.currentState.balanceLabel)
        assertEquals(ExpensePeriod.MONTH, stateHolder.currentState.selectedPeriod)
        assertEquals(5, stateHolder.currentState.availableCategories.size)
        assertEquals("Food", stateHolder.currentState.entryDraft.selectedCategory)
        assertTrue(stateHolder.currentState.categories.isEmpty())
        assertTrue(stateHolder.currentState.recentTransactions.isEmpty())
        assertTrue(stateHolder.currentState.isEmpty)
    }

    @Test
    fun saveEntryAddsTransactionAndUpdatesDashboard() = runTest {
        val repository = FakeExpensesRepository()
        val stateHolder = stateHolder(repository, this)

        stateHolder.onIntent(ExpenseDashboardIntent.Load)
        delay(10)
        stateHolder.onIntent(ExpenseDashboardIntent.UpdateTitle("Whole Foods"))
        stateHolder.onIntent(ExpenseDashboardIntent.UpdateAmount("84.60"))
        stateHolder.onIntent(ExpenseDashboardIntent.UpdateNote("Fresh groceries"))
        stateHolder.onIntent(ExpenseDashboardIntent.SelectCategory("Food"))
        stateHolder.onIntent(ExpenseDashboardIntent.SubmitEntry)
        delay(10)

        assertEquals("-${'$'}84.60", stateHolder.currentState.recentTransactions.first().amountLabel)
        assertEquals("Whole Foods", stateHolder.currentState.recentTransactions.first().title)
        assertEquals(1, stateHolder.currentState.categories.size)
        assertEquals("${'$'}84.60", stateHolder.currentState.expenseLabel)
        assertEquals("", stateHolder.currentState.entryDraft.title)
        assertTrue(!stateHolder.currentState.isEmpty)
    }

    @Test
    fun selectPeriodUpdatesChart() = runTest {
        val repository = FakeExpensesRepository(
            entries = mutableListOf(
                NewExpenseEntry("Lunch", 18.0, "Food", "", ExpenseEntryType.EXPENSE, 1),
            ),
        )
        val stateHolder = stateHolder(repository, this)

        stateHolder.onIntent(ExpenseDashboardIntent.Load)
        delay(10)
        stateHolder.onIntent(ExpenseDashboardIntent.SelectPeriod(ExpensePeriod.YEAR))
        delay(10)

        assertEquals(ExpensePeriod.YEAR, stateHolder.currentState.selectedPeriod)
        assertEquals("Year", stateHolder.currentState.selectedPeriodLabel)
        assertEquals(12, stateHolder.currentState.chartPoints.size)
    }

    @Test
    fun incomeEntryIsAcceptedWithoutExpenseCategoryValidation() = runTest {
        val repository = FakeExpensesRepository()
        val stateHolder = stateHolder(repository, this)

        stateHolder.onIntent(ExpenseDashboardIntent.Load)
        delay(10)
        stateHolder.onIntent(ExpenseDashboardIntent.UpdateEntryType(true))
        stateHolder.onIntent(ExpenseDashboardIntent.UpdateTitle("Salary"))
        stateHolder.onIntent(ExpenseDashboardIntent.UpdateAmount("500.00"))
        stateHolder.onIntent(ExpenseDashboardIntent.SubmitEntry)
        delay(10)

        assertEquals(1, stateHolder.currentState.recentTransactions.size)
        assertTrue(stateHolder.currentState.recentTransactions.first().isIncome)
        assertEquals("+${'$'}500.00", stateHolder.currentState.recentTransactions.first().amountLabel)
    }

    @Test
    fun deleteEntryRemovesTransactionAndKeepsDraft() = runTest {
        val repository = FakeExpensesRepository()
        repository.addEntry(
            NewExpenseEntry("Coffee", 6.5, "Food", "", ExpenseEntryType.EXPENSE, 1)
        )
        repository.addEntry(
            NewExpenseEntry("Salary", 500.0, "Income", "", ExpenseEntryType.INCOME, 2)
        )
        val stateHolder = stateHolder(repository, this)

        stateHolder.onIntent(ExpenseDashboardIntent.Load)
        delay(10)
        stateHolder.onIntent(ExpenseDashboardIntent.UpdateTitle("Taxi"))
        stateHolder.onIntent(ExpenseDashboardIntent.UpdateAmount("18.20"))
        stateHolder.onIntent(ExpenseDashboardIntent.UpdateNote("Airport"))
        val idToDelete = stateHolder.currentState.recentTransactions.first { !it.isIncome }.id
        stateHolder.onIntent(ExpenseDashboardIntent.DeleteEntry(idToDelete))
        delay(10)

        assertEquals(1, stateHolder.currentState.recentTransactions.size)
        assertEquals("+${'$'}500.00", stateHolder.currentState.recentTransactions.first().amountLabel)
        assertEquals("${'$'}0.00", stateHolder.currentState.expenseLabel)
        assertEquals("Taxi", stateHolder.currentState.entryDraft.title)
        assertEquals("18.20", stateHolder.currentState.entryDraft.amount)
        assertEquals("Airport", stateHolder.currentState.entryDraft.note)
    }

    @Test
    fun addExpenseUseCaseRejectsInvalidAmount() = runTest {
        val repository = FakeExpensesRepository()
        val useCase = AddExpenseEntryUseCase(repository)

        val error = runCatching {
            useCase(
                title = "Coffee",
                amountText = "oops",
                category = "Food",
                note = "",
                isIncome = false,
            )
        }.exceptionOrNull()

        assertEquals("Enter a valid amount.", error?.message)
    }

    @Test
    fun networkInstrumentationLogsRequestResponseAndBody() = runTest {
        val appLogger = InMemoryAppLogger()
        val client = HttpClient(
            MockEngine {
                respond(
                    content = """{"status":"ok"}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json"),
                )
            },
        ) {
            configureSharedHttpClient(appLogger = appLogger)
        }

        client.post("https://example.com/posts?token=secret") {
            header(HttpHeaders.Authorization, "Bearer secret-token")
            contentType(ContentType.Application.Json)
            setBody("""{"hello":"world"}""")
        }
        waitUntil {
            appLogger.entries.value.any { entry ->
                entry.message.contains("<-- 200")
            }
        }

        val entries = appLogger.entries.value
        val combinedLogs = entries.joinToString("\n\n") { entry ->
            listOfNotNull(entry.message, entry.details).joinToString("\n")
        }
        assertTrue(combinedLogs.contains("--> POST /posts"))
        assertTrue(combinedLogs.contains("\"hello\":\"world\""))
        assertTrue(combinedLogs.contains("Authorization:"))
        assertTrue(!combinedLogs.contains("secret-token"))
        assertTrue(combinedLogs.contains("<-- 200 POST /posts"))
        assertTrue(combinedLogs.contains("{\"status\":\"ok\"}"))
    }

    @Test
    fun networkInstrumentationLogsFailure() = runTest {
        val appLogger = InMemoryAppLogger()
        val client = HttpClient(
            MockEngine {
                error("network down")
            },
        ) {
            configureSharedHttpClient(appLogger = appLogger)
        }

        runCatching {
            client.get("https://example.com/posts")
        }
        waitUntil {
            appLogger.entries.value.any { entry ->
                entry.level == "ERROR" && entry.message.contains("<-- ERROR GET /posts")
            }
        }

        val entries = appLogger.entries.value
        val combinedLogs = entries.joinToString("\n\n") { entry ->
            listOfNotNull(entry.message, entry.details).joinToString("\n")
        }
        assertTrue(entries.any { it.level == "ERROR" && it.message.contains("<-- ERROR GET /posts") })
        assertTrue(combinedLogs.contains("--> GET /posts"))
        assertTrue(entries.any { it.details != null })
    }

    private fun stateHolder(
        repository: FakeExpensesRepository,
        scope: TestScope,
    ): ExpenseDashboardStateHolder =
        ExpenseDashboardStateHolder(
            getExpenseDashboardUseCase = GetExpenseDashboardUseCase(repository),
            getRecentTransactionsUseCase = GetRecentTransactionsUseCase(repository),
            getExpenseCategoriesUseCase = GetExpenseCategoriesUseCase(repository),
            addExpenseEntryUseCase = AddExpenseEntryUseCase(repository),
            deleteExpenseEntryUseCase = DeleteExpenseEntryUseCase(repository),
            scope = scope.backgroundScope,
        )
}

private suspend fun waitUntil(
    timeoutMillis: Long = 500L,
    stepMillis: Long = 10L,
    condition: () -> Boolean,
) {
    val attempts = (timeoutMillis / stepMillis).toInt().coerceAtLeast(1)
    repeat(attempts) {
        if (condition()) return
        delay(stepMillis)
    }
}

private class FakeExpensesRepository(
    val entries: MutableList<NewExpenseEntry> = mutableListOf(),
) : ExpensesRepository {
    override suspend fun getDashboard(period: ExpensePeriod): ExpenseDashboard {
        val expenses = entries.filter { it.type == ExpenseEntryType.EXPENSE }
        val income = entries.filter { it.type == ExpenseEntryType.INCOME }.sumOf { it.amount }
        val expense = expenses.sumOf { it.amount }
        val categories = expenses
            .groupBy { it.category }
            .map { (category, groupedEntries) ->
                val spent = groupedEntries.sumOf { it.amount }
                ExpenseCategory(
                    name = category,
                    spent = spent,
                    share = if (expense > 0.0) spent / expense else 0.0,
                    accentHex = ExpenseCategoryCatalog.accentHexFor(category, isIncome = false),
                )
            }

        return ExpenseDashboard(
            period = period,
            summary = AccountSummary(
                balance = income - expense,
                income = income,
                expense = expense,
            ),
            chartEntries = when (period) {
                ExpensePeriod.YEAR -> List(12) { index ->
                    ExpenseChartEntry(label = "M${index + 1}", amount = 10.0 * (index + 1))
                }

                ExpensePeriod.MONTH -> List(6) { index ->
                    ExpenseChartEntry(label = "${(index + 1) * 5}", amount = if (index == 5) expense else 0.0)
                }

                ExpensePeriod.WEEK -> listOf(
                    ExpenseChartEntry("Mon", 0.0),
                    ExpenseChartEntry("Tue", 0.0),
                    ExpenseChartEntry("Wed", 0.0),
                    ExpenseChartEntry("Thu", 0.0),
                    ExpenseChartEntry("Fri", expense),
                    ExpenseChartEntry("Sat", 0.0),
                    ExpenseChartEntry("Sun", 0.0),
                )
            },
            categories = categories,
        )
    }

    override suspend fun getRecentTransactions(limit: Int): List<ExpenseTransaction> = entries
        .asReversed()
        .take(limit)
        .mapIndexed { index, entry ->
            ExpenseTransaction(
                id = (index + 1).toLong(),
                title = entry.title,
                subtitle = entry.note.ifBlank {
                    if (entry.type == ExpenseEntryType.INCOME) "Income" else entry.category
                },
                dateLabel = "Apr 09",
                category = if (entry.type == ExpenseEntryType.INCOME) "Income" else entry.category,
                amount = if (entry.type == ExpenseEntryType.INCOME) entry.amount else -entry.amount,
                accentHex = ExpenseCategoryCatalog.accentHexFor(
                    category = entry.category,
                    isIncome = entry.type == ExpenseEntryType.INCOME,
                ),
                isIncome = entry.type == ExpenseEntryType.INCOME,
            )
        }

    override suspend fun addEntry(entry: NewExpenseEntry) {
        entries += entry
    }

    override suspend fun deleteEntry(id: Long) {
        if (id <= 0L) return
        val index = entries.lastIndex - (id.toInt() - 1)
        if (index in entries.indices) {
            entries.removeAt(index)
        }
    }

    override fun getAvailableCategories(): List<ExpenseCategoryOption> =
        ExpenseCategoryCatalog.allSelectable()
}
