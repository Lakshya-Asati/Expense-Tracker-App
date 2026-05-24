package com.example.expensetracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.ui.ExpenseViewModel
import com.example.expensetracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(viewModel: ExpenseViewModel) {
    val activeBudget by viewModel.activeBudget.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    
    var amount by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("Monthly") }
    val periods = listOf("Daily", "Weekly", "Monthly")
    var expanded by remember { mutableStateOf(false) }

    val currentSpending = if (activeBudget != null) {
        viewModel.getCurrentPeriodExpenses(allExpenses, activeBudget!!.period)
    } else 0.0

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("COMMAND CENTER", fontWeight = FontWeight.Black, letterSpacing = 2.sp, style = MaterialTheme.typography.titleMedium) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                // Circular Goal Visualization
                if (activeBudget != null) {
                    item {
                        val progress = (currentSpending / activeBudget!!.amount).coerceIn(0.0, 1.0).toFloat()
                        val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(2000, easing = FastOutSlowInEasing), label = "ring")
                        
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(280.dp)) {
                            // Outer Glow
                            Box(modifier = Modifier.size(240.dp).background(Brush.radialGradient(listOf(ElectricPurple.copy(alpha = 0.1f), Color.Transparent))))
                            
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.size(220.dp),
                                color = GlassWhite,
                                strokeWidth = 16.dp,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(220.dp),
                                color = if (progress > 0.9f) ErrorRose else ElectricPurple,
                                strokeWidth = 16.dp,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = TextPrimary)
                                Text("LIMIT REACHED", style = MaterialTheme.typography.labelSmall, color = TextSecondary, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(color = GlassWhite, shape = RoundedCornerShape(12.dp)) {
                                    Text(
                                        text = activeBudget!!.period.uppercase(),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ElectricPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            BudgetStatElite("Utilized", "₹${String.format("%.0f", currentSpending)}", SuccessEmerald)
                            BudgetStatElite("Threshold", "₹${String.format("%.0f", activeBudget!!.amount)}", ElectricPurple)
                            BudgetStatElite("Available", "₹${String.format("%.0f", (activeBudget!!.amount - currentSpending).coerceAtLeast(0.0))}", CyanGlow)
                        }
                    }
                }

                // Configuration Section
                item {
                    Column {
                        SectionHeader(title = "Define New Protocol")
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        PremiumTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = "Deployment Capital (₹)",
                            placeholder = "0.00"
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PremiumTextField(
                                value = selectedPeriod,
                                onValueChange = {},
                                label = "Reset Cycle",
                                isReadOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(SlateBlue)
                            ) {
                                periods.forEach { period ->
                                    DropdownMenuItem(
                                        text = { Text(period, color = TextPrimary) },
                                        onClick = {
                                            selectedPeriod = period
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = {
                                val budgetAmount = amount.toDoubleOrNull() ?: 0.0
                                if (budgetAmount > 0) {
                                    viewModel.setBudget(budgetAmount, selectedPeriod)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                        ) {
                            Text("INITIALIZE PROTOCOL", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun BudgetStatElite(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = TextPrimary)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary, letterSpacing = 1.sp)
    }
}
