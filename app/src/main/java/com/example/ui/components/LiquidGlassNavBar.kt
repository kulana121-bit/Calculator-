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
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val soundEnabled = LocalSoundEnabled.current

    val currentTab by rememberUpdatedState(selectedTab)
    val currentOnTabSelected by rememberUpdatedState(onTabSelected)

    val isLandscape = androidx.compose.ui.platform.LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val outerPaddingHorizontal = if (isLandscape) 90.dp else 18.dp
    val outerPaddingBottom = if (isLandscape) 6.dp else 12.dp

    // Frosted glass capsule background colors
    val barBgBrush = Brush.verticalGradient(
        colors = if (isLight) {
            listOf(
                Color.White.copy(alpha = 0.92f),
                Color.White.copy(alpha = 0.74f)
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

    // Target center X for the bubble (maps 1:1 to selected tab center)
    val targetCenterX = if (isDragging && containerWidth > 0) {
        dragX.coerceIn(tabWidthPx * 0.5f, containerWidth - tabWidthPx * 0.5f)
    } else {
        (selectedTab.ordinal + 0.5f) * tabWidthPx
    }

    // Dual-spring physics: leadX rushes forward, trailX catches up smoothly
    val leadX by animateFloatAsState(
        targetValue = targetCenterX,
        animationSpec = spring(
            dampingRatio = if (isDragging) 0.88f else 0.78f,
            stiffness = if (isDragging) 750f else 480f
        ),
        label = "gooeyLeadX"
    )

    val trailX by animateFloatAsState(
        targetValue = targetCenterX,
        animationSpec = spring(
            dampingRatio = if (isDragging) 0.85f else 0.82f,
            stiffness = if (isDragging) 500f else 280f
        ),
        label = "gooeyTrailX"
    )

    // Accurate bubble dimensions: clean circle (radius ~22dp) fully within the 56dp bar
    val bubbleRadiusPx = with(density) { 22.dp.toPx() }
    val bubbleCenterYPx = with(density) { 23.dp.toPx() }

    // Controlled blur radius (12dp, NOT 24dp) to avoid any edge smearing
    val blurRadiusPx = with(density) { 12.dp.toPx() }

    // Reusable offscreen Bitmap and Canvas (zero allocation per frame, compatible with all API levels)
    var offscreenBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var offscreenCanvas by remember { mutableStateOf<AndroidCanvas?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            offscreenBitmap?.recycle()
            offscreenBitmap = null
            offscreenCanvas = null
        }
    }

    // Blur Paint for offscreen metaball rendering
    val blurPaint = remember(blurRadiusPx) {
        AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = AndroidPaint.Style.FILL
            maskFilter = BlurMaskFilter(blurRadiusPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    // Theme bubble colors
    val bubbleColor = if (isLight) {
        theme.primaryAccent.copy(alpha = 0.16f)
    } else {
        theme.primaryAccent.copy(alpha = 0.24f)
    }
    val ringColor = theme.primaryAccent.copy(alpha = if (isLight) 0.35f else 0.42f)

    // Alpha threshold paint for snapping the blurred gradient into a crisp liquid bridge during motion
    val thresholdPaint = remember(theme.primaryAccent) {
        AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            val r = theme.primaryAccent.red * 255f
            val g = theme.primaryAccent.green * 255f
            val b = theme.primaryAccent.blue * 255f

            // Snap alpha [120..135] to [0..255]
            val matrix = ColorMatrix(floatArrayOf(
                0f, 0f, 0f, 0f, r,
                0f, 0f, 0f, 0f, g,
                0f, 0f, 0f, 0f, b,
                0f, 0f, 0f, 18f, -2232f
            ))
            colorFilter = ColorMatrixColorFilter(matrix)
        }
    }

    val drawBitmapPaint = remember(bubbleColor) {
        AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            alpha = (bubbleColor.alpha * 255f).toInt()
        }
    }

    // Animation motion check: only run the gooey blur-threshold bridge while actively transitioning
    val isMoving = isDragging || abs(leadX - targetCenterX) > 1.0f || abs(trailX - targetCenterX) > 1.0f

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
                    shape = RoundedCornerShape(28.dp),
                    spotColor = shadowSpot,
                    ambientColor = shadowAmbient
                )
                .clip(RoundedCornerShape(28.dp))
                .background(barBgBrush)
                .border(
                    border = BorderStroke(1.dp, barBorderBrush),
                    shape = RoundedCornerShape(28.dp)
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
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            // Interactive Dock Container (Height: 56dp for ideal proportions and touch targets)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
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
                // SELECTION BUBBLE RENDERING:
                // When moving: draws fluid gooey merge using BlurMaskFilter + thresholding
                // When idle: draws EXACTLY ONE clean circle hugging the selected tab's icon
                if (containerWidth > 0 && containerHeight > 0) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (!isMoving) {
                            // IDLE STATE: Exactly ONE clean bubble on the selected tab
                            drawCircle(
                                color = bubbleColor,
                                radius = bubbleRadiusPx,
                                center = Offset(targetCenterX, bubbleCenterYPx)
                            )
                            drawCircle(
                                color = ringColor,
                                radius = bubbleRadiusPx,
                                center = Offset(targetCenterX, bubbleCenterYPx),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        } else {
                            // MOTION STATE: Gooey metaball bridge between moving positions
                            val canvas = offscreenCanvas
                            val bmp = offscreenBitmap
                            if (canvas != null && bmp != null) {
                                canvas.drawColor(0, PorterDuff.Mode.CLEAR)

                                // Draw leading bubble and trailing bubble
                                canvas.drawCircle(leadX, bubbleCenterYPx, bubbleRadiusPx, blurPaint)
                                canvas.drawCircle(trailX, bubbleCenterYPx, bubbleRadiusPx, blurPaint)

                                // Connective bridge droplet while stretching
                                if (abs(leadX - trailX) > 4.dp.toPx()) {
                                    val midX = (leadX + trailX) / 2f
                                    val midR = bubbleRadiusPx * 0.78f
                                    canvas.drawCircle(midX, bubbleCenterYPx, midR, blurPaint)
                                }

                                // Apply threshold filter to snap blurred edges into a crisp liquid contour
                                canvas.drawBitmap(bmp, 0f, 0f, thresholdPaint)

                                // Render the crisp gooey bubble into dock canvas with correct theme alpha
                                drawIntoCanvas { composeCanvas ->
                                    composeCanvas.nativeCanvas.drawBitmap(bmp, 0f, 0f, drawBitmapPaint)
                                }
                            }
                        }
                    }
                }

                // Foreground Navigation Tabs: Icon Centered Exactly on Bubble + iOS Magnifier Zoom & Label Fade
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentBubbleCenter = if (isMoving) (leadX + trailX) / 2f else targetCenterX

                    AppNavTab.values().forEach { tab ->
                        val isSelected = tab == selectedTab

                        // iOS Magnifier Lens feel: icon zooms up (1.0 -> ~1.18) as bubble center passes over it
                        val tabCenterX = if (tabWidthPx > 0f) (tab.ordinal + 0.5f) * tabWidthPx else 0f
                        val distFromBubble = abs(currentBubbleCenter - tabCenterX)
                        val proximity = if (tabWidthPx > 0f) {
                            (1f - (distFromBubble / (tabWidthPx * 0.90f))).coerceIn(0f, 1f)
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

                        val tintColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                theme.primaryAccent
                            } else {
                                if (isLight) Color(0xFF8E8E93) else Color(0xFFA0A0A5)
                            },
                            label = "navTint_${tab.name}"
                        )

                        // Each tab has a minimum 48dp touch target (fills full tab column width and dock height)
                        Box(
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
                                        if (soundEnabled) {
                                            com.example.util.SoundHelper.playClickSound(context)
                                        }
                                        currentOnTabSelected(tab)
                                    }
                                }
                                .testTag("nav_tab_${tab.name.lowercase()}"),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            // Icon is positioned with its center aligned with the bubble's center Y (23dp)
                            Box(
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .size(22.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = tintColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Text label sits cleanly below the icon, fading in only when selected
                            if (labelAlpha > 0.01f) {
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tintColor.copy(alpha = labelAlpha),
                                    maxLines = 1,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 3.dp)
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
