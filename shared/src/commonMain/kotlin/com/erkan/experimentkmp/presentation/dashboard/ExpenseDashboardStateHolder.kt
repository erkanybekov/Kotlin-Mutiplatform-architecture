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
    private val initialCategories = categoryOptions()

    private val _state = MutableStateFlow(
        ExpenseDashboardUiState(
            availableCategories = initialCategories,
            entryDraft = emptyDraft(initialCategories),
        ),
    )
    val state: StateFlow<ExpenseDashboardUiState> = _state.asStateFlow()

    val currentState: ExpenseDashboardUiState
        get() = state.value

    private var hasLoaded = false

    fun onIntent(intent: ExpenseDashboardIntent) {
        when (intent) {
            ExpenseDashboardIntent.Load -> load()
            ExpenseDashboardIntent.Refresh -> refresh()
            ExpenseDashboardIntent.SubmitEntry -> handleSubmitEntry()
            ExpenseDashboardIntent.DismissError -> dismissError()
            is ExpenseDashboardIntent.SelectPeriod -> selectPeriod(intent.period)
            is ExpenseDashboardIntent.UpdateTitle -> updateDraft { draft ->
                draft.copy(
                    title = intent.value,
                    titleError = draft.titleError?.let { validateTitle(intent.value) },
                )
            }

            is ExpenseDashboardIntent.UpdateAmount -> updateDraft { draft ->
                draft.copy(
                    amount = intent.value,
                    amountError = draft.amountError?.let { validateAmount(intent.value) },
                )
            }

            is ExpenseDashboardIntent.UpdateNote -> updateDraft { draft ->
                draft.copy(note = intent.value)
            }

            is ExpenseDashboardIntent.UpdateEntryType -> updateDraft { draft ->
                draft.copy(
                    isIncome = intent.isIncome,
                    categoryError = null,
                )
            }

            is ExpenseDashboardIntent.SelectCategory -> updateDraft { draft ->
                draft.copy(
                    selectedCategory = intent.category,
                    categoryError = null,
                )
            }

            is ExpenseDashboardIntent.DeleteEntry -> handleDeleteEntry(intent.id)
        }
    }

    fun load() {
        if (hasLoaded || state.value.isLoading) return
        fetch(state.value.selectedPeriod)
    }

    fun refresh() {
        fetch(state.value.selectedPeriod)
    }

    fun selectPeriod(period: ExpensePeriod) {
        if (period == state.value.selectedPeriod && hasLoaded) return
        fetch(period)
    }

    fun selectWeek() = onIntent(ExpenseDashboardIntent.SelectPeriod(ExpensePeriod.WEEK))

    fun selectMonth() = onIntent(ExpenseDashboardIntent.SelectPeriod(ExpensePeriod.MONTH))

    fun selectYear() = onIntent(ExpenseDashboardIntent.SelectPeriod(ExpensePeriod.YEAR))

    fun updateTitle(value: String) = onIntent(ExpenseDashboardIntent.UpdateTitle(value))

    fun updateAmount(value: String) = onIntent(ExpenseDashboardIntent.UpdateAmount(value))

    fun updateNote(value: String) = onIntent(ExpenseDashboardIntent.UpdateNote(value))

    fun updateEntryType(isIncome: Boolean) =
        onIntent(ExpenseDashboardIntent.UpdateEntryType(isIncome))

    fun selectCategory(category: String) =
        onIntent(ExpenseDashboardIntent.SelectCategory(category))

    fun submitEntry() = onIntent(ExpenseDashboardIntent.SubmitEntry)

    fun clearError() = onIntent(ExpenseDashboardIntent.DismissError)

    fun deleteEntry(id: Long) = onIntent(ExpenseDashboardIntent.DeleteEntry(id))

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

    private fun handleSubmitEntry() {
        if (state.value.isSaving) return

        val validatedDraft = state.value.entryDraft.validated()
        if (validatedDraft.hasErrors) {
            _state.value = state.value.copy(entryDraft = validatedDraft)
            return
        }

        val selectedPeriod = state.value.selectedPeriod
        _state.value = state.value.copy(
            isSaving = true,
            errorMessage = null,
            entryDraft = validatedDraft,
        )

        scope.launch {
            runCatching {
                addExpenseEntryUseCase(
                    title = validatedDraft.title,
                    amountText = validatedDraft.amount,
                    category = validatedDraft.selectedCategory,
                    note = validatedDraft.note,
                    isIncome = validatedDraft.isIncome,
                )
                buildState(
                    period = selectedPeriod,
                    draft = emptyDraft(categoryOptions()),
                )
            }.onSuccess { dashboardState ->
                hasLoaded = true
                _state.value = dashboardState.copy(
                    isSaving = false,
                    errorMessage = null,
                )
            }.onFailure { error ->
                _state.value = state.value.copy(
                    isSaving = false,
                    entryDraft = validatedDraft,
                    errorMessage = error.message ?: "Could not save entry.",
                )
            }
        }
    }

    private fun dismissError() {
        if (state.value.errorMessage == null) return
        _state.value = state.value.copy(errorMessage = null)
    }

    private fun handleDeleteEntry(id: Long) {
        if (state.value.isSaving) return

        val selectedPeriod = state.value.selectedPeriod
        val draft = state.value.entryDraft
        _state.value = state.value.copy(
            isSaving = true,
            errorMessage = null,
        )

        scope.launch {
            runCatching {
                deleteExpenseEntryUseCase(id)
                buildState(
                    period = selectedPeriod,
                    draft = draft,
                )
            }.onSuccess { dashboardState ->
                hasLoaded = true
                _state.value = dashboardState.copy(
                    isSaving = false,
                    errorMessage = null,
                )
            }.onFailure { error ->
                _state.value = state.value.copy(
                    isSaving = false,
                    entryDraft = draft,
                    errorMessage = error.message ?: "Could not delete entry.",
                )
            }
        }
    }

    private fun updateDraft(
        transform: (ExpenseEntryDraftUiState) -> ExpenseEntryDraftUiState,
    ) {
        val availableCategories = state.value.availableCategories
        val updatedDraft = transform(state.value.entryDraft).normalized(availableCategories)
        _state.value = state.value.copy(
            entryDraft = updatedDraft,
            errorMessage = null,
        )
    }

    private fun fetch(period: ExpensePeriod) {
        if (state.value.isLoading) return

        val draft = state.value.entryDraft
        _state.value = state.value.copy(
            isLoading = true,
            selectedPeriod = period,
            selectedPeriodLabel = period.label,
            errorMessage = null,
        )

        scope.launch {
            runCatching {
                buildState(
                    period = period,
                    draft = draft,
                )
            }.onSuccess { dashboardState ->
                hasLoaded = true
                _state.value = dashboardState.copy(
                    isLoading = false,
                    isSaving = false,
                )
            }.onFailure { error ->
                _state.value = state.value.copy(
                    isLoading = false,
                    isSaving = false,
                    entryDraft = draft,
                    errorMessage = error.message ?: "Could not load dashboard.",
                )
            }
        }
    }

    private suspend fun buildState(
        period: ExpensePeriod,
        draft: ExpenseEntryDraftUiState,
    ): ExpenseDashboardUiState {
        val dashboard = getExpenseDashboardUseCase(period)
        val transactions = getRecentTransactionsUseCase()
        val categories = getExpenseCategoriesUseCase()
        val availableCategories = categories.map { category ->
            CategoryOptionUi(
                name = category.name,
                accentHex = category.accentHex,
            )
        }
        return dashboard.toUiState(
            transactions = transactions,
            availableCategories = categories,
            entryDraft = draft.normalized(availableCategories),
        )
    }

    private fun categoryOptions(): List<CategoryOptionUi> = getExpenseCategoriesUseCase().map { category ->
        CategoryOptionUi(
            name = category.name,
            accentHex = category.accentHex,
        )
    }

    private fun emptyDraft(
        availableCategories: List<CategoryOptionUi>,
    ): ExpenseEntryDraftUiState = ExpenseEntryDraftUiState(
        selectedCategory = availableCategories.firstOrNull()?.name.orEmpty(),
    )

    private fun ExpenseEntryDraftUiState.normalized(
        availableCategories: List<CategoryOptionUi>,
    ): ExpenseEntryDraftUiState {
        val defaultCategory = availableCategories.firstOrNull()?.name.orEmpty()
        val resolvedCategory = when {
            selectedCategory.isBlank() -> defaultCategory
            availableCategories.any { it.name == selectedCategory } -> selectedCategory
            else -> defaultCategory
        }
        return copy(selectedCategory = resolvedCategory)
    }

    private fun ExpenseEntryDraftUiState.validated(): ExpenseEntryDraftUiState = copy(
        titleError = validateTitle(title),
        amountError = validateAmount(amount),
        categoryError = validateCategory(
            isIncome = isIncome,
            selectedCategory = selectedCategory,
        ),
    )

    private val ExpenseEntryDraftUiState.hasErrors: Boolean
        get() = titleError != null || amountError != null || categoryError != null

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
}
