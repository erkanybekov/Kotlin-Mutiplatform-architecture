package com.erkan.experimentkmp.android.uikit

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object ExpensePalette {
    val Background = Color(0xFF090E1A)
    val BackgroundTop = Color(0xFF10192E)
    val BackgroundBottom = Color(0xFF080C16)
    val Surface = Color(0xFF10192E)
    val SurfaceMuted = Color(0xFF11192D)
    val SurfaceStrong = Color(0xFF121A2D)
    val SurfaceInset = Color(0xFF0E1528)
    val SurfaceChip = Color(0xFF18223A)
    val TextPrimary = Color(0xFFF5F7FC)
    val TextSecondary = Color(0xFF93A0BE)
    val TextMuted = Color(0xFF6F7C99)
    val AccentWarm = Color(0xFFFFA65B)
    val AccentGold = Color(0xFFFFBE4D)
    val AccentSuccess = Color(0xFF2AD5A6)
    val AccentIndigo = Color(0xFF7C83FF)
    val Error = Color(0xFFFF6E7A)
}

val ExpenseBackgroundBrush: Brush = Brush.verticalGradient(
    colors = listOf(
        ExpensePalette.Background,
        ExpensePalette.BackgroundTop,
        ExpensePalette.BackgroundBottom,
    ),
)

val ExpenseHeroBrush: Brush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1A2440),
        Color(0xFF11192E),
        Color(0xFF0D1325),
    ),
)

val ExpenseWarmBrush: Brush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFFF8A5B),
        ExpensePalette.AccentGold,
    ),
)

fun expenseColorFromHex(hex: String): Color {
    val normalized = hex.removePrefix("#")
    val value = normalized.toLongOrNull(16) ?: return ExpensePalette.AccentGold
    return when (normalized.length) {
        6 -> Color((0xFF000000 or value).toInt())
        8 -> Color(value.toInt())
        else -> ExpensePalette.AccentGold
    }
}
