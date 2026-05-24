package com.example.expensetracker.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _userName = MutableStateFlow(sharedPreferences.getString("user_name", "Lakshya") ?: "Lakshya")
    val userName: StateFlow<String> = _userName

    fun updateUserName(newName: String) {
        sharedPreferences.edit().putString("user_name", newName).apply()
        _userName.value = newName
    }
}
