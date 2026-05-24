package com.example.expensetracker.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.ui.screens.*
import com.example.expensetracker.ui.theme.*
import com.example.expensetracker.utils.NotificationHelper

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Analytics : Screen("analytics", "Insights", Icons.Default.Info)
    object Reports : Screen("reports", "Ledger", Icons.AutoMirrored.Filled.List)
    object Budget : Screen("budget", "Goals", Icons.Default.AccountBox)
}

@Composable
fun MainScreen(viewModel: ExpenseViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }
    
    val allExpenses by viewModel.allExpenses.collectAsState()
    val activeBudget by viewModel.activeBudget.collectAsState()
    
    // Track current route for dynamic button visibility
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isDashboard = currentRoute == Screen.Dashboard.route

    // Budget Alert Logic
    var lastAlertPercentage by remember { mutableStateOf(0) }
    
    LaunchedEffect(allExpenses, activeBudget) {
        activeBudget?.let { budget ->
            val currentSpending = viewModel.getCurrentPeriodExpenses(allExpenses, budget.period)
            val percentage = ((currentSpending / budget.amount) * 100).toInt()
            
            if (percentage >= 100 && lastAlertPercentage < 100) {
                notificationHelper.sendBudgetAlert(
                    "Budget Limit Reached",
                    "You have spent 100% of your ${budget.period.lowercase()} budget!"
                )
                lastAlertPercentage = 100
            } else if (percentage >= 90 && lastAlertPercentage < 90) {
                notificationHelper.sendBudgetAlert(
                    "Budget Warning",
                    "You have used 90% of your ${budget.period.lowercase()} budget."
                )
                lastAlertPercentage = 90
            } else if (percentage < 90) {
                lastAlertPercentage = 0
            }
        }
    }

    val leftItems = listOf(Screen.Dashboard, Screen.Analytics)
    val rightItems = listOf(Screen.Reports, Screen.Budget)

    Scaffold(
        containerColor = DeepNavy,
        bottomBar = {
            // Unified Elite Dock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Dock Background
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .shadow(32.dp, RoundedCornerShape(24.dp), spotColor = Color.Black),
                    shape = RoundedCornerShape(24.dp),
                    color = SlateBlue.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Side Tabs
                        leftItems.forEach { screen ->
                            DockTab(
                                screen = screen,
                                selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }

                        // Space for the center Add button (Dynamic width)
                        val spacerWidth by animateDpAsState(if (isDashboard) 72.dp else 0.dp, label = "spacer")
                        Spacer(modifier = Modifier.width(spacerWidth))

                        // Right Side Tabs
                        rightItems.forEach { screen ->
                            DockTab(
                                screen = screen,
                                selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }

                // Center Action: Elite Add Button (Context Aware)
                AnimatedVisibility(
                    visible = isDashboard,
                    enter = scaleIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeIn(),
                    exit = scaleOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-36).dp)
                            .size(68.dp)
                            .shadow(20.dp, CircleShape, spotColor = ElectricPurple)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(PremiumGradient))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(color = Color.White),
                                onClick = { navController.navigate("add_expense") }
                            )
                            .border(2.dp, GlassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = "Add", 
                            tint = Color.White, 
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                Screen.Dashboard.route,
                enterTransition = { fadeIn(animationSpec = tween(400)) + slideInHorizontally() },
                exitTransition = { fadeOut(animationSpec = tween(400)) }
            ) {
                DashboardScreen(
                    viewModel = viewModel,
                    onAddExpense = { navController.navigate("add_expense") },
                    onViewAll = { navController.navigate("expense_list") },
                    onEditExpense = { id -> navController.navigate("edit_expense/$id") }
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(viewModel = viewModel)
            }
            composable(Screen.Reports.route) {
                ReportsScreen(viewModel = viewModel)
            }
            composable(Screen.Budget.route) {
                BudgetScreen(viewModel = viewModel)
            }
            
            composable("add_expense") {
                AddEditExpenseScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("edit_expense/{expenseId}") { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getString("expenseId")?.toIntOrNull()
                AddEditExpenseScreen(
                    viewModel = viewModel,
                    expenseId = expenseId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("expense_list") {
                ExpenseListScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onEditExpense = { id -> navController.navigate("edit_expense/$id") }
                )
            }
        }
    }
}

@Composable
fun DockTab(screen: Screen, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false, color = ElectricPurple),
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            screen.icon, 
            contentDescription = null,
            tint = if (selected) ElectricPurple else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        AnimatedVisibility(visible = selected) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(ElectricPurple)
            )
        }
    }
}
