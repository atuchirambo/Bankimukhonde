package com.example.bankimukhonde.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Payments
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
import com.example.bankimukhonde.model.Loan
import com.example.bankimukhonde.model.Member
import com.example.bankimukhonde.model.Repayment
import com.example.bankimukhonde.model.monthlyInstalment
import com.example.bankimukhonde.ui.components.AppButton
import com.example.bankimukhonde.ui.components.AppScaffold
import com.example.bankimukhonde.ui.components.AppTextField
import com.example.bankimukhonde.ui.components.AvatarListItem
import com.example.bankimukhonde.ui.components.ConfirmDeleteDialog
import com.example.bankimukhonde.ui.components.EditDialog
import com.example.bankimukhonde.ui.components.RecordActionDialog
import com.example.bankimukhonde.ui.components.StatCard

@Composable
fun LoansOverviewScreen(viewModel: AppViewModel, onBack: () -> Unit, onIssueLoan: () -> Unit) {
    var selected by remember { mutableStateOf<Loan?>(null) }
    var editing by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    fun closeDialogs() {
        editing = false
        deleting = false
        selected = null
    }

    selected?.let { loan ->
        val name = viewModel.members.find { it.id == loan.memberId }?.name ?: "Unknown"
        val loanRepayments = viewModel.repaymentsFor(loan.id)
        val alreadyRepaid = loanRepayments.sumOf { it.amount }
        when {
            editing -> EditDialog(
                title = "Edit Loan",
                labels = listOf("Loan Amount", "Interest Rate (%)", "Date Issued", "Repayment Term (months, 0 = not set)"),
                initialValues = listOf(plain(loan.amount), plain(loan.interestRatePercent), loan.dateIssued, loan.termMonths.toString()),
                validate = { v ->
                    val amount = v[0].trim().toDoubleOrNull()
                    val rate = v[1].trim().toDoubleOrNull()
                    val term = v[3].trim().toIntOrNull()
                    when {
                        amount == null -> "Enter the loan amount as a number"
                        amount <= 0.0 -> "Loan amount must be greater than zero"
                        rate == null -> "Enter the interest rate as a number"
                        rate < 0.0 -> "Interest rate cannot be negative"
                        v[2].isBlank() -> "Enter the date"
                        term == null || term < 0 || term > 120 -> "Term must be a whole number from 0 to 120"
                        amount + amount * rate / 100 < alreadyRepaid - 0.001 ->
                            "The new total is less than the ${money(alreadyRepaid)} already repaid"
                        else -> null
                    }
                },
                onSave = { v ->
                    viewModel.updateLoan(loan, v[0].trim().toDouble(), v[1].trim().toDouble(), v[2].trim(), v[3].trim().toInt())
                    closeDialogs()
                },
                onDismiss = { closeDialogs() }
            )
            deleting -> ConfirmDeleteDialog(
                title = "Delete loan?",
                message = "The ${money(loan.amount)} loan to $name will be permanently deleted" +
                        if (loanRepayments.isNotEmpty()) ", together with its ${loanRepayments.size} repayment(s)." else ".",
                onConfirm = {
                    viewModel.deleteLoan(loan)
                    closeDialogs()
                },
                onDismiss = { closeDialogs() }
            )
            else -> RecordActionDialog(
                title = "Loan",
                details = "Member: $name\nAmount: ${money(loan.amount)}\nInterest: ${plain(loan.interestRatePercent)}%\n" +
                        "Date issued: ${loan.dateIssued}\n" +
                        (if (loan.termMonths > 0) "Term: ${loan.termMonths} months\nMonthly instalment: ${money(loan.monthlyInstalment() ?: 0.0)}\n"
                        else "Term: not set\n") +
                        "Repaid so far: ${money(alreadyRepaid)}\nBalance: ${money(loan.outstandingBalance)}",
                onEdit = { editing = true },
                onDelete = { deleting = true },
                onDismiss = { closeDialogs() }
            )
        }
    }

    AppScaffold(title = "Loans", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            StatCard(icon = Icons.Default.Payments, label = "Total Loans Issued (Group)", value = money(viewModel.totalLoansIssued()))
            AppButton(text = "Issue Loan") { onIssueLoan() }

            Text(text = "Loans", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
            if (viewModel.loans.isEmpty()) {
                Text("No loans issued yet.")
            }
            LazyColumn {
                items(viewModel.loans) { loan ->
                    val memberName = viewModel.members.find { it.id == loan.memberId }?.name ?: "Unknown"
                    AvatarListItem(
                        title = "$memberName — ${money(loan.amount)} (${plain(loan.interestRatePercent)}%)",
                        subtitle = if (loan.outstandingBalance <= 0.0) "Fully paid" else "Balance: ${money(loan.outstandingBalance)}",
                        onClick = { selected = loan }
                    )
                }
            }
        }
    }
}

@Composable
fun IssueLoanScreen(viewModel: AppViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var amount by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var termMonths by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AppScaffold(title = "Issue Loan", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedMember == null) {
                Text(text = "Select a member:", style = MaterialTheme.typography.titleMedium)
                if (viewModel.members.isEmpty()) {
                    Text("No members yet. Register a member first.")
                }
                viewModel.members.forEach { member ->
                    AvatarListItem(title = member.name, subtitle = member.phone, onClick = { selectedMember = member })
                }
            } else {
                Text(text = "Member: ${selectedMember!!.name}", style = MaterialTheme.typography.titleMedium)
                AppTextField(value = amount, onValueChange = { amount = it }, label = "Loan Amount")
                AppTextField(value = interestRate, onValueChange = { interestRate = it }, label = "Interest Rate (%)")
                AppTextField(value = termMonths, onValueChange = { termMonths = it }, label = "Repayment Term (months)")
                AppTextField(value = date, onValueChange = { date = it }, label = "Date Issued (e.g. 2026-10-24)", leadingIcon = Icons.Default.CalendarMonth)

                val previewAmount = amount.trim().toDoubleOrNull()
                val previewRate = interestRate.trim().toDoubleOrNull()
                val previewTerm = termMonths.trim().toIntOrNull()
                if (previewAmount != null && previewRate != null && previewTerm != null && previewAmount > 0 && previewTerm > 0) {
                    val total = previewAmount + previewAmount * previewRate / 100
                    Text("Total to repay: ${money(total)}\nMonthly instalment: ${money(total / previewTerm)}")
                }

                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                }

                AppButton(text = "Issue Loan") {
                    val amountValue = amount.trim().toDoubleOrNull()
                    val rateValue = interestRate.trim().toDoubleOrNull()
                    val termValue = termMonths.trim().toIntOrNull()
                    when {
                        amountValue == null -> errorMessage = "Enter the loan amount as a number"
                        amountValue <= 0.0 -> errorMessage = "Loan amount must be greater than zero"
                        rateValue == null -> errorMessage = "Enter the interest rate as a number (0 if none)"
                        rateValue < 0.0 -> errorMessage = "Interest rate cannot be negative"
                        termValue == null -> errorMessage = "Enter the term as a whole number of months"
                        termValue < 1 || termValue > 120 -> errorMessage = "Term must be between 1 and 120 months"
                        date.isBlank() -> errorMessage = "Enter the date"
                        else -> {
                            errorMessage = ""
                            viewModel.issueLoan(selectedMember!!.id, amountValue, rateValue, date.trim(), termValue)
                            onDone()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RepaymentsOverviewScreen(viewModel: AppViewModel, onBack: () -> Unit, onRecordRepayment: () -> Unit) {
    var selected by remember { mutableStateOf<Repayment?>(null) }
    var deleting by remember { mutableStateOf(false) }

    fun closeDialogs() {
        deleting = false
        selected = null
    }

    selected?.let { repayment ->
        val loan = viewModel.loans.find { it.id == repayment.loanId }
        val name = viewModel.members.find { it.id == loan?.memberId }?.name ?: "Unknown"
        if (deleting) {
            ConfirmDeleteDialog(
                title = "Delete repayment?",
                message = "The ${money(repayment.amount)} repayment from $name will be deleted, " +
                        "and the loan balance will go back up by that amount.",
                onConfirm = {
                    viewModel.deleteRepayment(repayment)
                    closeDialogs()
                },
                onDismiss = { closeDialogs() }
            )
        } else {
            RecordActionDialog(
                title = "Repayment",
                details = "Member: $name\nAmount: ${money(repayment.amount)}\nDate: ${repayment.date}\n\n" +
                        "To correct a mistake, delete this repayment and record it again.",
                onEdit = null,
                onDelete = { deleting = true },
                onDismiss = { closeDialogs() }
            )
        }
    }

    AppScaffold(title = "Repayments", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            StatCard(icon = Icons.Default.CalendarMonth, label = "Total Repayments (Group)", value = money(viewModel.totalRepayments()))
            AppButton(text = "Record Repayment") { onRecordRepayment() }

            Text(text = "Recent Repayments", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
            if (viewModel.repayments.isEmpty()) {
                Text("No repayments recorded yet.")
            }
            LazyColumn {
                items(viewModel.repayments) { repayment ->
                    val loan = viewModel.loans.find { it.id == repayment.loanId }
                    val memberName = viewModel.members.find { it.id == loan?.memberId }?.name ?: "Unknown"
                    AvatarListItem(
                        title = memberName,
                        subtitle = "${money(repayment.amount)} — ${repayment.date}",
                        onClick = { selected = repayment }
                    )
                }
            }
        }
    }
}

@Composable
fun RecordRepaymentScreen(viewModel: AppViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    var selectedLoan by remember { mutableStateOf<Loan?>(null) }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AppScaffold(title = "Record Repayment", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedLoan == null) {
                val unpaidLoans = viewModel.loans.filter { it.outstandingBalance > 0.0 }
                Text(text = "Select a loan:", style = MaterialTheme.typography.titleMedium)
                if (unpaidLoans.isEmpty()) {
                    Text("There are no loans with an outstanding balance.")
                }
                LazyColumn {
                    items(unpaidLoans) { loan ->
                        val memberName = viewModel.members.find { it.id == loan.memberId }?.name ?: "Unknown"
                        AvatarListItem(title = memberName, subtitle = "Balance: ${money(loan.outstandingBalance)}", onClick = { selectedLoan = loan })
                    }
                }
            } else {
                val loan = selectedLoan!!
                Text(text = "Outstanding balance: ${money(loan.outstandingBalance)}", style = MaterialTheme.typography.titleMedium)
                AppTextField(value = amount, onValueChange = { amount = it }, label = "Repayment Amount")
                AppTextField(value = date, onValueChange = { date = it }, label = "Date (e.g. 2026-10-24)")

                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                }

                AppButton(text = "Save Repayment") {
                    val amountValue = amount.trim().toDoubleOrNull()
                    when {
                        amountValue == null -> errorMessage = "Enter the amount as a number"
                        amountValue <= 0.0 -> errorMessage = "Amount must be greater than zero"
                        amountValue > loan.outstandingBalance + 0.001 ->
                            errorMessage = "Amount is more than the outstanding balance (${money(loan.outstandingBalance)})"
                        date.isBlank() -> errorMessage = "Enter the date"
                        else -> {
                            errorMessage = ""
                            viewModel.recordRepayment(loan.id, amountValue, date.trim())
                            onDone()
                        }
                    }
                }
            }
        }
    }
}