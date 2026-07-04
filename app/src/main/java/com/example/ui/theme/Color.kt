package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

// High Density Design Theme Colors
val HdPrimary = Color(0xFF2563EB) // Blue-600

val HdPrimaryLight: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF1E293B) else Color(0xFFDBEAFE)

val HdPrimaryDark: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF93C5FD) else Color(0xFF1E3A8A)

val HdAmber = Color(0xFFD97706) // Amber-600

val HdAmberLight: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF451A03) else Color(0xFFFEF3C7)

val HdAmberDark: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFFFBBF24) else Color(0xFF78350F)

val HdRed = Color(0xFFDC2626) // Red-600

val HdRedLight: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF450A0A) else Color(0xFFFEF2F2)

val HdRedDark: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFFF87171) else Color(0xFF7F1D1D)

val HdGreen = Color(0xFF16A34A) // Green-600

val HdGreenLight: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF064E3B) else Color(0xFFF0FDF4)

val HdBackground: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF0F172A) else Color(0xFFF7F9FC)

val HdSurface: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF1E293B) else Color(0xFFFFFFFF)

val HdBorder: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF334155) else Color(0xFFE2E8F0)

val HdTextPrimary: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFFF1F5F9) else Color(0xFF1B1B1F)

val HdTextSecondary: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF94A3B8) else Color(0xFF64748B)

val HdGrayLight: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF1E293B) else Color(0xFFF1F5F9)

// Keep legacy references for fallback compatibility
val PetrolPrimary = HdPrimary
val PetrolDark = Color(0xFF1E3A8A)

val PetrolLight: Color
    @Composable
    get() = HdPrimaryLight

val AmberSecondary = HdAmber

val AmberDark: Color
    @Composable
    get() = HdAmberDark

val AmberLight: Color
    @Composable
    get() = HdAmberLight

val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val LightBackground = Color(0xFFF7F9FC)
val LightSurface = Color(0xFFFFFFFF)
