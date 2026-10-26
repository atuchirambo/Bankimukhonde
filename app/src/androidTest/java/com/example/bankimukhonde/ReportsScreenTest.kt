package com.example.bankimukhonde

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.ui.screens.ReportsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun reportsScreen_displaysHeading() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        composeTestRule.setContent {
            ReportsScreen(
                viewModel = viewModel,
                onBack = {},
                onViewMembers = {}
            )
        }

        composeTestRule.onNodeWithText("Financial Reports").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total Savings").assertIsDisplayed()
    }
}
