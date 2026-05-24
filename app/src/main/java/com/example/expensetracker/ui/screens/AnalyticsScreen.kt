package com.example.expensetracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.ui.ExpenseViewModel
import com.example.expensetracker.ui.theme.*
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.entry.entryModelOf

import com.example.expensetracker.utils.CategoryUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: ExpenseViewModel) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    
    val categorySpending = allExpenses.groupBy { it.category }
        .mapValues { it.value.sumOf { exp -> exp.amount }.toFloat() }
    
    val totalAmount = categorySpending.values.sum()
    val chartEntries = categorySpending.values.toTypedArray()
    val chartModel = entryModelOf(*chartEntries)

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("FINANCIAL INSIGHTS", fontWeight = FontWeight.Black, letterSpacing = 2.sp, style = MaterialTheme.typography.titleMedium) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Elite Distribution Chart
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().border(1.dp, GlassBorder, RoundedCornerShape(36.dp)),
                        shape = RoundedCornerShape(36.dp),
                        color = SlateBlue.copy(alpha = 0.4f)
                    ) {
                        Column(modifier = Modifier.padding(28.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(ElectricPurple))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Asset Allocation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            if (chartEntries.isNotEmpty()) {
                                Chart(
                                    chart = columnChart(
                                        columns = listOf(
                                            lineComponent(
                                                color = ElectricPurple,
                                                thickness = 16.dp,
                                                shape = com.patrykandpatrick.vico.core.component.shape.Shapes.roundedCornerShape(allPercent = 40),
                                                dynamicShader = verticalGradient(
                                                    arrayOf(ElectricPurple, CyanGlow)
                                                )
                                            )
                                        )
                                    ),
                                    model = chartModel,
                                    startAxis = rememberStartAxis(
                                        label = textComponent(color = TextSecondary),
                                        axis = lineComponent(color = GlassBorder),
                                        guideline = lineComponent(color = GlassBorder.copy(alpha = 0.1f))
                                    ),
                                    bottomAxis = rememberBottomAxis(
                                        label = textComponent(color = TextSecondary),
                                        axis = lineComponent(color = GlassBorder),
                                        guideline = lineComponent(color = GlassBorder.copy(alpha = 0.1f))
                                    ),
                                    modifier = Modifier.height(200.dp)
                                )
                            } else {
                                Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("Analyzing signals...", color = TextSecondary)
                                }
                            }
                        }
                    }
                }
                
                // Detailed Matrix Breakdown
                item {
                    SectionHeader(title = "Category Matrix")
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        categorySpending.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
                            EliteCategoryCard(category, amount, totalAmount)
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun EliteCategoryCard(category: String, amount: Float, total: Float) {
    val progress = if (total > 0) amount / total else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1200), label = "progress")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassWhite)
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(CategoryUtils.getCategoryIcon(category), contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(category, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₹${String.format("%.0f", amount)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = TextPrimary)
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(GlassWhite)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(ElectricPurple, CyanGlow)))
                )
            }
        }
    }
}
