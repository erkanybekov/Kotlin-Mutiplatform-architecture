package com.erkan.experimentkmp.android.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.erkan.experimentkmp.android.MyApplicationTheme
import com.erkan.experimentkmp.domain.model.ExpensePeriod
import com.erkan.experimentkmp.presentation.dashboard.CategoryOptionUi
import com.erkan.experimentkmp.presentation.dashboard.ExpenseDashboardIntent
import com.erkan.experimentkmp.presentation.dashboard.ExpenseDashboardUiState
import com.erkan.experimentkmp.presentation.dashboard.ExpenseEntryDraftUiState
import com.erkan.experimentkmp.presentation.dashboard.TransactionItemUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExpenseDashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyTransactionsState_isShown() {
        setDashboardContent(state = ExpenseDashboardUiState())

        composeRule.scrollToTag(ExpenseDashboardTestTags.EmptyTransactionsCard)

        composeRule
            .onNodeWithTag(ExpenseDashboardTestTags.EmptyTransactionsCard)
            .assertIsDisplayed()
    }

    @Test
    fun quickEntry_dispatchesInputTypeAndSubmitIntents() {
        val intents = mutableListOf<ExpenseDashboardIntent>()

        setDashboardContent(
            state = ExpenseDashboardUiState(
                availableCategories = listOf(
                    CategoryOptionUi("Food", "#FF8A5B"),
                    CategoryOptionUi("Transport", "#47D1B0"),
                ),
            ),
            intents = intents,
        )

        composeRule.scrollToTag(ExpenseDashboardTestTags.QuickEntrySection)
        composeRule.onNodeWithTag(ExpenseDashboardTestTags.TitleInput).performTextInput("Coffee")
        composeRule.onNodeWithTag(ExpenseDashboardTestTags.AmountInput).performTextInput("12.50")
        composeRule.onNodeWithTag(ExpenseDashboardTestTags.NoteInput).performTextInput("Morning drink")
        composeRule.onNodeWithTag(ExpenseDashboardTestTags.IncomeTypeChip).performClick()
        composeRule.onNodeWithTag(ExpenseDashboardTestTags.SaveButton).performClick()

        assertEquals(
            ExpenseDashboardIntent.UpdateTitle("Coffee"),
            intents.filterIsInstance<ExpenseDashboardIntent.UpdateTitle>().last(),
        )
        assertEquals(
            ExpenseDashboardIntent.UpdateAmount("12.50"),
            intents.filterIsInstance<ExpenseDashboardIntent.UpdateAmount>().last(),
        )
        assertEquals(
            ExpenseDashboardIntent.UpdateNote("Morning drink"),
            intents.filterIsInstance<ExpenseDashboardIntent.UpdateNote>().last(),
        )
        assertTrue(intents.contains(ExpenseDashboardIntent.UpdateEntryType(true)))
        assertTrue(intents.contains(ExpenseDashboardIntent.SubmitEntry))
    }

    @Test
    fun periodSelection_dispatchesIntent() {
        val intents = mutableListOf<ExpenseDashboardIntent>()

        setDashboardContent(
            state = ExpenseDashboardUiState(
                selectedPeriod = ExpensePeriod.MONTH,
            ),
            intents = intents,
        )

        composeRule.scrollToTag(ExpenseDashboardTestTags.AnalyticsSection)
        composeRule
            .onNodeWithTag(ExpenseDashboardTestTags.periodChip(ExpensePeriod.YEAR))
            .performClick()

        assertTrue(intents.contains(ExpenseDashboardIntent.SelectPeriod(ExpensePeriod.YEAR)))
    }

    @Test
    fun deleteTransaction_dispatchesDeleteIntent() {
        val intents = mutableListOf<ExpenseDashboardIntent>()

        setDashboardContent(
            state = ExpenseDashboardUiState(
                recentTransactions = listOf(
                    TransactionItemUi(
                        id = 42L,
                        title = "Taxi",
                        subtitle = "Airport",
                        dateLabel = "Apr 10",
                        category = "Transport",
                        amountLabel = "-$24.00",
                        accentHex = "#47D1B0",
                        isIncome = false,
                    ),
                ),
                isEmpty = false,
            ),
            intents = intents,
        )

        composeRule.scrollToTag(ExpenseDashboardTestTags.transactionDeleteButton(42L))
        composeRule
            .onNodeWithTag(ExpenseDashboardTestTags.transactionDeleteButton(42L))
            .performClick()

        assertTrue(intents.contains(ExpenseDashboardIntent.DeleteEntry(42L)))
    }

    private fun setDashboardContent(
        state: ExpenseDashboardUiState,
        intents: MutableList<ExpenseDashboardIntent> = mutableListOf(),
    ) {
        composeRule.setContent {
            var currentState by mutableStateOf(state)

            MyApplicationTheme {
                ExpenseDashboardScreen(
                    state = currentState,
                    onIntent = { intent ->
                        intents += intent
                        currentState = currentState.reduceForTest(intent)
                    },
                )
            }
        }
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.scrollToTag(tag: String) {
        onNodeWithTag(ExpenseDashboardTestTags.ContentList)
            .performScrollToNode(hasTestTag(tag))
    }

    private fun ExpenseDashboardUiState.reduceForTest(
        intent: ExpenseDashboardIntent,
    ): ExpenseDashboardUiState = when (intent) {
        is ExpenseDashboardIntent.UpdateTitle -> copy(
            entryDraft = entryDraft.copy(title = intent.value),
        )

        is ExpenseDashboardIntent.UpdateAmount -> copy(
            entryDraft = entryDraft.copy(amount = intent.value),
        )

        is ExpenseDashboardIntent.UpdateNote -> copy(
            entryDraft = entryDraft.copy(note = intent.value),
        )

        is ExpenseDashboardIntent.UpdateEntryType -> copy(
            entryDraft = entryDraft.copy(isIncome = intent.isIncome),
        )

        is ExpenseDashboardIntent.SelectCategory -> copy(
            entryDraft = entryDraft.copy(selectedCategory = intent.category),
        )

        is ExpenseDashboardIntent.SelectPeriod -> copy(
            selectedPeriod = intent.period,
            selectedPeriodLabel = intent.period.label,
        )

        is ExpenseDashboardIntent.DeleteEntry -> {
            val updatedTransactions = recentTransactions.filterNot { it.id == intent.id }
            copy(
                recentTransactions = updatedTransactions,
                isEmpty = updatedTransactions.isEmpty(),
            )
        }

        ExpenseDashboardIntent.Load,
        ExpenseDashboardIntent.Refresh,
        ExpenseDashboardIntent.SubmitEntry,
        ExpenseDashboardIntent.DismissError,
        -> this
    }
}
