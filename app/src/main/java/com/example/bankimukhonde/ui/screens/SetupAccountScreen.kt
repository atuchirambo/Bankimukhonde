package com.example.bankimukhonde.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FilterChip
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

@Composable
fun SetupAccountScreen(viewModel: AppViewModel, onSetupComplete: () -> Unit) {
    var isCreatingGroup by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Chairperson") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Groups,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
        }

        Text(
            text = "Create or Join VSL Group",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "Set up your administrator credentials for group management.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = isCreatingGroup,
                onClick = {
                    isCreatingGroup = true
                    role = "Chairperson"
                },
                label = { Text("Create Group (Chairperson)") }
            )
            FilterChip(
                selected = !isCreatingGroup,
                onClick = {
                    isCreatingGroup = false
                    role = "Secretary"
                },
                label = { Text("Join Group (Invite Code)") }
            )
        }

        if (isCreatingGroup) {
            AppTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = "Group Name (e.g. Chimwemwe VSL)",
                leadingIcon = Icons.Default.Groups
            )
        } else {
            AppTextField(
                value = inviteCode,
                onValueChange = { inviteCode = it.uppercase() },
                label = "6-Character Invite Code",
                leadingIcon = Icons.Default.Key
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = role == "Secretary",
                    onClick = { role = "Secretary" },
                    label = { Text("Secretary") }
                )
                FilterChip(
                    selected = role == "Treasurer",
                    onClick = { role = "Treasurer" },
                    label = { Text("Treasurer") }
                )
            }
        }

        AppTextField(value = username, onValueChange = { username = it }, label = "Username", leadingIcon = Icons.Default.Person)
        AppTextField(value = email, onValueChange = { email = it }, label = "Email (Optional)", leadingIcon = Icons.Default.Email)
        AppPasswordField(value = password, onValueChange = { password = it }, label = "Password")
        AppPasswordField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirm Password")

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        AppButton(text = if (isCreatingGroup) "Create Group & Account" else "Join Group & Account") {
            when {
                isCreatingGroup && groupName.isBlank() -> errorMessage = "Please enter a group name"
                !isCreatingGroup && inviteCode.isBlank() -> errorMessage = "Please enter an invite code"
                username.isBlank() -> errorMessage = "Please enter a username"
                password.length < 4 -> errorMessage = "Password must be at least 4 characters"
                password != confirmPassword -> errorMessage = "Passwords do not match"
                else -> {
                    errorMessage = ""
                    viewModel.setupAdminGroup(
                        username = username.trim(),
                        email = email.trim(),
                        password = password,
                        role = role,
                        groupName = groupName.trim(),
                        inviteCode = inviteCode.trim().uppercase(),
                        isCreatingGroup = isCreatingGroup,
                        onResult = { success, msg ->
                            if (success) {
                                onSetupComplete()
                            } else {
                                errorMessage = msg
                            }
                        }
                    )
                    onSetupComplete()
                }
            }
        }
    }
}
