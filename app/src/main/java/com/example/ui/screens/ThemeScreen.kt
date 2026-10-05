package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.CalculatorViewModel

@Composable
fun ThemeScreen(
    currentTheme: ThemeMode,
    viewModel: CalculatorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val themes = ThemeMode.selectableThemes

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 20.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (currentTheme.isLight) Color.White else Color(0x33FFFFFF))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = currentTheme.textPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Themes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = currentTheme.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(themes) { themeItem ->
                val isSelected = themeItem.name == currentTheme.name ||
                        (themeItem == ThemeMode.LIGHT && currentTheme.name == "AURORA")

                ThemePreviewCard(
                    themeItem = themeItem,
                    isSelected = isSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.setTheme(themeItem)
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemePreviewCard(
    themeItem: ThemeMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(26.dp)

    val icon: ImageVector = when (themeItem) {
        ThemeMode.LIGHT -> Icons.Default.LightMode
        ThemeMode.DARK -> Icons.Default.DarkMode
        ThemeMode.CYBER_NEON -> Icons.Default.Stream
        ThemeMode.NORDIC -> Icons.Default.FilterDrama
        ThemeMode.MIDNIGHT_LIGHT -> Icons.Default.NightsStay
        else -> Icons.Default.LightMode
    }

    val previewBg = themeItem.backgroundColors.first()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .shadow(
                elevation = if (isSelected) 10.dp else 4.dp,
                shape = cardShape,
                spotColor = if (isSelected) themeItem.primaryAccent.copy(alpha = 0.35f) else Color(0x14000000)
            )
            .clip(cardShape)
            .background(previewBg)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) themeItem.primaryAccent else Color(0x14000000),
                shape = cardShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .drawBehind {
                val w = size.width
                val h = size.height

                // Ambient glow for preview
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            themeItem.primaryAccent.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.7f, h * 0.35f),
                        radius = w * 0.65f
                    )
                )
            }
            .padding(16.dp)
            .testTag("theme_card_${themeItem.name.lowercase()}"),
        contentAlignment = Alignment.TopStart
    ) {
        // Selection Checkmark Badge
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(themeItem.primaryAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon Pill
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (themeItem.isLight) Color.White.copy(alpha = 0.85f) else Color(0x33FFFFFF)
                    )
                    .border(
                        1.dp,
                        if (themeItem.isLight) Color(0x12000000) else Color(0x22FFFFFF),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = themeItem.title,
                    tint = themeItem.primaryAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = themeItem.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeItem.textPrimary
                )
                Text(
                    text = if (themeItem.isLight) "Light System" else "Dark System",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = themeItem.textSecondary
                )
            }
        }
    }
}
