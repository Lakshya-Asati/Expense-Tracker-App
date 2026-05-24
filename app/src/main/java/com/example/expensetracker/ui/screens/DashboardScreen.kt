package com.example.expensetracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.ui.ExpenseViewModel
import com.example.expensetracker.ui.components.ModernExpenseItem
import com.example.expensetracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.expensetracker.utils.CsvExporter

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.expensetracker.utils.CategoryUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    onAddExpense: () -> Unit,
    onViewAll: () -> Unit,
    onEditExpense: (Int) -> Unit
) {
    val context = LocalContext.current
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val activeBudget by viewModel.activeBudget.collectAsState()
    val userName by viewModel.userName.collectAsState()
    
    var selectedCategory by remember { mutableStateOf("All") }
    var showProfileDialog by remember { mutableStateOf(false) }
    
    if (showProfileDialog) {
        ProfileDialog(
            currentName = userName,
            onDismiss = { showProfileDialog = false },
            onSaveName = { viewModel.updateUserName(it) }
        )
    }

    val filteredExpenses = if (selectedCategory == "All") {
        allExpenses
    } else {
        allExpenses.filter { it.category == selectedCategory }
    }

    // Group expenses by Date
    val groupedExpenses = filteredExpenses.groupBy { 
        val date = Date(it.date)
        val today = Calendar.getInstance()
        val expenseDate = Calendar.getInstance().apply { time = date }
        
        when {
            today.get(Calendar.DAY_OF_YEAR) == expenseDate.get(Calendar.DAY_OF_YEAR) &&
            today.get(Calendar.YEAR) == expenseDate.get(Calendar.YEAR) -> "Today"
            today.get(Calendar.DAY_OF_YEAR) - 1 == expenseDate.get(Calendar.DAY_OF_YEAR) &&
            today.get(Calendar.YEAR) == expenseDate.get(Calendar.YEAR) -> "Yesterday"
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
        }
    }

    val chartEntryModel = if (filteredExpenses.isNotEmpty()) {
        entryModelOf(*filteredExpenses.take(10).reversed().mapIndexed { index, expense -> index to expense.amount }.map { it.second }.toTypedArray())
    } else null

    val infiniteTransition = rememberInfiniteTransition(label = "elite_glow")
    val animX by infiniteTransition.animateFloat(
        initialValue = -100f, targetValue = 400f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Reverse),
        label = "x"
    )
    val animY by infiniteTransition.animateFloat(
        initialValue = -100f, targetValue = 600f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse),
        label = "y"
    )

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = animX.dp, y = animY.dp)
                .background(Brush.radialGradient(listOf(PurpleGlow, Color.Transparent)))
        )
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                EliteHeader(
                    userName = userName,
                    onExport = {
                        val csvContent = CsvExporter.exportExpensesToCsv(allExpenses)
                        val file = CsvExporter.saveCsvToFile(context, csvContent)
                        if (file != null) {
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Report"))
                        } else {
                            Toast.makeText(context, "Failed to export", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onProfileClick = { showProfileDialog = true }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                item {
                    EliteSummaryCard(totalAmount = totalExpenses ?: 0.0, viewModel = viewModel)
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(listOf("All") + CategoryUtils.categories) { category ->
                            FilterChip(
                                label = category,
                                selected = category == selectedCategory,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }
                }

                if (chartEntryModel != null) {
                    item {
                        Column {
                            SectionHeader(title = "Spend Dynamics")
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth().border(1.dp, GlassBorder, RoundedCornerShape(32.dp)),
                                shape = RoundedCornerShape(32.dp),
                                color = SlateBlue.copy(alpha = 0.4f)
                            ) {
                                Chart(
                                    chart = lineChart(
                                        lines = listOf(
                                            LineChart.LineSpec(
                                                lineColor = ElectricPurple.toArgb(),
                                                lineBackgroundShader = verticalGradient(
                                                    arrayOf(ElectricPurple, Color.Transparent)
                                                )
                                            )
                                        )
                                    ),
                                    model = chartEntryModel,
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
                                    modifier = Modifier.padding(24.dp).height(180.dp)
                                )
                            }
                        }
                    }
                }

                if (filteredExpenses.isEmpty()) {
                    item { EmptyState() }
                } else {
                    groupedExpenses.forEach { (date, expenses) ->
                        item {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                        }
                        itemsIndexed(expenses.take(10)) { index, expense ->
                            ModernExpenseItem(expense = expense, index = index, onClick = { onEditExpense(expense.id) })
                        }
                    }
                    
                    if (filteredExpenses.size > 10) {
                        item {
                            TextButton(
                                onClick = onViewAll,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("See Full History", color = ElectricPurple, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(140.dp)) } // More spacer for the dock
            }
        }
    }
}

@Composable
fun EliteHeader(userName: String, onExport: () -> Unit, onProfileClick: () -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good Morning,"
        in 12..16 -> "Good Afternoon,"
        in 17..20 -> "Good Evening,"
        else -> "Good Night,"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = com.example.expensetracker.R.drawable.brand_logo_final),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SlateBlue.copy(alpha = 0.5f))
                    .border(1.dp, GlassBorder, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(greeting, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(userName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = TextPrimary)
            }
        }
        Row {
            IconButton(
                onClick = onExport,
                modifier = Modifier.background(GlassWhite, CircleShape).border(1.dp, GlassBorder, CircleShape)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Export", tint = TextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(PremiumGradient))
                    .clickable { onProfileClick() }
                    .padding(2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(DeepNavy), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = ElectricPurple)
                }
            }
        }
    }
}

@Composable
fun ProfileDialog(currentName: String, onDismiss: () -> Unit, onSaveName: (String) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (isEditing) {
                TextButton(onClick = {
                    onSaveName(editedName)
                    isEditing = false
                }) {
                    Text("Save", color = SuccessEmerald, fontWeight = FontWeight.Bold)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = ElectricPurple, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (isEditing) {
                TextButton(onClick = { isEditing = false }) {
                    Text("Cancel", color = ErrorRose)
                }
            }
        },
        title = { Text("COMMANDER PROFILE", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(PremiumGradient))
                            .padding(2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(DeepNavy), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        if (isEditing) {
                            OutlinedTextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricPurple,
                                    unfocusedBorderColor = GlassBorder
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(currentName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Elite Member", style = MaterialTheme.typography.labelMedium, color = ElectricPurple)
                        }
                    }
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = GlassBorder)
                Spacer(modifier = Modifier.height(24.dp))
                ProfileStatItem("Status", "Operational", SuccessEmerald)
                ProfileStatItem("Security", "Encrypted", CyanGlow)
                ProfileStatItem("Data Sync", "Enabled", SuccessEmerald)
            }
        },
        containerColor = SlateBlue,
        titleContentColor = TextPrimary,
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(32.dp))
    )
}

@Composable
fun ProfileStatItem(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(8.dp))
            Text(value, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EliteSummaryCard(totalAmount: Double, viewModel: ExpenseViewModel) {
    val activeBudget by viewModel.activeBudget.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    
    val currentPeriodExpenses = if (activeBudget != null) {
        viewModel.getCurrentPeriodExpenses(allExpenses, activeBudget!!.period)
    } else totalAmount

    val remainingBalance = if (activeBudget != null) {
        (activeBudget!!.amount - currentPeriodExpenses).coerceAtLeast(0.0)
    } else totalAmount

    val progress = if (activeBudget != null && activeBudget!!.amount > 0) {
        (currentPeriodExpenses / activeBudget!!.amount).coerceIn(0.0, 1.0).toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1500), label = "progress")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .shadow(24.dp, RoundedCornerShape(36.dp), spotColor = ElectricPurple.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(36.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF020617))))
                .border(1.dp, GlassBorder, RoundedCornerShape(36.dp))
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = 240.dp, y = (-40).dp)
                    .rotate(45f)
                    .background(GlassWhite)
            )

            Column(modifier = Modifier.padding(32.dp)) {
                Text(
                    text = if (activeBudget != null) "REMAINING BUDGET" else "TOTAL SPENDING",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "₹${String.format(Locale.US, "%,.2f", if (activeBudget != null) remainingBalance else totalAmount)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 42.sp
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (activeBudget != null) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Monthly Pulse", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = if(progress > 0.9f) ErrorRose else CyanGlow)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(animatedProgress).fillMaxHeight().clip(CircleShape).background(Brush.horizontalGradient(listOf(ElectricPurple, CyanGlow)))
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SuccessEmerald))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Financial Health: Optimal", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) ElectricPurple else GlassWhite,
        border = BorderStroke(1.dp, if (selected) Color.Transparent else GlassBorder),
        modifier = Modifier.height(40.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) Color.White else TextSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Black,
        color = TextPrimary,
        letterSpacing = 0.5.sp
    )
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(60.dp).rotate(45f), tint = TextSecondary.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("No signals detected yet", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}
