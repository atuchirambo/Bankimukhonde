package com.example.bankimukhonde.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.data.money
import com.example.bankimukhonde.ui.components.AppScaffold
import com.example.bankimukhonde.ui.components.BalanceColor
import com.example.bankimukhonde.ui.components.HeroCard
import com.example.bankimukhonde.ui.components.MiniStat
import com.example.bankimukhonde.ui.components.QuickTile
import com.example.bankimukhonde.ui.components.SectionHeader

@Composable
fun DashboardScreen(viewModel: AppViewModel, onNavigate: (String) -> Unit) {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning,"
        hour < 18 -> "Good afternoon,"
        else -> "Good evening,"
    }
    val memberCount = viewModel.members.size
    val outstanding = viewModel.loans.sumOf { it.outstandingBalance }

    AppScaffold(title = "BANKIMUKHONDE") { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HeroCard(
                greeting = greeting,
                name = viewModel.adminUsername,
                label = "Total Savings",
                value = money(viewModel.totalSavings()),
                footer = if (memberCount == 1) "1 member" else "$memberCount members"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStat(
                    label = "Outstanding loans",
                    value = money(outstanding),
                    modifier = Modifier.weight(1f),
                    valueColor = BalanceColor
                )
                MiniStat(
                    label = "Repaid so far",
                    value = money(viewModel.totalRepayments()),
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }

            SectionHeader("Quick actions")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickTile(Icons.Default.Groups, "Members", { onNavigate("members") }, Modifier.weight(1f))
                QuickTile(Icons.Default.Savings, "Savings", { onNavigate("savings") }, Modifier.weight(1f))
                QuickTile(Icons.Default.Payments, "Loans", { onNavigate("loans") }, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickTile(Icons.Default.CalendarMonth, "Repayments", { onNavigate("repayments") }, Modifier.weight(1f))
                QuickTile(Icons.Default.Assessment, "Reports", { onNavigate("reports") }, Modifier.weight(1f))
                QuickTile(Icons.Default.Settings, "Settings", { onNavigate("settings") }, Modifier.weight(1f))
            }
        }
    }
}