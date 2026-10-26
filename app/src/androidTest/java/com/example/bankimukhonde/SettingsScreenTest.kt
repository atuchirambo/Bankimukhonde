package com.example.bankimukhonde

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.ui.screens.SettingsScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_displaysOptions() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onBack = {},
                onChangePassword = {},
                onLogout = {}
            )
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Change Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Logout").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_clicksChangePassword() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        var clicked = false

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onBack = {},
                onChangePassword = { clicked = true },
                onLogout = {}
            )
        }

        composeTestRule.onNodeWithText("Change Password").performClick()
        assertTrue(clicked)
    }
}
