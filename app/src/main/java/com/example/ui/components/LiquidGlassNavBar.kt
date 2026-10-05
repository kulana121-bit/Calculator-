package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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

    val isLandscape = androidx.compose.ui.platform.LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val outerPaddingHorizontal = if (isLandscape) 90.dp else 16.dp
    val outerPaddingVertical = if (isLandscape) 4.dp else 10.dp
    
    val barBg = if (isLight) Color.White.copy(alpha = 0.95f) else theme.surfaceGlass
    val barBorder = if (isLight) Color(0x12000000) else Color(0x33FFFFFF)
    val shadowSpot = if (isLight) Color(0x1A000000) else Color(0x77000000)

    var containerWidth by remember { mutableIntStateOf(0) }
    val tabCount = AppNavTab.values().size

    // State for tracking swipe/drag interactions dynamically
    var isDragging by remember { mutableStateOf(false) }
    var dragX by remember { mutableFloatStateOf(0f) }

    // Precise inner width tracking for perfectly aligned tabs
    val tabWidthPx = if (containerWidth > 0) containerWidth.toFloat() / tabCount else 0f

    // Calculate active indicator target: tracks finger during drag, snaps to tab on release
    val targetIndicatorX = if (isDragging && containerWidth > 0) {
        (dragX - (tabWidthPx / 2f)).coerceIn(0f, (containerWidth - tabWidthPx).coerceAtLeast(0f))
    } else {
        selectedTab.ordinal * tabWidthPx
    }

    // High-performance, realistic spring physics for a modern glide feel
    val animatedIndicatorX by animateFloatAsState(
        targetValue = targetIndicatorX,
        animationSpec = spring(
            dampingRatio = if (isDragging) 0.9f else 0.78f, // Ultra-stable tracking during active dragging, smooth on snap
            stiffness = if (isDragging) 800f else 380f     // Instant feedback during drag, graceful on tab tap
        ),
        label = "navIndicatorX"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = outerPaddingHorizontal, vertical = outerPaddingVertical)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = shadowSpot,
                    ambientColor = Color(0x0F000000)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(barBg)
                .border(
                    border = BorderStroke(1.dp, barBorder),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Inner Box for measuring exact tab layout bounds after padding is applied
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp) // Explicit elegant height for a sleek dock bar
                    .onSizeChanged {
                        containerWidth = it.width
                    }
                    .pointerInput(containerWidth) {
                        if (containerWidth <= 0) return@pointerInput
                        val stepPx = containerWidth.toFloat() / tabCount
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                dragX = offset.x
                                val tabIndex = (offset.x / stepPx).toInt().coerceIn(0, tabCount - 1)
                                val targetTab = AppNavTab.values()[tabIndex]
                                if (targetTab != selectedTab) {
                                    VibrationHelper.tick(context)
                                    onTabSelected(targetTab)
                                }
                            },
                            onDragEnd = {
                                isDragging = false
                            },
                            onDragCancel = {
                                isDragging = false
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                dragX = (dragX + dragAmount).coerceIn(0f, containerWidth.toFloat())
                                val tabIndex = (dragX / stepPx).toInt().coerceIn(0, tabCount - 1)
                                val targetTab = AppNavTab.values()[tabIndex]
                                if (targetTab != selectedTab) {
                                    VibrationHelper.tick(context)
                                    onTabSelected(targetTab)
                                }
                                change.consume()
                            }
                        )
                    }
            ) {
                val tabWidthDp = with(density) { tabWidthPx.toDp() }
                val animatedIndicatorXDp = with(density) { animatedIndicatorX.toDp() }

                // 1. Sliding Liquid Glass Background Indicator Pill (Sleeker and centered vertically)
                if (containerWidth > 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = animatedIndicatorXDp)
                            .width(tabWidthDp)
                            .height(36.dp) // Sleeker, more refined capsule height
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isLight) theme.primaryAccent.copy(alpha = 0.12f)
                                else Color.White.copy(alpha = 0.08f)
                            )
                            .border(
                                width = 1.dp,
                                color = theme.primaryAccent.copy(alpha = 0.30f),
                                shape = RoundedCornerShape(18.dp)
                            )
                    )
                }

                // 2. Navigation Tabs Foreground Row
                Row(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppNavTab.values().forEach { tab ->
                        val isSelected = tab == selectedTab

                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "navIconScale_${tab.name}"
                        )

                        val tintColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                theme.primaryAccent
                            } else {
                                if (isLight) Color(0xFF8E8E93) else Color(0xFF8E8E93)
                            },
                            label = "navTint_${tab.name}"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (!isSelected) {
                                        VibrationHelper.tick(context)
                                        onTabSelected(tab)
                                    }
                                }
                                .testTag("nav_tab_${tab.name.lowercase()}")
                        ) {
                            Box(
                                modifier = Modifier
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
                                    modifier = Modifier.size(20.dp) // Perfect size
                                )
                            }

                            Text(
                                text = tab.title,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = tintColor,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
