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
import com.example.bankimukhonde.ui.screens.AddMemberScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddMemberScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addMemberScreen_displaysInputFields() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        composeTestRule.setContent {
            AddMemberScreen(
                viewModel = viewModel,
                onBack = {},
                onDone = {}
            )
        }

        composeTestRule.onNodeWithText("Register Member").assertIsDisplayed()
        composeTestRule.onNodeWithText("Full Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phone Number").assertIsDisplayed()
        composeTestRule.onNodeWithText("Date Joined").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
    }

    @Test
    fun addMemberScreen_savesMember_whenSaveClicked() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        var doneCalled = false

        composeTestRule.setContent {
            AddMemberScreen(
                viewModel = viewModel,
                onBack = {},
                onDone = { doneCalled = true }
            )
        }

        composeTestRule.onNodeWithText("Full Name").performTextInput("Limbani Phiri")
        composeTestRule.onNodeWithText("Phone Number").performTextInput("0999555666")
        composeTestRule.onNodeWithText("Date Joined").performTextInput("2026-03-01")

        composeTestRule.onNodeWithText("Save").performClick()

        assertTrue(doneCalled)
    }
}