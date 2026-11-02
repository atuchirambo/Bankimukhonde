package com.example.bankimukhonde

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.ui.screens.MemberListScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MemberListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun memberListScreen_displaysHeader() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)

        composeTestRule.setContent {
            MemberListScreen(
                viewModel = viewModel,
                onBack = {},
                onAddMember = {},
                onSelectMember = {}
            )
        }

        composeTestRule.onNodeWithText("Members").assertIsDisplayed()
    }
}
