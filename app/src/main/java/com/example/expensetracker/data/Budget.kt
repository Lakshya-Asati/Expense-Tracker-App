package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val period: String, // "Daily", "Weekly", "Monthly"
    val startDate: Long
)
