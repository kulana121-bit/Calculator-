package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ThemeMode

@Composable
fun LiquidGlassCard(
    theme: ThemeMode,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 6.dp,
    highlightIntensity: Float = 0.4f,
    content: @Composable BoxScope.() -> Unit
) {
    val isLight = theme.isLight

    // High-end dual-tone gradient background for true liquid glass feel
    val cardBgBrush = Brush.verticalGradient(
        colors = if (isLight) {
            listOf(
                Color.White.copy(alpha = 0.85f),
                Color.White.copy(alpha = 0.55f)
            )
        } else {
            listOf(
                theme.surfaceGlass.copy(alpha = 0.72f),
                theme.surfaceGlass.copy(alpha = 0.42f)
            )
        }
    )

    // Highly polished double-layered border simulating fine light refraction
    val cardBorderBrush = Brush.verticalGradient(
        colors = if (isLight) {
            listOf(
                Color.White.copy(alpha = 0.75f),
                Color.White.copy(alpha = 0.25f),
                theme.primaryAccent.copy(alpha = 0.15f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.32f),
                Color.White.copy(alpha = 0.05f),
                theme.primaryAccent.copy(alpha = 0.12f)
            )
        }
    )

    val shadowSpot = if (isLight) Color(0x10000000) else Color(0x52000000)
    val shadowAmbient = if (isLight) Color(0x05000000) else Color(0x24000000)

    Box(
        modifier = modifier
            .then(
                if (elevation > 0.dp) {
                    Modifier.shadow(
                        elevation = elevation,
                        shape = shape,
                        spotColor = shadowSpot,
                        ambientColor = shadowAmbient
                    )
                } else Modifier
            )
            .clip(shape)
            .background(cardBgBrush)
            .border(
                border = BorderStroke(borderWidth, cardBorderBrush),
                shape = shape
            ),
        content = content
    )
}
