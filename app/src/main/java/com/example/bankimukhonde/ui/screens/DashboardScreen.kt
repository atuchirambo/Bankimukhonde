package com.example.bankimukhonde.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.ui.components.AppScaffold
import com.example.bankimukhonde.ui.components.DashboardMenuItem
import com.example.bankimukhonde.ui.components.StatCard
import com.example.bankimukhonde.data.money

@Composable
fun DashboardScreen(viewModel: AppViewModel, onNavigate: (String) -> Unit) {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning,"
        hour < 18 -> "Good afternoon,"
        else -> "Good evening,"
    }
    AppScaffold(title = "BANKIMUKHONDE") { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = greeting, style = MaterialTheme.typography.bodyMedium)
            Text(text = viewModel.adminUsername, style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    DashboardMenuItem(Icons.Default.Groups, "Members") { onNavigate("members") }
                }
                Column(modifier = Modifier.weight(1f)) {
                    DashboardMenuItem(Icons.Default.Savings, "Savings") { onNavigate("savings") }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    DashboardMenuItem(Icons.Default.Payments, "Loans") { onNavigate("loans") }
                }
                Column(modifier = Modifier.weight(1f)) {
                    DashboardMenuItem(Icons.Default.CalendarMonth, "Repayments") { onNavigate("repayments") }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    DashboardMenuItem(Icons.Default.Assessment, "Reports") { onNavigate("reports") }
                }
                Column(modifier = Modifier.weight(1f)) {
                    DashboardMenuItem(Icons.Default.Settings, "Settings") {  onNavigate("settings")}
                }
            }

            StatCard(icon = Icons.Default.Savings, label = "Total Savings", value = money(viewModel.totalSavings()))
        }
    }
}