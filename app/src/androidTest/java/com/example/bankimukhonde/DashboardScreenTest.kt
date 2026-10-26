package com.example.bankimukhonde

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.ui.screens.DashboardScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboardScreen_displaysOverviewAndNavButtons() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        composeTestRule.setContent {
            DashboardScreen(viewModel = viewModel, onNavigate = {})
        }

        composeTestRule.onNodeWithText("Bankimukhonde Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Members").assertIsDisplayed()
        composeTestRule.onNodeWithText("Savings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Loans").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reports").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_navigatesOnButtonClick() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        var navigatedRoute = ""

        composeTestRule.setContent {
            DashboardScreen(viewModel = viewModel, onNavigate = { route -> navigatedRoute = route })
        }

        composeTestRule.onNodeWithText("Members").performClick()
        assertTrue(navigatedRoute == "members")
    }
}
