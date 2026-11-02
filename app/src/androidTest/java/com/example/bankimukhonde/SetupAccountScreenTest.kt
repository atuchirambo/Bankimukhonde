package com.example.bankimukhonde

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.ui.screens.SetupAccountScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SetupAccountScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun setupAccountScreen_displaysFields() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        composeTestRule.setContent {
            SetupAccountScreen(
                viewModel = viewModel,
                onSetupComplete = {}
            )
        }

        composeTestRule.onNodeWithText("BANKIMUKHONDE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Administrator Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Username").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Setup Account").assertIsDisplayed()
    }
}
