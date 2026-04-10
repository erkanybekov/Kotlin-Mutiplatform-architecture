package com.erkan.experimentkmp.presentation.dashboard

import com.erkan.experimentkmp.domain.model.ExpensePeriod
import com.erkan.experimentkmp.domain.usecase.AddExpenseEntryUseCase
import com.erkan.experimentkmp.domain.usecase.DeleteExpenseEntryUseCase
import com.erkan.experimentkmp.domain.usecase.GetExpenseCategoriesUseCase
import com.erkan.experimentkmp.domain.usecase.GetExpenseDashboardUseCase
import com.erkan.experimentkmp.domain.usecase.GetRecentTransactionsUseCase
import com.erkan.experimentkmp.presentation.shared.ObservationHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExpenseDashboardStateHolder(
    private val getExpenseDashboardUseCase: GetExpenseDashboardUseCase,
    private val getRecentTransactionsUseCase: GetRecentTransactionsUseCase,
    private val getExpenseCategoriesUseCase: GetExpenseCategoriesUseCase,
    private val addExpenseEntryUseCase: AddExpenseEntryUseCase,
    private val deleteExpenseEntryUseCase: DeleteExpenseEntryUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _state = MutableStateFlow(
        ExpenseDashboardUiState(
            availableCategories = getExpenseCategoriesUseCase().map { category ->
                CategoryOptionUi(
                    name = category.name,
                    accentHex = category.accentHex,
                )
            },
        ),
    )
    val state: StateFlow<ExpenseDashboardUiState> = _state.asStateFlow()

    val currentState: ExpenseDashboardUiState
        get() = state.value

    private var hasLoaded = false

    fun load() {
        if (hasLoaded || state.value.isLoading) return
        refresh()
    }

    fun refresh() {
        fetch(state.value.selectedPeriod)
    }

    fun selectPeriod(period: ExpensePeriod) {
        if (period == state.value.selectedPeriod && hasLoaded) return
        fetch(period)
    }

    fun selectWeek() = selectPeriod(ExpensePeriod.WEEK)

    fun selectMonth() = selectPeriod(ExpensePeriod.MONTH)

    fun selectYear() = selectPeriod(ExpensePeriod.YEAR)

    fun saveEntry(
        title: String,
        amountText: String,
        category: String,
        note: String,
        isIncome: Boolean,
    ) {
        if (state.value.isSaving) return

        scope.launch {
            _state.value = state.value.copy(
                isSaving = true,
                errorMessage = null,
            )

            runCatching {
                addExpenseEntryUseCase(
                    title = title,
                    amountText = amountText,
                    category = category,
                    note = note,
                    isIncome = isIncome,
                )
                buildState(state.value.selectedPeriod)
            }.onSuccess { dashboardState ->
                hasLoaded = true
                _state.value = dashboardState.copy(
                    isSaving = false,
                    saveSuccessCount = state.value.saveSuccessCount + 1,
                    errorMessage = null,
                )
            }.onFailure { error ->
                _state.value = state.value.copy(
                    isSaving = false,
                    errorMessage = error.message ?: "Could not save entry.",
                )
            }
        }
    }

    fun clearError() {
        if (state.value.errorMessage == null) return
        _state.value = state.value.copy(errorMessage = null)
    }

    fun deleteEntry(id: Long) {
        if (state.value.isSaving) return

        scope.launch {
            _state.value = state.value.copy(
                isSaving = true,
                errorMessage = null,
            )

            runCatching {
                deleteExpenseEntryUseCase(id)
                buildState(state.value.selectedPeriod)
            }.onSuccess { dashboardState ->
                hasLoaded = true
                _state.value = dashboardState.copy(
                    isSaving = false,
                    saveSuccessCount = state.value.saveSuccessCount + 1,
                    errorMessage = null,
                )
            }.onFailure { error ->
                _state.value = state.value.copy(
                    isSaving = false,
                    errorMessage = error.message ?: "Could not delete entry.",
                )
            }
        }
    }

    fun watch(block: (ExpenseDashboardUiState) -> Unit): ObservationHandle {
        block(state.value)
        val job: Job = scope.launch {
            state.collectLatest(block)
        }
        return object : ObservationHandle {
            override fun dispose() {
                job.cancel()
            }
        }
    }

    private fun fetch(period: ExpensePeriod) {
        scope.launch {
            _state.value = state.value.copy(
                isLoading = true,
                selectedPeriod = period,
                selectedPeriodLabel = period.label,
                errorMessage = null,
            )

            runCatching {
                buildState(period)
            }.onSuccess { dashboardState ->
                hasLoaded = true
                _state.value = dashboardState.copy(
                    isLoading = false,
                    isSaving = false,
                    saveSuccessCount = state.value.saveSuccessCount,
                )
            }.onFailure { error ->
                _state.value = state.value.copy(
                    isLoading = false,
                    isSaving = false,
                    errorMessage = error.message ?: "Could not load dashboard.",
                )
            }
        }
    }

    private suspend fun buildState(period: ExpensePeriod): ExpenseDashboardUiState {
        val dashboard = getExpenseDashboardUseCase(period)
        val transactions = getRecentTransactionsUseCase()
        val categories = getExpenseCategoriesUseCase()
        return dashboard.toUiState(
            transactions = transactions,
            availableCategories = categories,
        )
    }
}
