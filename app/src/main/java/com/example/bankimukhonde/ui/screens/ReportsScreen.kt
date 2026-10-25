package com.example.bankimukhonde.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.example.bankimukhonde.ui.components.AppScaffold
import com.example.bankimukhonde.ui.components.AvatarListItem
import com.example.bankimukhonde.ui.components.StatCard
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(viewModel: AppViewModel, onBack: () -> Unit, onViewMembers: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val csv = BackupManager.buildCsv(
                    viewModel.members, viewModel.savingsEntries, viewModel.loans, viewModel.repayments
                )
                message = if (BackupManager.writeText(context, uri, csv)) "Report exported successfully."
                else "Export failed. Please try again."
            }
        }
    }

    AppScaffold(title = "Reports", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AvatarListItem(
                title = "Individual Member Statement",
                subtitle = "Open a member to see their statement",
                onClick = onViewMembers
            )

            Text(text = "Group Summary Report", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            StatCard(icon = Icons.Default.Assessment, label = "Total Savings", value = "MK ${viewModel.totalSavings()}")
            StatCard(icon = Icons.Default.Assessment, label = "Total Loans Issued", value = "MK ${viewModel.totalLoansIssued()}")
            StatCard(icon = Icons.Default.Assessment, label = "Total Repayments", value = "MK ${viewModel.totalRepayments()}")

            OutlinedButton(
                onClick = {
                    val stamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    exportLauncher.launch("bankimukhonde_report_$stamp.csv")
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Export Report (CSV)")
            }

            if (message.isNotEmpty()) {
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}