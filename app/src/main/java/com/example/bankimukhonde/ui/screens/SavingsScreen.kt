package com.example.bankimukhonde.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
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
import com.example.bankimukhonde.data.money
import com.example.bankimukhonde.data.plain
import com.example.bankimukhonde.model.Member
import com.example.bankimukhonde.model.SavingsEntry
import com.example.bankimukhonde.ui.components.AppButton
import com.example.bankimukhonde.ui.components.AppScaffold
import com.example.bankimukhonde.ui.components.AppTextField
import com.example.bankimukhonde.ui.components.AvatarListItem
import com.example.bankimukhonde.ui.components.ConfirmDeleteDialog
import com.example.bankimukhonde.ui.components.EditDialog
import com.example.bankimukhonde.ui.components.RecordActionDialog
import com.example.bankimukhonde.ui.components.StatCard

@Composable
fun SavingsOverviewScreen(viewModel: AppViewModel, onBack: () -> Unit, onRecordContribution: () -> Unit) {
    var selected by remember { mutableStateOf<SavingsEntry?>(null) }
    var editing by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    fun closeDialogs() {
        editing = false
        deleting = false
        selected = null
    }

    selected?.let { entry ->
        val name = viewModel.members.find { it.id == entry.memberId }?.name ?: "Unknown"
        when {
            editing -> EditDialog(
                title = "Edit Contribution",
                labels = listOf("Amount", "Date"),
                initialValues = listOf(plain(entry.amount), entry.date),
                validate = { v ->
                    val amount = v[0].trim().toDoubleOrNull()
                    when {
                        amount == null -> "Enter the amount as a number"
                        amount <= 0.0 -> "Amount must be greater than zero"
                        v[1].isBlank() -> "Enter the date"
                        else -> null
                    }
                },
                onSave = { v ->
                    viewModel.updateSavings(entry, v[0].trim().toDouble(), v[1].trim())
                    closeDialogs()
                },
                onDismiss = { closeDialogs() }
            )
            deleting -> ConfirmDeleteDialog(
                title = "Delete contribution?",
                message = "${money(entry.amount)} from $name on ${entry.date} will be permanently deleted.",
                onConfirm = {
                    viewModel.deleteSavings(entry)
                    closeDialogs()
                },
                onDismiss = { closeDialogs() }
            )
            else -> RecordActionDialog(
                title = "Contribution",
                details = "Member: $name\nAmount: ${money(entry.amount)}\nDate: ${entry.date}",
                onEdit = { editing = true },
                onDelete = { deleting = true },
                onDismiss = { closeDialogs() }
            )
        }
    }

    AppScaffold(title = "Savings", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            StatCard(icon = Icons.Default.Savings, label = "Total Savings (Group)", value = money(viewModel.totalSavings()))
            AppButton(text = "Record Contribution") { onRecordContribution() }

            Text(text = "Recent Contributions", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
            if (viewModel.savingsEntries.isEmpty()) {
                Text("No contributions recorded yet.")
            }
            LazyColumn {
                items(viewModel.savingsEntries) { entry ->
                    val memberName = viewModel.members.find { it.id == entry.memberId }?.name ?: "Unknown"
                    AvatarListItem(
                        title = memberName,
                        subtitle = "${money(entry.amount)} — ${entry.date}",
                        onClick = { selected = entry }
                    )
                }
            }
        }
    }
}

@Composable
fun RecordSavingsScreen(viewModel: AppViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AppScaffold(title = "Record Savings", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedMember == null) {
                Text(text = "Select a member:", style = MaterialTheme.typography.titleMedium)
                if (viewModel.members.isEmpty()) {
                    Text("No members yet. Register a member first.")
                }
                LazyColumn {
                    items(viewModel.members) { member ->
                        AvatarListItem(title = member.name, subtitle = member.phone, onClick = { selectedMember = member })
                    }
                }
            } else {
                Text(text = "Member: ${selectedMember!!.name}", style = MaterialTheme.typography.titleMedium)
                AppTextField(value = amount, onValueChange = { amount = it }, label = "Amount")
                AppTextField(value = date, onValueChange = { date = it }, label = "Date (e.g. 2026-10-24)")

                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                }

                AppButton(text = "Save Contribution") {
                    val amountValue = amount.trim().toDoubleOrNull()
                    when {
                        amountValue == null -> errorMessage = "Enter the amount as a number"
                        amountValue <= 0.0 -> errorMessage = "Amount must be greater than zero"
                        date.isBlank() -> errorMessage = "Enter the date"
                        else -> {
                            errorMessage = ""
                            viewModel.recordSavings(selectedMember!!.id, amountValue, date.trim())
                            onDone()
                        }
                    }
                }
            }
        }
    }
}