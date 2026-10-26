package com.example.bankimukhonde

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.ui.screens.LoansOverviewScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoanScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loansOverview_displaysHeadingAndActions() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        composeTestRule.setContent {
            LoansOverviewScreen(
                viewModel = viewModel,
                onBack = {},
                onIssueLoan = {}
            )
        }

        composeTestRule.onNodeWithText("Loans Management").assertIsDisplayed()
        composeTestRule.onNodeWithText("Issue Loan").assertIsDisplayed()
    }

    @Test
    fun loansOverview_clicksIssueLoan() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        var clicked = false

        composeTestRule.setContent {
            LoansOverviewScreen(
                viewModel = viewModel,
                onBack = {},
                onIssueLoan = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Issue Loan").performClick()
        assertTrue(clicked)
    }
}
