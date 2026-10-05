package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SmartCalculatorTheme(
    themeMode: ThemeMode = ThemeMode.AURORA,
    content: @Composable () -> Unit
) {
    val colorScheme = if (themeMode.isLight) {
        lightColorScheme(
            primary = themeMode.primaryAccent,
            secondary = themeMode.secondaryAccent,
            tertiary = themeMode.tertiaryAccent,
            background = themeMode.backgroundColors.first(),
            surface = themeMode.surfaceGlass,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = themeMode.textPrimary,
            onSurface = themeMode.textPrimary
        )
    } else {
        darkColorScheme(
            primary = themeMode.primaryAccent,
            secondary = themeMode.secondaryAccent,
            tertiary = themeMode.tertiaryAccent,
            background = themeMode.backgroundColors.first(),
            surface = themeMode.surfaceGlass,
            onPrimary = Color(0xFF030712),
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = themeMode.textPrimary,
            onSurface = themeMode.textPrimary
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for preview/test compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    SmartCalculatorTheme(themeMode = ThemeMode.AURORA, content = content)
}
