package com.example.bankimukhonde.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.data.BackupManager
import com.example.bankimukhonde.ui.components.ActionListItem
import com.example.bankimukhonde.ui.components.AppScaffold
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                message = if (BackupManager.backup(context, uri)) "Backup saved successfully."
                else "Backup failed. Please try again."
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) pendingRestoreUri = uri
    }

    pendingRestoreUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            title = { Text("Restore data?") },
            text = {
                Text(
                    "This replaces ALL records on this phone with the ones in the backup file. " +
                            "This cannot be undone, and the app will restart."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingRestoreUri = null
                    scope.launch {
                        if (BackupManager.restore(context, uri)) {
                            BackupManager.restartApp(context)
                        } else {
                            message = "Restore failed. That file is not a valid BANKIMUKHONDE backup."
                        }
                    }
                }) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreUri = null }) { Text("Cancel") }
            }
        )
    }

    AppScaffold(title = "Settings", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            Text(text = "Account", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
            ActionListItem(icon = Icons.Default.Person, title = "Administrator", subtitle = "Logged in as ${viewModel.adminUsername}", onClick = {})
            ActionListItem(icon = Icons.Default.Lock, title = "Change Password", onClick = onChangePassword)

            Text(text = "Data", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
            ActionListItem(
                icon = Icons.Default.Save,
                title = "Backup Data",
                subtitle = "Save a copy of all records to a file",
                onClick = {
                    val stamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    backupLauncher.launch("bankimukhonde_backup_$stamp.db")
                }
            )
            ActionListItem(
                icon = Icons.Default.RestartAlt,
                title = "Restore Data",
                subtitle = "Load records from a backup file",
                onClick = { restoreLauncher.launch(arrayOf("*/*")) }
            )
            if (message.isNotEmpty()) {
                Text(text = message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            }

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