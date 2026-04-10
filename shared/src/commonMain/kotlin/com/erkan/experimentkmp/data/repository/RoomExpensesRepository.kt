package com.erkan.experimentkmp.data.repository

import com.erkan.experimentkmp.data.local.db.ExpenseDao
import com.erkan.experimentkmp.data.local.db.ExpenseEntity
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
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class RoomExpensesRepository(
    private val expenseDao: ExpenseDao,
) : ExpensesRepository {
    override suspend fun getDashboard(period: ExpensePeriod): ExpenseDashboard {
        val allEntries = expenseDao.getAll()
        val filteredEntries = allEntries.filter { entity -> entity.isInPeriod(period) }
        val totalExpense = filteredEntries
            .filterNot { it.isIncome }
            .sumOf { it.amount }
        val totalIncome = filteredEntries
            .filter { it.isIncome }
            .sumOf { it.amount }

        return ExpenseDashboard(
            period = period,
            summary = AccountSummary(
                balance = allEntries.sumOf { it.signedAmount },
                income = totalIncome,
                expense = totalExpense,
            ),
            chartEntries = chartEntries(
                period = period,
                entries = filteredEntries,
            ),
            categories = categories(
                entries = filteredEntries,
                totalExpense = totalExpense,
            ),
        )
    }

    override suspend fun getRecentTransactions(limit: Int): List<ExpenseTransaction> = expenseDao
        .getRecent(limit)
        .map { entity -> entity.toDomain() }

    override suspend fun addEntry(entry: NewExpenseEntry) {
        expenseDao.insert(
            ExpenseEntity(
                title = entry.title,
                note = entry.note,
                amount = entry.amount,
                category = entry.category,
                type = entry.type.name,
                createdAtEpochMillis = entry.createdAtEpochMillis,
            ),
        )
    }

    override suspend fun deleteEntry(id: Long) {
        expenseDao.deleteById(id)
    }

    override fun getAvailableCategories(): List<ExpenseCategoryOption> =
        ExpenseCategoryCatalog.allSelectable()

    private fun categories(
        entries: List<ExpenseEntity>,
        totalExpense: Double,
    ): List<ExpenseCategory> = entries
        .filterNot { it.isIncome }
        .groupBy { it.category }
        .mapNotNull { (category, groupedEntries) ->
            val spent = groupedEntries.sumOf { it.amount }
            if (spent <= 0.0) return@mapNotNull null
            ExpenseCategory(
                name = category,
                spent = spent,
                share = if (totalExpense > 0.0) spent / totalExpense else 0.0,
                accentHex = ExpenseCategoryCatalog.accentHexFor(category, isIncome = false),
            )
        }
        .sortedByDescending { it.spent }

    private fun chartEntries(
        period: ExpensePeriod,
        entries: List<ExpenseEntity>,
    ): List<ExpenseChartEntry> {
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        return when (period) {
            ExpensePeriod.WEEK -> (6 downTo 0).map { daysAgo ->
                val bucketDate = today.minus(DatePeriod(days = daysAgo))
                ExpenseChartEntry(
                    label = bucketDate.dayOfWeek.name.lowercase()
                        .replaceFirstChar(Char::uppercase),
                    amount = entries
                        .filterNot { it.isIncome }
                        .filter { it.localDate == bucketDate }
                        .sumOf { it.amount },
                )
            }

            ExpensePeriod.MONTH -> (5 downTo 0).map { bucketIndex ->
                val endDate = today.minus(DatePeriod(days = bucketIndex * 5))
                val startDate = endDate.minus(DatePeriod(days = 4))
                ExpenseChartEntry(
                    label = endDate.dayOfMonth.toString(),
                    amount = entries
                        .filterNot { it.isIncome }
                        .filter { it.localDate in startDate..endDate }
                        .sumOf { it.amount },
                )
            }

            ExpensePeriod.YEAR -> (11 downTo 0).map { monthsAgo ->
                val bucketDate = firstDayOfMonth(today).minus(DatePeriod(months = monthsAgo))
                val bucketLabel = bucketDate.month.name.lowercase()
                    .replaceFirstChar(Char::uppercase)
                    .take(3)
                ExpenseChartEntry(
                    label = bucketLabel,
                    amount = entries
                        .filterNot { it.isIncome }
                        .filter { entity ->
                            val localDate = entity.localDate
                            localDate.year == bucketDate.year && localDate.monthNumber == bucketDate.monthNumber
                        }
                        .sumOf { it.amount },
                )
            }
        }
    }

    private fun ExpenseEntity.toDomain(): ExpenseTransaction = ExpenseTransaction(
        id = id,
        title = title,
        subtitle = note.ifBlank {
            if (isIncome) "Income" else category
        },
        dateLabel = localDate.let { date ->
            val month = date.month.name.lowercase().replaceFirstChar(Char::uppercase).take(3)
            "$month ${date.dayOfMonth.toString().padStart(2, '0')}"
        },
        category = category,
        amount = signedAmount,
        accentHex = ExpenseCategoryCatalog.accentHexFor(
            category = category,
            isIncome = isIncome,
        ),
        isIncome = isIncome,
    )

    private val ExpenseEntity.isIncome: Boolean
        get() = type == ExpenseEntryType.INCOME.name

    private val ExpenseEntity.signedAmount: Double
        get() = if (isIncome) amount else -amount

    private val ExpenseEntity.localDate: LocalDate
        get() = Instant.fromEpochMilliseconds(createdAtEpochMillis)
            .toLocalDateTime(timeZone)
            .date

    private fun ExpenseEntity.isInPeriod(period: ExpensePeriod): Boolean {
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        val entryDate = localDate
        return when (period) {
            ExpensePeriod.WEEK -> entryDate >= today.minus(DatePeriod(days = 6))
            ExpensePeriod.MONTH -> entryDate >= today.minus(DatePeriod(days = 29))
            ExpensePeriod.YEAR -> entryDate >= firstDayOfMonth(today).minus(DatePeriod(months = 11))
        }
    }

    private fun firstDayOfMonth(date: LocalDate): LocalDate = LocalDate(
        year = date.year,
        monthNumber = date.monthNumber,
        dayOfMonth = 1,
    )

    private companion object {
        val timeZone: TimeZone = TimeZone.currentSystemDefault()
    }
}
