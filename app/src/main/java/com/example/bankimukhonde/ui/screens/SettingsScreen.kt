package com.example.bankimukhonde.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bankimukhonde.ui.components.ActionListItem
import com.example.bankimukhonde.ui.components.AppScaffold
import androidx.compose.material3.Icon

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    AppScaffold(title = "Settings", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {

            Text(text = "Account", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
            ActionListItem(icon = Icons.Default.Person, title = "Administrator", subtitle = "Logged in as admin", onClick = {})
            ActionListItem(icon = Icons.Default.Lock, title = "Change Password", onClick = onChangePassword)

            Text(text = "Data", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
            ActionListItem(icon = Icons.Default.Save, title = "Backup Data", subtitle = "Save a copy to this device", onClick = {})
            ActionListItem(icon = Icons.Default.RestartAlt, title = "Restore Data", subtitle = "Load from a previous backup", onClick = {})

            Text(text = "About", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
            ActionListItem(icon = Icons.Default.Info, title = "App Version", subtitle = "BANKIMUKHONDE v1.0 (Offline)", onClick = {})

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Text(text = "  Log Out")
            }
        }
    }
}