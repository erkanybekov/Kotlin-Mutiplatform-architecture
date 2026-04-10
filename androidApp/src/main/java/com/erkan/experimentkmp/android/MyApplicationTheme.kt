package com.erkan.experimentkmp.android

import com.erkan.experimentkmp.android.uikit.ExpensePalette
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    val colors = darkColorScheme(
        primary = ExpensePalette.AccentWarm,
        secondary = ExpensePalette.AccentSuccess,
        tertiary = ExpensePalette.AccentIndigo,
        background = ExpensePalette.Background,
        surface = ExpensePalette.Surface,
        surfaceVariant = Color(0xFF131E34),
        onBackground = ExpensePalette.TextPrimary,
        onSurface = ExpensePalette.TextPrimary,
        onSurfaceVariant = ExpensePalette.TextSecondary,
        error = ExpensePalette.Error,
    )
    val typography = Typography(
        headlineLarge = TextStyle(
            fontWeight = FontWeight.Black,
            fontSize = 34.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.8).sp,
        ),
        displaySmall = TextStyle(
            fontWeight = FontWeight.Black,
            fontSize = 36.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.6).sp,
        ),
        titleLarge = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        ),
        titleMedium = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
        ),
        labelLarge = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.2.sp,
        ),
        bodyMedium = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        ),
        bodySmall = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 18.sp,
        ),
    )
    val shapes = Shapes(
        small = RoundedCornerShape(18.dp),
        medium = RoundedCornerShape(24.dp),
        large = RoundedCornerShape(32.dp),
    )

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
