package com.example.expensetracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.Expense
import com.example.expensetracker.ui.ExpenseViewModel
import com.example.expensetracker.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.example.expensetracker.utils.CategoryUtils
import android.app.DatePickerDialog
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    viewModel: ExpenseViewModel,
    expenseId: Int? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            date = selectedDate.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(expenseId) {
        if (expenseId != null) {
            val expense = viewModel.getExpenseById(expenseId)
            if (expense != null) {
                amount = expense.amount.toString()
                category = expense.category
                note = expense.note
                date = expense.date
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            if (expenseId == null) "New Entry" else "Edit Record",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    },
                    actions = {
                        if (expenseId != null) {
                            IconButton(onClick = {
                                scope.launch {
                                    val expense = viewModel.getExpenseById(expenseId)
                                    if (expense != null) {
                                        viewModel.delete(expense)
                                        onNavigateBack()
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRose)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Column {
                        Text("Transaction Details", style = MaterialTheme.typography.labelLarge, color = ElectricPurple, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        PremiumTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = "Amount (₹)",
                            placeholder = "0.00",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PremiumTextField(
                                value = category,
                                onValueChange = {},
                                label = "Category",
                                isReadOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(SlateBlue)
                            ) {
                                CategoryUtils.categories.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption, color = TextPrimary) },
                                        onClick = {
                                            category = selectionOption
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        PremiumTextField(
                            value = dateFormat.format(Date(date)),
                            onValueChange = {},
                            label = "Date",
                            isReadOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { datePickerDialog.show() }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = ElectricPurple)
                                }
                            },
                            modifier = Modifier.clickable { datePickerDialog.show() }
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        
                        PremiumTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = "Note",
                            placeholder = "Optional description",
                            minLines = 3
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(
                        onClick = {
                            val expenseAmount = amount.toDoubleOrNull() ?: 0.0
                            if (expenseAmount > 0 && category.isNotEmpty()) {
                                val expense = Expense(
                                    id = expenseId ?: 0,
                                    amount = expenseAmount,
                                    category = category,
                                    date = date,
                                    note = note
                                )
                                if (expenseId == null) {
                                    viewModel.insert(expense)
                                } else {
                                    viewModel.update(expense)
                                }
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                    ) {
                        Text(
                            if (expenseId == null) "Confirm Transaction" else "Update Record", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isReadOnly: Boolean = false,
    minLines: Int = 1,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = TextSecondary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextSecondary.copy(alpha = 0.5f)) },
            readOnly = isReadOnly,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricPurple,
                unfocusedBorderColor = GlassBorder,
                focusedContainerColor = SlateBlue.copy(alpha = 0.3f),
                unfocusedContainerColor = SlateBlue.copy(alpha = 0.3f),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            trailingIcon = trailingIcon,
            minLines = minLines,
            singleLine = minLines == 1,
            keyboardOptions = keyboardOptions
        )
    }
}
