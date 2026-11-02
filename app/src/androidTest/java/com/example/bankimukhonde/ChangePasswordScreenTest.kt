package com.example.bankimukhonde

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.ui.screens.ChangePasswordScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChangePasswordScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun changePasswordScreen_displaysFields() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        composeTestRule.setContent {
            ChangePasswordScreen(
                viewModel = viewModel,
                onBack = {},
                onDone = {}
            )
        }

        composeTestRule.onNodeWithText("Change Admin Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Current Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("New Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Update Password").assertIsDisplayed()
    }
}
