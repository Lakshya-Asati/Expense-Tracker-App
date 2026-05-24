package com.example.expensetracker.ui.theme

import androidx.compose.ui.graphics.Color

// Quantum Midnight Palette - Hackathon Edition
val DeepNavy = Color(0xFF020617) 
val SlateBlue = Color(0xFF0F172A) // Darker for more contrast
val ElectricPurple = Color(0xFF8B5CF6) 
val NeonPink = Color(0xFFEC4899) 
val CyanGlow = Color(0xFF22D3EE) // Brighter Cyan

val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF64748B)

val GlassWhite = Color(0x0DFFFFFF) // Very subtle
val GlassBorder = Color(0x1AFFFFFF)

val SuccessEmerald = Color(0xFF10B981)
val ErrorRose = Color(0xFFF43F5E)

// High-End Gradients
val PremiumGradient = listOf(ElectricPurple, Color(0xFF6366F1))
val WalletGradient = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
val ActionGradient = listOf(NeonPink, Color(0xFFF43F5E))

// Glow Effects
val PurpleGlow = ElectricPurple.copy(alpha = 0.15f)
val PinkGlow = NeonPink.copy(alpha = 0.15f)

// Legacy compatibility
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
