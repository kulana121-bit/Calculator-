package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ThemeMode

val LocalVibrationEnabled = androidx.compose.runtime.staticCompositionLocalOf { true }
val LocalSoundEnabled = androidx.compose.runtime.staticCompositionLocalOf { false }

enum class CalcButtonType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    ACTION,
    EQUALS,
    MEMORY
}

@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    theme: ThemeMode,
    modifier: Modifier = Modifier,
    type: CalcButtonType = CalcButtonType.NUMBER,
    icon: ImageVector? = null,
    fontSize: TextUnit = 24.sp,
    shape: Shape = RoundedCornerShape(26.dp),
    testTag: String = text.lowercase(),
    vibrationEnabled: Boolean = LocalVibrationEnabled.current,
    soundEnabled: Boolean = LocalSoundEnabled.current
) {
    val haptic = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile physical press spring animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale"
    )

    val isLight = theme.isLight

    val (bgColor, textColor, borderColor, elevation, shadowSpot) = when (type) {
        CalcButtonType.NUMBER -> {
            if (isLight) {
                ButtonColors(
                    bg = Color.White,
                    text = Color(0xFF1C1C1E),
                    border = Color(0x0A000000),
                    elevation = 4.dp,
                    shadow = Color(0x12000000)
                )
            } else {
                ButtonColors(
                    bg = theme.buttonNumberColor,
                    text = theme.textPrimary,
                    border = Color(0x1AFFFFFF),
                    elevation = 4.dp,
                    shadow = Color(0x40000000)
                )
            }
        }
        CalcButtonType.OPERATOR -> {
            if (isLight) {
                ButtonColors(
                    bg = Color(0xFFFFEDE0),
                    text = Color(0xFFFF7A00),
                    border = Color(0x1AFF7A00),
                    elevation = 3.dp,
                    shadow = Color(0x14FF7A00)
                )
            } else {
                ButtonColors(
                    bg = theme.buttonOpColor,
                    text = theme.primaryAccent,
                    border = theme.primaryAccent.copy(alpha = 0.4f),
                    elevation = 4.dp,
                    shadow = Color(0x40000000)
                )
            }
        }
        CalcButtonType.FUNCTION, CalcButtonType.ACTION, CalcButtonType.MEMORY -> {
            if (isLight) {
                ButtonColors(
                    bg = Color(0xFFF6F5F2),
                    text = Color(0xFF1C1C1E),
                    border = Color(0x0A000000),
                    elevation = 3.dp,
                    shadow = Color(0x0F000000)
                )
            } else {
                ButtonColors(
                    bg = theme.buttonFuncColor,
                    text = theme.textPrimary,
                    border = Color(0x1AFFFFFF),
                    elevation = 3.dp,
                    shadow = Color(0x40000000)
                )
            }
        }
        CalcButtonType.EQUALS -> {
            ButtonColors(
                bg = theme.buttonActionColor,
                text = Color.White,
                border = Color.Transparent,
                elevation = 6.dp,
                shadow = theme.buttonActionColor.copy(alpha = 0.5f)
            )
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (elevation > 0.dp) {
                    Modifier.shadow(
                        elevation = if (isPressed) elevation / 2 else elevation,
                        shape = shape,
                        spotColor = shadowSpot,
                        ambientColor = Color(0x08000000)
                    )
                } else Modifier
            )
            .clip(shape)
            .background(bgColor)
            .border(
                border = BorderStroke(1.dp, borderColor),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (vibrationEnabled) {
                        com.example.util.VibrationHelper.tick(context)
                    } else {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    }
                    if (soundEnabled) {
                        view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                    }
                    onClick()
                }
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = textColor,
                modifier = Modifier.padding(12.dp)
            )
        } else {
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = if (type == CalcButtonType.EQUALS || type == CalcButtonType.OPERATOR) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class ButtonColors(
    val bg: Color,
    val text: Color,
    val border: Color,
    val elevation: androidx.compose.ui.unit.Dp,
    val shadow: Color
)
