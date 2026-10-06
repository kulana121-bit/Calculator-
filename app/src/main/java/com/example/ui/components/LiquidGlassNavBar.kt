package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas as AndroidCanvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint as AndroidPaint
import android.graphics.PorterDuff
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ThemeMode
import com.example.util.VibrationHelper
import kotlin.math.abs
import kotlin.math.min

enum class AppNavTab(val title: String, val icon: ImageVector) {
    CALCULATOR("Calc", Icons.Default.Calculate),
    CURRENCY("Currency", Icons.Default.CurrencyExchange),
    UNITS("Units", Icons.Default.SquareFoot),
    TOOLS("Tools", Icons.Default.Widgets),
    HISTORY("History", Icons.Default.History)
}

@Composable
fun LiquidGlassNavBar(
    selectedTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit,
    theme: ThemeMode,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val isLight = theme.isLight
    val vibrationEnabled = LocalVibrationEnabled.current

    val currentTab by rememberUpdatedState(selectedTab)
    val currentOnTabSelected by rememberUpdatedState(onTabSelected)

    val isLandscape = androidx.compose.ui.platform.LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val outerPaddingHorizontal = if (isLandscape) 90.dp else 18.dp
    val outerPaddingBottom = if (isLandscape) 6.dp else 12.dp

    // Frosted glass capsule background colors matching LiquidGlassCard style
    val barBgBrush = Brush.verticalGradient(
        colors = if (isLight) {
            listOf(
                Color.White.copy(alpha = 0.90f),
                Color.White.copy(alpha = 0.72f)
            )
        } else {
            listOf(
                theme.surfaceGlass.copy(alpha = 0.88f),
                theme.surfaceGlass.copy(alpha = 0.65f)
            )
        }
    )

    val barBorderBrush = Brush.verticalGradient(
        colors = if (isLight) {
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.35f),
                theme.primaryAccent.copy(alpha = 0.20f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.38f),
                Color.White.copy(alpha = 0.08f),
                theme.primaryAccent.copy(alpha = 0.25f)
            )
        }
    )

    val shadowSpot = if (isLight) Color(0x18000000) else Color(0x60000000)
    val shadowAmbient = if (isLight) Color(0x0A000000) else Color(0x30000000)

    var containerWidth by remember { mutableIntStateOf(0) }
    var containerHeight by remember { mutableIntStateOf(0) }
    val tabCount = AppNavTab.values().size

    // Drag interaction states
    var isDragging by remember { mutableStateOf(false) }
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragTargetTab by remember { mutableStateOf(selectedTab) }

    val tabWidthPx = if (containerWidth > 0) containerWidth.toFloat() / tabCount else 0f

    // Target center X for the bubble
    val targetCenterX = if (isDragging && containerWidth > 0) {
        dragX.coerceIn(tabWidthPx * 0.5f, containerWidth - tabWidthPx * 0.5f)
    } else {
        (selectedTab.ordinal + 0.5f) * tabWidthPx
    }

    // Dual-spring physics: leadX rushes forward, trailX catches up smoothly
    // creating fluid gooey stretching between old and new positions
    val leadX by animateFloatAsState(
        targetValue = targetCenterX,
        animationSpec = spring(
            dampingRatio = if (isDragging) 0.88f else 0.76f,
            stiffness = if (isDragging) 750f else 460f
        ),
        label = "gooeyLeadX"
    )

    val trailX by animateFloatAsState(
        targetValue = targetCenterX,
        animationSpec = spring(
            dampingRatio = if (isDragging) 0.85f else 0.82f,
            stiffness = if (isDragging) 500f else 260f
        ),
        label = "gooeyTrailX"
    )

    val bubbleRadiusPx = if (tabWidthPx > 0f) min(tabWidthPx * 0.36f, 26.dp.value * 2.75f) else 0f
    val blurRadiusPx = with(density) { 24.dp.toPx() }

    // Reusable offscreen Bitmap and Canvas to ensure 60fps zero-allocation per frame (works on all API levels)
    var offscreenBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var offscreenCanvas by remember { mutableStateOf<AndroidCanvas?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            offscreenBitmap?.recycle()
            offscreenBitmap = null
            offscreenCanvas = null
        }
    }

    // Blur Paint using universal BlurMaskFilter (supported on all API levels back to Android 10/API 29 and earlier)
    val blurPaint = remember(blurRadiusPx) {
        AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = AndroidPaint.Style.FILL
            maskFilter = BlurMaskFilter(blurRadiusPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    // High-contrast alpha threshold Paint using universal ColorMatrixColorFilter
    // Snaps blurred halo edges into a crisp, organic liquid merge with theme-aware tint
    val thresholdPaint = remember(theme.primaryAccent, isLight) {
        AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            val bubbleColor = if (isLight) {
                theme.primaryAccent.copy(alpha = 0.18f)
            } else {
                theme.primaryAccent.copy(alpha = 0.26f)
            }
            val r = bubbleColor.red * 255f
            val g = bubbleColor.green * 255f
            val b = bubbleColor.blue * 255f
            val a = bubbleColor.alpha

            val alphaScale = 22f
            val alphaThreshold = 110f
            val alphaOffset = -alphaThreshold * alphaScale * a

            val matrix = ColorMatrix(floatArrayOf(
                0f, 0f, 0f, 0f, r,
                0f, 0f, 0f, 0f, g,
                0f, 0f, 0f, 0f, b,
                0f, 0f, 0f, alphaScale * a, alphaOffset
            ))
            colorFilter = ColorMatrixColorFilter(matrix)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = outerPaddingHorizontal)
            .padding(bottom = outerPaddingBottom)
    ) {
        // Floating Frosted Glass Bar Capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(30.dp),
                    spotColor = shadowSpot,
                    ambientColor = shadowAmbient
                )
                .clip(RoundedCornerShape(30.dp))
                .background(barBgBrush)
                .border(
                    border = BorderStroke(1.dp, barBorderBrush),
                    shape = RoundedCornerShape(30.dp)
                )
                .drawBehind {
                    // Subtle 1dp inner top highlight line across the floating capsule
                    val highlightAlpha = if (isLight) 0.70f else 0.28f
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = highlightAlpha),
                                Color.Transparent
                            )
                        ),
                        start = Offset(24.dp.toPx(), 1.5f),
                        end = Offset(size.width - 24.dp.toPx(), 1.5f),
                        strokeWidth = 1.2f
                    )
                }
                .padding(horizontal = 6.dp, vertical = 5.dp)
        ) {
            // Interactive Dock Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .onSizeChanged { size ->
                        containerWidth = size.width
                        containerHeight = size.height
                        if (size.width > 0 && size.height > 0) {
                            val currentBmp = offscreenBitmap
                            if (currentBmp == null || currentBmp.width != size.width || currentBmp.height != size.height) {
                                currentBmp?.recycle()
                                val newBmp = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
                                offscreenBitmap = newBmp
                                offscreenCanvas = AndroidCanvas(newBmp)
                            }
                        }
                    }
                    .pointerInput(containerWidth) {
                        if (containerWidth <= 0) return@pointerInput
                        val stepPx = containerWidth.toFloat() / tabCount
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                dragX = offset.x
                                val tabIndex = (offset.x / stepPx).toInt().coerceIn(0, tabCount - 1)
                                dragTargetTab = AppNavTab.values()[tabIndex]
                            },
                            onDragEnd = {
                                isDragging = false
                                if (dragTargetTab != currentTab) {
                                    if (vibrationEnabled) {
                                        VibrationHelper.tick(context)
                                    }
                                    currentOnTabSelected(dragTargetTab)
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                dragTargetTab = currentTab
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                dragX = (dragX + dragAmount).coerceIn(0f, containerWidth.toFloat())
                                val tabIndex = (dragX / stepPx).toInt().coerceIn(0, tabCount - 1)
                                dragTargetTab = AppNavTab.values()[tabIndex]
                                change.consume()
                            }
                        )
                    }
            ) {
                // Universal Gooey Selection Bubble (BlurMaskFilter + ColorMatrix thresholding on reused Bitmap)
                val canvas = offscreenCanvas
                val bmp = offscreenBitmap
                if (canvas != null && bmp != null && containerWidth > 0 && containerHeight > 0) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cy = containerHeight / 2f
                        val r = bubbleRadiusPx

                        // Clear offscreen bitmap
                        canvas.drawColor(0, PorterDuff.Mode.CLEAR)

                        // Draw moving/leading bubble and trailing bubble
                        canvas.drawCircle(leadX, cy, r, blurPaint)
                        canvas.drawCircle(trailX, cy, r, blurPaint)

                        // Connecting liquid droplet bridge while in transit
                        if (abs(leadX - trailX) > 1f) {
                            val midX = (leadX + trailX) / 2f
                            val midR = r * 0.88f
                            canvas.drawCircle(midX, cy, midR, blurPaint)
                        }

                        // Render blurred and alpha-thresholded gooey bubble to screen
                        drawIntoCanvas { composeCanvas ->
                            composeCanvas.nativeCanvas.drawBitmap(bmp, 0f, 0f, thresholdPaint)
                        }

                        // Crisp specular ring when resting on selected tab
                        if (!isDragging && abs(leadX - trailX) < 2f) {
                            val ringColor = theme.primaryAccent.copy(alpha = if (isLight) 0.32f else 0.40f)
                            drawCircle(
                                color = ringColor,
                                radius = bubbleRadiusPx,
                                center = Offset(leadX, cy),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                            )
                        }
                    }
                }

                // Foreground Navigation Tabs with iOS Magnifier Icon Zoom & Label Fade
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentBubbleCenter = (leadX + trailX) / 2f

                    AppNavTab.values().forEach { tab ->
                        val isSelected = tab == selectedTab

                        // iOS Magnifier Lens feel: icon zooms up (1.0 -> ~1.18) as bubble center passes over it
                        val tabCenterX = if (tabWidthPx > 0f) (tab.ordinal + 0.5f) * tabWidthPx else 0f
                        val distFromBubble = abs(currentBubbleCenter - tabCenterX)
                        val proximity = if (tabWidthPx > 0f) {
                            (1f - (distFromBubble / (tabWidthPx * 0.95f))).coerceIn(0f, 1f)
                        } else 0f
                        val targetZoom = 1.0f + 0.18f * (proximity * proximity)

                        val iconScale by animateFloatAsState(
                            targetValue = targetZoom,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "navIconZoom_${tab.name}"
                        )

                        // Label only appears under the selected tab (clean fade in/out)
                        val labelAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0f,
                            animationSpec = spring(
                                dampingRatio = 0.85f,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "navLabelAlpha_${tab.name}"
                        )

                        val iconOffsetY by animateFloatAsState(
                            targetValue = if (isSelected) -2f else 0f,
                            animationSpec = spring(
                                dampingRatio = 0.85f,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "navIconOffsetY_${tab.name}"
                        )

                        val tintColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                theme.primaryAccent
                            } else {
                                if (isLight) Color(0xFF8E8E93) else Color(0xFFA0A0A5)
                            },
                            label = "navTint_${tab.name}"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (tab != currentTab) {
                                        if (vibrationEnabled) {
                                            VibrationHelper.tick(context)
                                        }
                                        currentOnTabSelected(tab)
                                    }
                                }
                                .testTag("nav_tab_${tab.name.lowercase()}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .offset(y = iconOffsetY.dp)
                                    .graphicsLayer {
                                        this.scaleX = iconScale
                                        this.scaleY = iconScale
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = tintColor,
                                    modifier = Modifier.size(21.dp)
                                )
                            }

                            if (labelAlpha > 0.01f) {
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tintColor.copy(alpha = labelAlpha),
                                    maxLines = 1,
                                    modifier = Modifier
                                        .padding(top = 1.dp)
                                        .graphicsLayer {
                                            this.alpha = labelAlpha
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
