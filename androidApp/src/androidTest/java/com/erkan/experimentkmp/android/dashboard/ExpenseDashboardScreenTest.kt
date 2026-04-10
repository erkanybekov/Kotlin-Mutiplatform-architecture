package com.erkan.experimentkmp.android.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
        composeRule.setContent {
            MyApplicationTheme {
                ExpenseDashboardScreen(
                    state = ExpenseDashboardUiState(),
                    onIntent = {},
                )
            }
        }

        composeRule
            .onNodeWithTag(ExpenseDashboardTestTags.EmptyTransactionsCard)
            .assertIsDisplayed()
    }

    @Test
    fun quickEntry_dispatchesInputTypeAndSubmitIntents() {
        val intents = mutableListOf<ExpenseDashboardIntent>()

        composeRule.setContent {
            MyApplicationTheme {
                ExpenseDashboardScreen(
                    state = ExpenseDashboardUiState(
                        availableCategories = listOf(
                            CategoryOptionUi("Food", "#FF8A5B"),
                            CategoryOptionUi("Transport", "#47D1B0"),
                        ),
                    ),
                    onIntent = intents::add,
                )
            }
        }

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

        composeRule.setContent {
            MyApplicationTheme {
                ExpenseDashboardScreen(
                    state = ExpenseDashboardUiState(
                        selectedPeriod = ExpensePeriod.MONTH,
                    ),
                    onIntent = intents::add,
                )
            }
        }

        composeRule
            .onNodeWithTag(ExpenseDashboardTestTags.periodChip(ExpensePeriod.YEAR))
            .performClick()

        assertTrue(intents.contains(ExpenseDashboardIntent.SelectPeriod(ExpensePeriod.YEAR)))
    }

    @Test
    fun deleteTransaction_dispatchesDeleteIntent() {
        val intents = mutableListOf<ExpenseDashboardIntent>()

        composeRule.setContent {
            MyApplicationTheme {
                ExpenseDashboardScreen(
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
                    onIntent = intents::add,
                )
            }
        }

        composeRule
            .onNodeWithTag(ExpenseDashboardTestTags.transactionDeleteButton(42L))
            .performClick()

        assertTrue(intents.contains(ExpenseDashboardIntent.DeleteEntry(42L)))
    }
}
