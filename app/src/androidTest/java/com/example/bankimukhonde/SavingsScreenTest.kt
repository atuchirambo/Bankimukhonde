package com.example.bankimukhonde

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.ui.screens.SavingsOverviewScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SavingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun savingsOverview_displaysHeadingAndActions() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        composeTestRule.setContent {
            SavingsOverviewScreen(
                viewModel = viewModel,
                onBack = {},
                onRecordContribution = {}
            )
        }

        composeTestRule.onNodeWithText("Savings Overview").assertIsDisplayed()
        composeTestRule.onNodeWithText("Record Contribution").assertIsDisplayed()
    }

    @Test
    fun savingsOverview_clicksRecordContribution() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        var clicked = false

        composeTestRule.setContent {
            SavingsOverviewScreen(
                viewModel = viewModel,
                onBack = {},
                onRecordContribution = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Record Contribution").performClick()
        assertTrue(clicked)
    }
}
