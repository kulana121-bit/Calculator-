package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.CalculatorUiState
import com.example.ui.viewmodel.CalculatorViewModel

@Composable
fun SettingsScreen(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = state.theme
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (theme.isLight) Color.White else Color(0x33FFFFFF))
                    .testTag("settings_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Settings",
                color = theme.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Preference Card 1: Appearance & Theme
        LiquidGlassCard(
            theme = theme,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingsNavigationRow(
                icon = Icons.Default.Palette,
                iconColor = Color(0xFFF97316),
                title = "Themes & Colors",
                subtitle = state.theme.title,
                theme = theme,
                onClick = { viewModel.openThemes() },
                testTag = "settings_themes"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preference Card 2: Feedback & Sounds
        LiquidGlassCard(
            theme = theme,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                SettingsToggleRow(
                    icon = Icons.Default.TouchApp,
                    iconColor = Color(0xFF10B981),
                    title = "Haptic Feedback",
                    checked = state.hapticFeedbackEnabled,
                    onCheckedChange = { viewModel.toggleHapticFeedback() },
                    theme = theme,
                    testTag = "settings_haptic"
                )

                SettingsDivider(theme)

                SettingsToggleRow(
                    icon = Icons.Default.VolumeUp,
                    iconColor = Color(0xFF3B82F6),
                    title = "Sound",
                    checked = state.soundEnabled,
                    onCheckedChange = { viewModel.toggleSound() },
                    theme = theme,
                    testTag = "settings_sound"
                )

                SettingsDivider(theme)

                SettingsToggleRow(
                    icon = Icons.Default.Vibration,
                    iconColor = Color(0xFF8B5CF6),
                    title = "Vibration",
                    checked = state.vibrationEnabled,
                    onCheckedChange = { viewModel.toggleVibration() },
                    theme = theme,
                    testTag = "settings_vibration"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preference Card 4: Language
        LiquidGlassCard(
            theme = theme,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingsNavigationRow(
                icon = Icons.Default.Language,
                iconColor = Color(0xFF6366F1),
                title = "Language",
                subtitle = state.currentLanguage,
                theme = theme,
                onClick = { showLanguageDialog = true },
                testTag = "settings_language"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preference Card 5: Community & Support
        LiquidGlassCard(
            theme = theme,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                SettingsNavigationRow(
                    icon = Icons.Default.StarRate,
                    iconColor = Color(0xFFF59E0B),
                    title = "Rate App",
                    subtitle = null,
                    theme = theme,
                    onClick = { showRateDialog = true },
                    testTag = "settings_rate"
                )

                SettingsDivider(theme)

                SettingsNavigationRow(
                    icon = Icons.Default.Share,
                    iconColor = Color(0xFF06B6D4),
                    title = "Share App",
                    subtitle = null,
                    theme = theme,
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Check out Calculator - a clean, premium calculator app with unit & currency converter and interactive home screen widget!"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Calculator"))
                    },
                    testTag = "settings_share"
                )

                SettingsDivider(theme)

                SettingsNavigationRow(
                    icon = Icons.Default.Info,
                    iconColor = Color(0xFF8B5CF6),
                    title = "About",
                    subtitle = "v1.0.0",
                    theme = theme,
                    onClick = { viewModel.openAbout() },
                    testTag = "settings_about"
                )
            }
        }
    }

    // ----------------------------------------------------
    // Widget Sticky Notes Selector Dialog
    // ----------------------------------------------------
    if (showRateDialog) {
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text("Enjoying Calculator?", fontWeight = FontWeight.Bold) },
            text = { Text("Thank you for using Calculator! We strive to deliver an Apple-grade calculation experience.") },
            confirmButton = {
                TextButton(onClick = { showRateDialog = false }) {
                    Text("Rate 5 Stars ⭐", color = theme.primaryAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateDialog = false }) {
                    Text("Maybe Later", color = theme.textSecondary)
                }
            },
            containerColor = if (theme.isLight) Color.White else Color(0xFF242428),
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showLanguageDialog) {
        val languages = listOf("English", "Spanish", "French", "German", "Japanese", "Hindi")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lang, fontSize = 16.sp, color = theme.textPrimary)
                            if (lang == state.currentLanguage) {
                                Text("✓", color = theme.primaryAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close", color = theme.primaryAccent)
                }
            },
            containerColor = if (theme.isLight) Color.White else Color(0xFF242428),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String?,
    theme: ThemeMode,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    color = theme.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = theme.textSecondary.copy(alpha = 0.4f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    theme: ThemeMode,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                color = theme.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = theme.primaryAccent,
                uncheckedThumbColor = Color(0xFFA1A1AA),
                uncheckedTrackColor = if (theme.isLight) Color(0xFFE4E4E7) else Color(0x33FFFFFF)
            )
        )
    }
}

@Composable
private fun SettingsDivider(theme: ThemeMode) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 50.dp)
            .height(1.dp)
            .background(if (theme.isLight) Color(0x0D000000) else Color(0x1AFFFFFF))
    )
}
