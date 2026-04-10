package com.erkan.experimentkmp.android.uikit

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object ExpensePalette {
    val Background = Color(0xFF111318)
    val Surface = Color(0xFF181C22)
    val SurfaceMuted = Color(0xFF20242C)
    val SurfaceStrong = Color(0xFF232832)
    val SurfaceInset = Color(0xFF151A21)
    val SurfaceChip = Color(0xFF272D38)
    val TextPrimary = Color(0xFFF1F4FA)
    val TextSecondary = Color(0xFFC0C6D4)
    val TextMuted = Color(0xFF959CAC)
    val AccentWarm = Color(0xFFFA8A57)
    val AccentGold = Color(0xFFF4B844)
    val AccentSuccess = Color(0xFF2FBF9B)
    val AccentIndigo = Color(0xFF7E86F6)
    val Error = Color(0xFFE46A74)
}

val ExpenseBackgroundBrush: Brush = Brush.verticalGradient(
    colors = listOf(
        ExpensePalette.Background,
        ExpensePalette.Background,
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
