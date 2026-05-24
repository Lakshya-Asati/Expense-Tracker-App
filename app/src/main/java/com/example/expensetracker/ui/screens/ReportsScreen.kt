package com.example.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.Expense
import com.example.expensetracker.ui.ExpenseViewModel
import com.example.expensetracker.ui.components.ModernExpenseItem
import com.example.expensetracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ExpenseViewModel) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    
    // Group by Month
    val monthlyReports = allExpenses.groupBy { 
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(it.date))
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("FINANCIAL LEDGER", fontWeight = FontWeight.Black, letterSpacing = 2.sp, style = MaterialTheme.typography.titleMedium) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                monthlyReports.forEach { (month, expenses) ->
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth().border(1.dp, GlassBorder, RoundedCornerShape(28.dp)),
                            shape = RoundedCornerShape(28.dp),
                            color = ElectricPurple.copy(alpha = 0.05f)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(month, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                    Text("${expenses.size} Records", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                                }
                                Text(
                                    "₹${String.format("%.0f", expenses.sumOf { it.amount })}", 
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = ElectricPurple
                                )
                            }
                        }
                    }
                    itemsIndexed(expenses.take(5)) { index, expense ->
                        ModernExpenseItem(expense = expense, index = index, onClick = {})
                    }
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = GlassBorder)
                    }
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}
