package com.example.bankimukhonde.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.ui.components.AppButton
import com.example.bankimukhonde.ui.components.AppPasswordField
import com.example.bankimukhonde.ui.components.AppTextField

/**
 * Shown only the first time the app is opened on a device, when no
 * administrator account exists yet. The chairperson creates the
 * username and password that will be used to log in from then on.
 */
@Composable
fun SetupAccountScreen(viewModel: AppViewModel, onSetupComplete: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Groups,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Text(
            text = "Set Up Administrator Account",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "This is a one-time setup. Choose the username and password you will use to log in.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        AppTextField(value = username, onValueChange = { username = it }, label = "Username", leadingIcon = Icons.Default.Person)
        AppPasswordField(value = password, onValueChange = { password = it }, label = "Password")
        AppPasswordField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirm Password")

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        AppButton(text = "Create Account") {
            when {
                username.isBlank() -> errorMessage = "Please enter a username"
                password.length < 4 -> errorMessage = "Password must be at least 4 characters"
                password != confirmPassword -> errorMessage = "Passwords do not match"
                else -> {
                    errorMessage = ""
                    viewModel.setupAdmin(username.trim(), password)
                    onSetupComplete()
                }
            }
        }
    }
}

