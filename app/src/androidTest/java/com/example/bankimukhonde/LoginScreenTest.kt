package com.example.bankimukhonde

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.ui.screens.LoginScreen
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysHeadingAndInputs() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        composeTestRule.setContent {
            LoginScreen(viewModel = viewModel, onLoginSuccess = {})
        }

        composeTestRule.onNodeWithText("BANKIMUKHONDE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Administrator Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Username").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsErrorMessage_onFailedLogin() = runBlocking {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)
        viewModel.setupAdmin("admin", "correctpass")

        composeTestRule.setContent {
            LoginScreen(viewModel = viewModel, onLoginSuccess = {})
        }

        composeTestRule.onNodeWithText("Username").performTextInput("admin")
        composeTestRule.onNodeWithText("Password").performTextInput("wrongpass")
        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.onNodeWithText("Incorrect username or password").assertIsDisplayed()
    }

    @Test
    fun loginScreen_navigates_onSuccessfulLogin() = runBlocking {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)
        viewModel.setupAdmin("admin", "password123")

        var loggedIn = false

        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { loggedIn = true }
            )
        }

        composeTestRule.onNodeWithText("Username").performTextInput("admin")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Login").performClick()

        assertTrue(loggedIn)
    }
}