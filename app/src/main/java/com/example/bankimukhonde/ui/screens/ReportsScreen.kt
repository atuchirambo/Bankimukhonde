package com.example.bankimukhonde.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.ui.components.AppScaffold
import com.example.bankimukhonde.ui.components.AvatarListItem
import com.example.bankimukhonde.ui.components.StatCard

@Composable
fun ReportsScreen(viewModel: AppViewModel, onBack: () -> Unit, onViewMembers: () -> Unit) {
    AppScaffold(title = "Reports", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AvatarListItem(
                title = "Individual Member Statement",
                subtitle = "View or export a member's financial statement",
                onClick = onViewMembers
            )

            Text(text = "Group Summary Report", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            StatCard(icon = Icons.Default.Assessment, label = "Total Savings", value = "MK ${viewModel.totalSavings()}")
            StatCard(icon = Icons.Default.Assessment, label = "Total Loans Issued", value = "MK ${viewModel.totalLoansIssued()}")
            StatCard(icon = Icons.Default.Assessment, label = "Total Repayments", value = "MK ${viewModel.totalRepayments()}")

            OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text("Export / Backup Data")
            }
        }
    }
}