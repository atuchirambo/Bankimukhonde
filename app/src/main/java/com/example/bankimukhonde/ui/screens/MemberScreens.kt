package com.example.bankimukhonde.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.model.Member
import com.example.bankimukhonde.ui.components.AppButton
import com.example.bankimukhonde.ui.components.AppScaffold
import com.example.bankimukhonde.ui.components.AppTextField
import com.example.bankimukhonde.ui.components.AvatarListItem
import com.example.bankimukhonde.ui.components.StatCard

@Composable
fun MemberListScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onAddMember: () -> Unit,
    onSelectMember: (Member) -> Unit
) {
    AppScaffold(
        title = "Members",
        showBack = true,
        onBack = onBack,
        actions = { IconButton(onClick = { }) { Icon(Icons.Default.Search, contentDescription = "Search") } },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMember) { Icon(Icons.Default.Add, contentDescription = "Add member") }
        }
    ) { innerPadding: PaddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            items(viewModel.members) { member ->
                AvatarListItem(
                    title = member.name,
                    subtitle = member.phone,
                    onClick = {
                        viewModel.selectedMember = member
                        onSelectMember(member)
                    }
                )
            }
        }
    }
}

@Composable
fun AddMemberScreen(viewModel: AppViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dateJoined by remember { mutableStateOf("") }

    AppScaffold(title = "Register Member", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppTextField(value = name, onValueChange = { name = it }, label = "Full Name", leadingIcon = Icons.Default.Person)
            AppTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number", leadingIcon = Icons.Default.Phone)
            AppTextField(value = dateJoined, onValueChange = { dateJoined = it }, label = "Date Joined", leadingIcon = Icons.Default.CalendarMonth)

            AppButton(text = "Save") {
                if (name.isNotBlank()) {
                    viewModel.addMember(name, phone, dateJoined)
                    onDone()
                }
            }
        }
    }
}

@Composable
fun MemberDetailScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val member = viewModel.selectedMember ?: return
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Savings", "Loans", "Repayments")

    AppScaffold(title = "Member Details", showBack = true, onBack = onBack) { innerPadding: PaddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            AvatarListItem(title = member.name, subtitle = member.phone, onClick = { })

            TabRow(selectedTabIndex = tab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
                }
            }

            when (tab) {
                0 -> {
                    val entries = viewModel.savingsFor(member.id)
                    StatCard(icon = Icons.Default.Person, label = "Total Savings", value = "MK ${entries.sumOf { it.amount }}")
                    LazyColumn { items(entries) { e -> AvatarListItem(title = "MK ${e.amount}", subtitle = e.date, onClick = {}) } }
                }
                1 -> {
                    val loans = viewModel.loansFor(member.id)
                    LazyColumn { items(loans) { l -> AvatarListItem(title = "MK ${l.amount} (${l.interestRatePercent}%)", subtitle = "Balance: ${l.outstandingBalance}", onClick = {}) } }
                }
                2 -> {
                    val repayments = viewModel.repaymentsForMember(member.id)
                    LazyColumn { items(repayments) { r -> AvatarListItem(title = "MK ${r.amount}", subtitle = r.date, onClick = {}) } }
                }
            }
        }
    }
}