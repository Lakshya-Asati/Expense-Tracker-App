package com.example.expensetracker.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryUtils {
    val categories = listOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Health", "Travel", "Other")

    fun getCategoryIcon(category: String): ImageVector {
        return when (category.lowercase()) {
            "food" -> Icons.Default.ShoppingCart
            "transport" -> Icons.Default.LocationOn
            "shopping" -> Icons.Default.AccountBox
            "entertainment" -> Icons.Default.PlayArrow
            "health" -> Icons.Default.Favorite
            "bills" -> Icons.AutoMirrored.Filled.List
            "travel" -> Icons.Default.Place
            else -> Icons.Default.Menu
        }
    }
}
