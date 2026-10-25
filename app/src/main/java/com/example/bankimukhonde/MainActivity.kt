package com.example.bankimukhonde

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bankimukhonde.data.AppViewModel
import com.example.bankimukhonde.ui.components.AppBottomBar
import com.example.bankimukhonde.ui.components.bottomNavItems
import com.example.bankimukhonde.ui.screens.AddMemberScreen
import com.example.bankimukhonde.ui.screens.DashboardScreen
import com.example.bankimukhonde.ui.screens.IssueLoanScreen
import com.example.bankimukhonde.ui.screens.LoansOverviewScreen
import com.example.bankimukhonde.ui.screens.LoginScreen
import com.example.bankimukhonde.ui.screens.MemberDetailScreen
import com.example.bankimukhonde.ui.screens.MemberListScreen
import com.example.bankimukhonde.ui.screens.RecordRepaymentScreen
import com.example.bankimukhonde.ui.screens.RecordSavingsScreen
import com.example.bankimukhonde.ui.screens.RepaymentsOverviewScreen
import com.example.bankimukhonde.ui.screens.ReportsScreen
import com.example.bankimukhonde.ui.screens.SavingsOverviewScreen
import com.example.bankimukhonde.ui.screens.SettingsScreen
import com.example.bankimukhonde.ui.screens.SetupAccountScreen
import com.example.bankimukhonde.ui.screens.WelcomeScreen
import com.example.bankimukhonde.ui.screens.ChangePasswordScreen
import androidx.compose.runtime.remember

private val BankimukhondeColors = lightColorScheme(
    primary = Color(0xFF1F6B3A),
    onPrimary = Color.White,
    secondary = Color(0xFF4C8C62),
    background = Color(0xFFFAFAFA),
    surface = Color.White
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = BankimukhondeColors) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BankimukhondeApp()
                }
            }
        }
    }
}

@Composable
fun BankimukhondeApp() {
    val navController: NavHostController = rememberNavController()
    val viewModel: AppViewModel = viewModel()
    // Wait until we know whether an administrator account exists
    if (!viewModel.adminLoaded) return

    // Which screen are we on right now? Used to decide whether to show the bottom bar.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    // First launch (no admin yet) -> setup. Otherwise -> welcome.
    val startDestination = remember { if (viewModel.isAdminSetUp) "welcome" else "setup" }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("dashboard") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
        ) {

            composable("setup") {
                SetupAccountScreen(
                    viewModel = viewModel,
                    onSetupComplete = {
                        navController.navigate("login") { popUpTo("setup") { inclusive = true } }
                    }
                )
            }

            composable("welcome") {
                WelcomeScreen(onGetStarted = { navController.navigate("login") })
            }

            composable("login") {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { navController.navigate("dashboard") }
                )
            }

            composable("dashboard") {
                DashboardScreen(viewModel = viewModel, onNavigate = { route -> navController.navigate(route) })
            }

            composable("members") {
                MemberListScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onAddMember = { navController.navigate("addMember") },
                    onSelectMember = { navController.navigate("memberDetail") }
                )
            }

            composable("addMember") {
                AddMemberScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() }
                )
            }

            composable("memberDetail") {
                MemberDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }

            composable("savings") {
                SavingsOverviewScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onRecordContribution = { navController.navigate("recordSavings") }
                )
            }

            composable("recordSavings") {
                RecordSavingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() }
                )
            }

            composable("loans") {
                LoansOverviewScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onIssueLoan = { navController.navigate("issueLoan") }
                )
            }

            composable("issueLoan") {
                IssueLoanScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() }
                )
            }

            composable("repayments") {
                RepaymentsOverviewScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onRecordRepayment = { navController.navigate("recordRepayment") }
                )
            }

            composable("recordRepayment") {
                RecordRepaymentScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() }
                )
            }

            composable("reports") {
                ReportsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onViewMembers = { navController.navigate("members") }
                )
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onChangePassword = { navController.navigate("changePassword") },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )
            }
            composable("changePassword") {
                ChangePasswordScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}