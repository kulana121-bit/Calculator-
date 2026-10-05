package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    theme: ThemeMode,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.85f) }
    val progressAnim = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "SplashOrbPulse")
    val blobPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blobPulse"
    )

    LaunchedEffect(Unit) {
        // Smooth entrance
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(700, easing = FastOutSlowInEasing)
        )
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(1400, easing = LinearEasing)
        )
        delay(300)
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F5))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .drawBehind {
                val w = size.width
                val h = size.height

                // Soft blurred orange ambient gradient blob in background
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF9500).copy(alpha = 0.30f * blobPulse),
                            Color(0xFFFF7A00).copy(alpha = 0.18f * blobPulse),
                            Color(0xFFFFB340).copy(alpha = 0.06f * blobPulse),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.5f, h * 0.42f),
                        radius = w * 0.65f
                    )
                )

                // Additional warm ambient glow at bottom right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF7A00).copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.85f, h * 0.82f),
                        radius = w * 0.6f
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .graphicsLayer {
                    alpha = alphaAnim.value
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                }
                .padding(horizontal = 32.dp)
        ) {
            // Center floating calculator icon
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(36.dp),
                        spotColor = Color(0x2BFF7A00),
                        ambientColor = Color(0x12000000)
                    )
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = Color(0x1AFFFFFF),
                        shape = RoundedCornerShape(36.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_calculator_asset),
                    contentDescription = "Calculator Logo",
                    modifier = Modifier
                        .size(124.dp)
                        .clip(RoundedCornerShape(30.dp))
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Calculator",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E),
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Calculate a smarter tomorrow",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF8E8E93),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Minimal animated orange progress bar
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E5EA))
            ) {
                Box(
                    modifier = Modifier
                        .width((120 * progressAnim.value).dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFF9500), Color(0xFFFF7A00))
                            )
                        )
                )
            }
        }
    }
}
