package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.UnitCategory
import com.example.engine.UnitConversionEngine
import com.example.engine.UnitItem
import com.example.ui.components.CalcButtonType
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.CalculatorUiState
import com.example.ui.viewmodel.CalculatorViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val theme = state.theme
    val haptic = LocalHapticFeedback.current
    var unitPickerTarget by remember { mutableStateOf<String?>(null) }
    val categoryScrollState = rememberScrollState()

    val quickReferenceList: List<Triple<String, String, UnitItem>> = remember(state.unitCategory, state.unitInputValue, state.fromUnit) {
        val inputVal = state.unitInputValue.toDoubleOrNull() ?: 1.0
        val units = UnitConversionEngine.getUnitsForCategory(state.unitCategory)

        units.filter { it != state.fromUnit && it != state.toUnit }.take(3).map { unit ->
            val converted = UnitConversionEngine.convert(inputVal, state.fromUnit, unit)
            val formatted = DecimalFormat("#,##0.00##").format(converted)
            Triple(formatted, "${unit.name} (${unit.symbol})", unit)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .padding(top = 8.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ----------------------------------------------------
        // Top Section: Title "Units"
        // ----------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Units",
                color = theme.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // ----------------------------------------------------
        // Category Selector Pills: Length (active orange), Area, Volume, Weight, Speed, Temperature
        // ----------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(categoryScrollState)
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnitConversionEngine.CATEGORIES.forEach { category ->
                val isSelected = category == state.unitCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) theme.primaryAccent
                            else if (theme.isLight) Color.White else Color(0x22FFFFFF)
                        )
                        .border(
                            1.dp,
                            if (isSelected) theme.primaryAccent
                            else if (theme.isLight) Color(0x0F000000) else Color(0x22FFFFFF),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.selectUnitCategory(category)
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("unit_cat_${category.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.title,
                        color = if (isSelected) Color.White else theme.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // ----------------------------------------------------
        // Conversion Cards:
        // Input Card: "1", dropdown "Kilometer (km) ▾"
        // Floating circular swap button (⇄)
        // Output Card: "1,000", dropdown "Meter (m) ▾"
        // ----------------------------------------------------
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Input Card
            LiquidGlassCard(
                theme = theme,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { unitPickerTarget = "FROM" }
                    .testTag("unit_from_card"),
                shape = RoundedCornerShape(22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (theme.isLight) Color(0xFFF6F5F2) else Color(0x22FFFFFF))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${state.fromUnit.name} (${state.fromUnit.symbol})",
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = theme.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = if (state.unitInputValue.isEmpty()) "0" else state.unitInputValue,
                        color = theme.textPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            // Floating Circular Swap Button (⇄)
            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .size(44.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = Color(0x22000000)
                    )
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0x12000000), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.swapUnits()
                    }
                    .testTag("swap_units_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Swap units",
                    tint = theme.primaryAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Output Card
            LiquidGlassCard(
                theme = theme,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { unitPickerTarget = "TO" }
                    .testTag("unit_to_card"),
                shape = RoundedCornerShape(22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (theme.isLight) Color(0xFFF6F5F2) else Color(0x22FFFFFF))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${state.toUnit.name} (${state.toUnit.symbol})",
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = theme.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = state.unitOutputValue,
                        color = theme.primaryAccent,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }
            }
        }

        // ----------------------------------------------------
        // Quick Reference Conversion List inside a rounded glass card
        // (Matching Screen 4 of the reference mockup)
        // ----------------------------------------------------
        if (quickReferenceList.isNotEmpty()) {
            LiquidGlassCard(
                theme = theme,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    for (index in quickReferenceList.indices) {
                        val item = quickReferenceList[index]
                        val valueStr = item.first
                        val labelStr = item.second
                        val targetUnit = item.third

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setToUnit(targetUnit)
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = valueStr,
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = labelStr,
                                    color = theme.textSecondary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = theme.textSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        if (index < quickReferenceList.size - 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(if (theme.isLight) Color(0x0A000000) else Color(0x14FFFFFF))
                            )
                        }
                    }
                }
            }
        }

        // ----------------------------------------------------
        // Numeric Keypad
        // ----------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: 7, 8, 9, ⌫
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("7", "8", "9").forEach { digit ->
                    LiquidGlassButton(
                        text = digit,
                        onClick = { viewModel.onUnitDigit(digit) },
                        theme = theme,
                        type = CalcButtonType.NUMBER,
                        modifier = Modifier.weight(1f).height(50.dp)
                    )
                }
                LiquidGlassButton(
                    text = "⌫",
                    onClick = { viewModel.onUnitBackspace() },
                    theme = theme,
                    type = CalcButtonType.FUNCTION,
                    fontSize = 20.sp,
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    modifier = Modifier.weight(1f).height(50.dp)
                )
            }

            // Row 2: 4, 5, 6, AC
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("4", "5", "6").forEach { digit ->
                    LiquidGlassButton(
                        text = digit,
                        onClick = { viewModel.onUnitDigit(digit) },
                        theme = theme,
                        type = CalcButtonType.NUMBER,
                        modifier = Modifier.weight(1f).height(50.dp)
                    )
                }
                LiquidGlassButton(
                    text = "AC",
                    onClick = { viewModel.onUnitClear() },
                    theme = theme,
                    type = CalcButtonType.FUNCTION,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f).height(50.dp)
                )
            }

            // Row 3: 1, 2, 3, 0
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("1", "2", "3").forEach { digit ->
                    LiquidGlassButton(
                        text = digit,
                        onClick = { viewModel.onUnitDigit(digit) },
                        theme = theme,
                        type = CalcButtonType.NUMBER,
                        modifier = Modifier.weight(1f).height(50.dp)
                    )
                }
                LiquidGlassButton(
                    text = "0",
                    onClick = { viewModel.onUnitDigit("0") },
                    theme = theme,
                    type = CalcButtonType.NUMBER,
                    modifier = Modifier.weight(1f).height(50.dp)
                )
            }

            // Row 4: ., 00, +/-, Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassButton(
                    text = ".",
                    onClick = { viewModel.onUnitDot() },
                    theme = theme,
                    type = CalcButtonType.NUMBER,
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f).height(50.dp)
                )
                LiquidGlassButton(
                    text = "00",
                    onClick = { viewModel.onUnitDigit("00") },
                    theme = theme,
                    type = CalcButtonType.NUMBER,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f).height(50.dp)
                )
                LiquidGlassButton(
                    text = "+/-",
                    onClick = { viewModel.onUnitNegate() },
                    theme = theme,
                    type = CalcButtonType.FUNCTION,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f).height(50.dp)
                )
                LiquidGlassButton(
                    text = "Copy",
                    onClick = {
                        viewModel.copyToClipboard(
                            "${state.unitInputValue} ${state.fromUnit.symbol} = ${state.unitOutputValue} ${state.toUnit.symbol}"
                        )
                    },
                    theme = theme,
                    type = CalcButtonType.EQUALS,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f).height(50.dp)
                )
            }
        }
    }

    // ----------------------------------------------------
    // Unit Picker Bottom Sheet
    // ----------------------------------------------------
    if (unitPickerTarget != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val isFrom = unitPickerTarget == "FROM"
        val units = UnitConversionEngine.getUnitsForCategory(state.unitCategory)

        ModalBottomSheet(
            onDismissRequest = { unitPickerTarget = null },
            sheetState = sheetState,
            containerColor = if (theme.isLight) Color(0xFFFBF9F6) else Color(0xFF1E1E22),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (isFrom) "Select Source Unit" else "Select Target Unit",
                    color = theme.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(units) { item ->
                        val isSelected = if (isFrom) item == state.fromUnit else item == state.toUnit

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) theme.primaryAccent.copy(alpha = 0.15f)
                                    else if (theme.isLight) Color.White else Color(0x1AFFFFFF)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) theme.primaryAccent else Color.Transparent,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    if (isFrom) viewModel.setFromUnit(item)
                                    else viewModel.setToUnit(item)
                                    unitPickerTarget = null
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.name} (${item.symbol})",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) theme.primaryAccent else theme.textPrimary,
                                fontSize = 16.sp
                            )

                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    color = theme.primaryAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
