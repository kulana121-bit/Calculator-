package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LiquidGlassBackground(
    theme: ThemeMode,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val bgColors = theme.backgroundColors
    val accentColor = theme.primaryAccent
    val secondaryColor = theme.secondaryAccent
    val isLight = theme.isLight

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val w = size.width
                val h = size.height

                // 1. Subtle base gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = bgColors,
                        startY = 0f,
                        endY = h
                    )
                )

                // 2. Primary ambient warm glow blob (Top-Right / Upper quadrant)
                val primaryAlpha = if (isLight) 0.18f else 0.22f
                val primaryCenter = Offset(x = w * 0.82f, y = h * 0.22f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = primaryAlpha),
                            secondaryColor.copy(alpha = primaryAlpha * 0.45f),
                            Color.Transparent
                        ),
                        center = primaryCenter,
                        radius = w * 0.82f
                    )
                )

                // 3. Secondary ambient warmth (Bottom-Right / Lower quadrant)
                val secondaryAlpha = if (isLight) 0.12f else 0.16f
                val secondaryCenter = Offset(x = w * 0.90f, y = h * 0.85f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            secondaryColor.copy(alpha = secondaryAlpha),
                            accentColor.copy(alpha = secondaryAlpha * 0.35f),
                            Color.Transparent
                        ),
                        center = secondaryCenter,
                        radius = w * 0.75f
                    )
                )

                // 4. Delicate Top-Left fill light to balance contrast
                if (isLight) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.1f, h * 0.05f),
                            radius = w * 0.5f
                        )
                    )
                }
            },
        content = content
    )
}

fun Modifier.softBlurGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color.White,
    borderColor: Color = Color(0x0D000000),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 6.dp
): Modifier = this
    .then(
        if (elevation > 0.dp) {
            Modifier.shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color(0x08000000),
                spotColor = Color(0x12000000)
            )
        } else Modifier
    )
    .clip(shape)
    .background(backgroundColor)
    .border(
        border = BorderStroke(borderWidth, borderColor),
        shape = shape
    )

fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color.White,
    borderColor: Color = Color(0x0D000000),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 6.dp
): Modifier = softBlurGlass(
    shape = shape,
    backgroundColor = backgroundColor,
    borderColor = borderColor,
    borderWidth = borderWidth,
    elevation = elevation
)
