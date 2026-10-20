package com.example.bankimukhonde.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/** One tab in the bottom bar: where it goes, what it says, what icon it shows. */
data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

/** The routes that show the bottom bar. Other screens (forms, details) hide it. */
val bottomNavItems = listOf(
    BottomNavItem("dashboard", "Home", Icons.Default.Home),
    BottomNavItem("members", "Members", Icons.Default.Groups),
    BottomNavItem("savings", "Savings", Icons.Default.Savings),
    BottomNavItem("loans", "Loans", Icons.Default.Payments),
    BottomNavItem("reports", "Reports", Icons.Default.Assessment)
)

@Composable
fun AppBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}