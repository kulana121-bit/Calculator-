package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.ThemeMode

@Composable
fun AboutScreen(
    theme: ThemeMode,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dialogTitle by remember { mutableStateOf<String?>(null) }
    var dialogContent by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (theme.isLight) Color.White else Color(0x33FFFFFF))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Floating Calculator Logo
        Box(
            modifier = Modifier
                .size(108.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = Color(0x2BFF7A00),
                    ambientColor = Color(0x12000000)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = Color(0x1AFFFFFF),
                    shape = RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_calculator_asset),
                contentDescription = "Calculator Logo",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Calculator",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "v1.0.0",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = theme.textSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Simple Tools for a Smarter You",
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = theme.textSecondary
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Legal and Info Cards
        LiquidGlassCard(
            theme = theme,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                AboutActionRow(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    theme = theme,
                    onClick = {
                        dialogTitle = "Privacy Policy"
                        dialogContent = "Calculator values your privacy. All calculations, history records, and conversion preferences are stored 100% locally on your device. No user calculation data is ever collected, monitored, or transmitted to third-party tracking servers."
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (theme.isLight) Color(0x0A000000) else Color(0x14FFFFFF))
                )

                AboutActionRow(
                    icon = Icons.Default.Description,
                    title = "Terms of Service",
                    theme = theme,
                    onClick = {
                        dialogTitle = "Terms of Service"
                        dialogContent = "Calculator is provided as a utility application for mathematical calculations, unit transformations, and currency conversions. Exchange rates are provided for reference purposes."
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (theme.isLight) Color(0x0A000000) else Color(0x14FFFFFF))
                )

                AboutActionRow(
                    icon = Icons.Default.Code,
                    title = "Open Source Licenses",
                    theme = theme,
                    onClick = {
                        dialogTitle = "Open Source Licenses"
                        dialogContent = "Built using Android Jetpack Compose, Kotlin Coroutines, Room Database, Material Design 3, and Android Architecture Components under the Apache 2.0 License."
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Motivational Card from Screen 9 of the mockup
        LiquidGlassCard(
            theme = theme,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Small Steps\nBig Calculations",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(theme.primaryAccent)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Calculate A Brighter Tomorrow",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Made with ❤️ for everyone",
            fontSize = 13.sp,
            color = theme.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (dialogTitle != null && dialogContent != null) {
        AlertDialog(
            onDismissRequest = {
                dialogTitle = null
                dialogContent = null
            },
            title = { Text(dialogTitle ?: "", fontWeight = FontWeight.Bold) },
            text = { Text(dialogContent ?: "", fontSize = 14.sp, lineHeight = 20.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        dialogTitle = null
                        dialogContent = null
                    }
                ) {
                    Text("Close", color = theme.primaryAccent, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun AboutActionRow(
    icon: ImageVector,
    title: String,
    theme: ThemeMode,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 14.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.primaryAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = theme.primaryAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = theme.textPrimary
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = theme.textSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(13.dp)
        )
    }
}
