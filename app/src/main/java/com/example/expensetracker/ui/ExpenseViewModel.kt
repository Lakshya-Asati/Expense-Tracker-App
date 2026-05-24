package com.example.expensetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.Expense
import com.example.expensetracker.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.expensetracker.data.Budget
import com.example.expensetracker.utils.UserPreferences
import kotlinx.coroutines.flow.map
import java.util.Calendar

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpenses: StateFlow<Double?> = repository.totalExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val activeBudget: StateFlow<Budget?> = repository.activeBudget
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userName: StateFlow<String> = userPreferences.userName

    fun updateUserName(newName: String) {
        userPreferences.updateUserName(newName)
    }

    fun insert(expense: Expense) = viewModelScope.launch {
        repository.insert(expense)
    }

    fun update(expense: Expense) = viewModelScope.launch {
        repository.update(expense)
    }

    fun delete(expense: Expense) = viewModelScope.launch {
        repository.delete(expense)
    }

    suspend fun getExpenseById(id: Int): Expense? {
        return repository.getExpenseById(id)
    }

    fun setBudget(amount: Double, period: String) = viewModelScope.launch {
        repository.setBudget(Budget(amount = amount, period = period, startDate = System.currentTimeMillis()))
    }

    fun getCurrentPeriodExpenses(expenses: List<Expense>, period: String): Double {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        return expenses.filter { expense ->
            calendar.timeInMillis = expense.date
            val expenseYear = calendar.get(Calendar.YEAR)
            val expenseMonth = calendar.get(Calendar.MONTH)
            val expenseDay = calendar.get(Calendar.DAY_OF_YEAR)
            val expenseWeek = calendar.get(Calendar.WEEK_OF_YEAR)

            calendar.timeInMillis = now
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
            val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)

            when (period) {
                "Daily" -> expenseYear == currentYear && expenseDay == currentDay
                "Weekly" -> expenseYear == currentYear && expenseWeek == currentWeek
                "Monthly" -> expenseYear == currentYear && expenseMonth == currentMonth
                else -> false
            }
        }.sumOf { it.amount }
    }
}

class ExpenseViewModelFactory(
    private val repository: ExpenseRepository,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
