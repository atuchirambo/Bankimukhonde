package com.example.bankimukhonde.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.ui.components.AppButton
import com.example.bankimukhonde.ui.components.AppPasswordField
import com.example.bankimukhonde.ui.components.AppScaffold

@Composable
fun ChangePasswordScreen(viewModel: AppViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AppScaffold(title = "Change Password", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppPasswordField(value = currentPassword, onValueChange = { currentPassword = it }, label = "Current Password")
            AppPasswordField(value = newPassword, onValueChange = { newPassword = it }, label = "New Password")
            AppPasswordField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirm New Password")

            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }

            AppButton(text = "Update Password") {
                when {
                    newPassword.length < 4 -> errorMessage = "New password must be at least 4 characters"
                    newPassword != confirmPassword -> errorMessage = "New passwords do not match"
                    !viewModel.changePassword(currentPassword, newPassword) -> errorMessage = "Current password is incorrect"
                    else -> {
                        errorMessage = ""
                        onDone()
                    }
                }
            }
        }
    }
}