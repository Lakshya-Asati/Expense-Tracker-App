package com.example.expensetracker

import android.app.Application
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.repository.ExpenseRepository
import com.example.expensetracker.utils.UserPreferences

class ExpenseApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { 
        ExpenseRepository(database.expenseDao(), database.budgetDao()) 
    }
    val userPreferences by lazy { UserPreferences(this) }
}
