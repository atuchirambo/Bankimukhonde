package com.example.bankimukhonde.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.bankimukhonde.model.Loan
import com.example.bankimukhonde.model.Member
import com.example.bankimukhonde.ui.components.AppButton
import com.example.bankimukhonde.ui.components.AppScaffold
import com.example.bankimukhonde.ui.components.AppTextField
import com.example.bankimukhonde.ui.components.AvatarListItem
import com.example.bankimukhonde.ui.components.StatCard

@Composable
fun LoansOverviewScreen(viewModel: AppViewModel, onBack: () -> Unit, onIssueLoan: () -> Unit) {
    AppScaffold(title = "Loans", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            StatCard(icon = Icons.Default.Payments, label = "Total Loans Issued (Group)", value = "MK ${viewModel.totalLoansIssued()}")
            AppButton(text = "Issue Loan") { onIssueLoan() }

            Text(text = "Active Loans", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
            LazyColumn {
                items(viewModel.loans) { loan ->
                    val memberName = viewModel.members.find { it.id == loan.memberId }?.name ?: "Unknown"
                    AvatarListItem(
                        title = "$memberName — MK ${loan.amount} (${loan.interestRatePercent}%)",
                        subtitle = "Balance: MK ${loan.outstandingBalance}",
                        onClick = {}
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
    var date by remember { mutableStateOf("") }

    AppScaffold(title = "Issue Loan", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedMember == null) {
                Text(text = "Select a member:", style = MaterialTheme.typography.titleMedium)
                LazyColumn {
                    items(viewModel.members) { member ->
                        AvatarListItem(title = member.name, subtitle = member.phone, onClick = { selectedMember = member })
                    }
                }
            } else {
                Text(text = "Member: ${selectedMember!!.name}", style = MaterialTheme.typography.titleMedium)
                AppTextField(value = amount, onValueChange = { amount = it }, label = "Loan Amount")
                AppTextField(value = interestRate, onValueChange = { interestRate = it }, label = "Interest Rate (%)")
                AppTextField(value = date, onValueChange = { date = it }, label = "Date Issued", leadingIcon = Icons.Default.CalendarMonth)

                AppButton(text = "Issue Loan") {
                    val amountValue = amount.toDoubleOrNull()
                    val rateValue = interestRate.toDoubleOrNull()
                    if (amountValue != null && rateValue != null) {
                        viewModel.issueLoan(selectedMember!!.id, amountValue, rateValue, date)
                        onDone()
                    }
                }
            }
        }
    }
}

@Composable
fun RepaymentsOverviewScreen(viewModel: AppViewModel, onBack: () -> Unit, onRecordRepayment: () -> Unit) {
    AppScaffold(title = "Repayments", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            StatCard(icon = Icons.Default.CalendarMonth, label = "Total Repayments (Group)", value = "MK ${viewModel.totalRepayments()}")
            AppButton(text = "Record Repayment") { onRecordRepayment() }

            Text(text = "Recent Repayments", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
            LazyColumn {
                items(viewModel.repayments) { repayment ->
                    val loan = viewModel.loans.find { it.id == repayment.loanId }
                    val memberName = viewModel.members.find { it.id == loan?.memberId }?.name ?: "Unknown"
                    AvatarListItem(title = memberName, subtitle = "MK ${repayment.amount} — ${repayment.date}", onClick = {})
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

    AppScaffold(title = "Record Repayment", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedLoan == null) {
                Text(text = "Select a loan:", style = MaterialTheme.typography.titleMedium)
                LazyColumn {
                    items(viewModel.loans) { loan ->
                        val memberName = viewModel.members.find { it.id == loan.memberId }?.name ?: "Unknown"
                        AvatarListItem(title = memberName, subtitle = "Balance: MK ${loan.outstandingBalance}", onClick = { selectedLoan = loan })
                    }
                }
            } else {
                Text(text = "Outstanding balance: MK ${selectedLoan!!.outstandingBalance}", style = MaterialTheme.typography.titleMedium)
                AppTextField(value = amount, onValueChange = { amount = it }, label = "Repayment Amount")
                AppTextField(value = date, onValueChange = { date = it }, label = "Date")

                AppButton(text = "Save Repayment") {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null) {
                        viewModel.recordRepayment(selectedLoan!!.id, amountValue, date)
                        onDone()
                    }
                }
            }
        }
    }
}