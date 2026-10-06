package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun SmartCalculatorTheme(
    themeMode: ThemeMode = ThemeMode.AURORA,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val colorScheme = if (themeMode == ThemeMode.DYNAMIC) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            // Graceful fallback to default theme on older Android (API 29)
            if (isDark) {
                darkColorScheme(
                    primary = ThemeMode.DARK.primaryAccent,
                    secondary = ThemeMode.DARK.secondaryAccent,
                    tertiary = ThemeMode.DARK.tertiaryAccent,
                    background = ThemeMode.DARK.backgroundColors.first(),
                    surface = ThemeMode.DARK.surfaceGlass,
                    onPrimary = Color(0xFF030712),
                    onSecondary = Color.White,
                    onTertiary = Color.White,
                    onBackground = ThemeMode.DARK.textPrimary,
                    onSurface = ThemeMode.DARK.textPrimary
                )
            } else {
                lightColorScheme(
                    primary = ThemeMode.LIGHT.primaryAccent,
                    secondary = ThemeMode.LIGHT.secondaryAccent,
                    tertiary = ThemeMode.LIGHT.tertiaryAccent,
                    background = ThemeMode.LIGHT.backgroundColors.first(),
                    surface = ThemeMode.LIGHT.surfaceGlass,
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onTertiary = Color.White,
                    onBackground = ThemeMode.LIGHT.textPrimary,
                    onSurface = ThemeMode.LIGHT.textPrimary
                )
            }
        }
    } else if (themeMode == ThemeMode.AMOLED || themeMode == ThemeMode.AMOLED_BLACK) {
        darkColorScheme(
            primary = themeMode.primaryAccent,
            secondary = themeMode.secondaryAccent,
            tertiary = themeMode.tertiaryAccent,
            background = Color(0xFF000000),
            surface = Color(0xFF000000),
            onPrimary = Color(0xFF030712),
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
    } else if (themeMode.isLight) {
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
