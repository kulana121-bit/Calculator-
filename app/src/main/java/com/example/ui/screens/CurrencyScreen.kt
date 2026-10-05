package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.CurrencyData
import com.example.ui.components.CalcButtonType
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.CalculatorUiState
import com.example.ui.viewmodel.CalculatorViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreen(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val theme = state.theme
    val haptic = LocalHapticFeedback.current
    val fromInfo = CurrencyData.getCurrency(state.fromCurrency)
    val toInfo = CurrencyData.getCurrency(state.toCurrency)

    var currencyPickerTarget by remember { mutableStateOf<String?>(null) }
    var currencySearchQuery by remember { mutableStateOf("") }
    var swapRotation by remember { mutableStateOf(0f) }
    var selectedTimeframe by remember { mutableStateOf("1M") }

    val rotationAngle by animateFloatAsState(
        targetValue = swapRotation,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "swapRotation"
    )

    val formatCurrency = remember {
        val symbols = DecimalFormatSymbols(Locale.US)
        DecimalFormat("#,##0.00", symbols)
    }

    val rateSummary = remember(state.fromCurrency, state.toCurrency, state.currencyRates) {
        val fromRate = state.currencyRates[state.fromCurrency] ?: 1.0
        val toRate = state.currencyRates[state.toCurrency] ?: 1.0
        val relativeRate = if (fromRate > 0) toRate / fromRate else 1.0
        "1 ${state.fromCurrency} = ${String.format(Locale.US, "%.3f", relativeRate)} ${state.toCurrency}"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .padding(top = 8.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ----------------------------------------------------
        // Top Section: Title "Currency" + Refresh Action
        // ----------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Currency",
                color = theme.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.refreshCurrencyRates()
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (theme.isLight) Color.White else Color(0x33FFFFFF))
                    .border(1.dp, if (theme.isLight) Color(0x0F000000) else Color(0x22FFFFFF), CircleShape)
                    .testTag("refresh_currency_btn")
            ) {
                if (state.isCurrencyLoading) {
                    CircularProgressIndicator(
                        color = theme.primaryAccent,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = theme.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ----------------------------------------------------
        // Currency Cards Section:
        // Card 1: From Currency (Flag, Code, Input number)
        // Floating circular swap button
        // Card 2: To Currency (Flag, Code, Converted result number)
        // ----------------------------------------------------
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card 1: FROM
            LiquidGlassCard(
                theme = theme,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { currencyPickerTarget = "FROM" }
                    .testTag("currency_from_card"),
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
                        Text(text = fromInfo.flag, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = fromInfo.code,
                                color = theme.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = fromInfo.name,
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = theme.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = if (state.currencyAmount.isEmpty()) "0" else state.currencyAmount,
                        color = theme.textPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            // Floating circular white glass swap button with soft shadow
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
                        swapRotation += 180f
                        viewModel.swapCurrencies()
                    }
                    .testTag("swap_currency_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Swap",
                    tint = theme.primaryAccent,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle)
                )
            }

            // Card 2: TO
            LiquidGlassCard(
                theme = theme,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { currencyPickerTarget = "TO" }
                    .testTag("currency_to_card"),
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
                        Text(text = toInfo.flag, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = toInfo.code,
                                color = theme.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = toInfo.name,
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = theme.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = formatCurrency.format(state.convertedCurrencyAmount),
                        color = theme.primaryAccent,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }
            }
        }

        // ----------------------------------------------------
        // Minimalist Orange Line Chart:
        // - Timeframe selector: 1D, 1W, 1M, 1Y
        // - Rate summary: "1 USD = 83.295 INR"
        // - Smooth curved line chart with glowing gradient fill
        // ----------------------------------------------------
        LiquidGlassCard(
            theme = theme,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = rateSummary,
                            color = theme.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Live rates • Updated today",
                            color = theme.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    // Timeframe pills: 1D, 1W, 1M, 1Y
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (theme.isLight) Color(0xFFF3F2EE) else Color(0x22FFFFFF))
                            .padding(2.dp)
                    ) {
                        listOf("1D", "1W", "1M", "1Y").forEach { tf ->
                            val isSelected = tf == selectedTimeframe
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) theme.primaryAccent else Color.Transparent
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedTimeframe = tf
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tf,
                                    color = if (isSelected) Color.White else theme.textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Smooth minimalist orange line chart
                val chartPoints = remember(selectedTimeframe) {
                    when (selectedTimeframe) {
                        "1D" -> listOf(0.40f, 0.45f, 0.42f, 0.50f, 0.48f, 0.55f, 0.52f, 0.60f)
                        "1W" -> listOf(0.35f, 0.42f, 0.38f, 0.52f, 0.49f, 0.62f, 0.58f, 0.68f)
                        "1M" -> listOf(0.30f, 0.48f, 0.45f, 0.58f, 0.52f, 0.70f, 0.65f, 0.75f)
                        else -> listOf(0.20f, 0.38f, 0.32f, 0.50f, 0.46f, 0.68f, 0.62f, 0.82f)
                    }
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val n = chartPoints.size
                    if (n < 2) return@Canvas

                    val stepX = w / (n - 1)
                    val strokePath = Path()
                    val fillPath = Path()

                    val startY = h * (1f - chartPoints[0])
                    strokePath.moveTo(0f, startY)
                    fillPath.moveTo(0f, h)
                    fillPath.lineTo(0f, startY)

                    for (i in 1 until n) {
                        val prevX = (i - 1) * stepX
                        val prevY = h * (1f - chartPoints[i - 1])
                        val curX = i * stepX
                        val curY = h * (1f - chartPoints[i])

                        val cX = (prevX + curX) / 2
                        strokePath.cubicTo(cX, prevY, cX, curY, curX, curY)
                        fillPath.cubicTo(cX, prevY, cX, curY, curX, curY)
                    }

                    fillPath.lineTo(w, h)
                    fillPath.close()

                    // Glowing gradient fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF7A00).copy(alpha = 0.28f),
                                Color(0xFFFF7A00).copy(alpha = 0.02f)
                            )
                        )
                    )

                    // Line stroke
                    drawPath(
                        path = strokePath,
                        color = Color(0xFFFF7A00),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        // ----------------------------------------------------
        // Numeric Keypad with Soft White Buttons
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
                        onClick = { viewModel.onCurrencyDigit(digit) },
                        theme = theme,
                        type = CalcButtonType.NUMBER,
                        modifier = Modifier.weight(1f).height(52.dp)
                    )
                }
                LiquidGlassButton(
                    text = "⌫",
                    onClick = { viewModel.onCurrencyBackspace() },
                    theme = theme,
                    type = CalcButtonType.FUNCTION,
                    fontSize = 20.sp,
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    modifier = Modifier.weight(1f).height(52.dp)
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
                        onClick = { viewModel.onCurrencyDigit(digit) },
                        theme = theme,
                        type = CalcButtonType.NUMBER,
                        modifier = Modifier.weight(1f).height(52.dp)
                    )
                }
                LiquidGlassButton(
                    text = "AC",
                    onClick = { viewModel.onCurrencyClear() },
                    theme = theme,
                    type = CalcButtonType.FUNCTION,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f).height(52.dp)
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
                        onClick = { viewModel.onCurrencyDigit(digit) },
                        theme = theme,
                        type = CalcButtonType.NUMBER,
                        modifier = Modifier.weight(1f).height(52.dp)
                    )
                }
                LiquidGlassButton(
                    text = "0",
                    onClick = { viewModel.onCurrencyDigit("0") },
                    theme = theme,
                    type = CalcButtonType.NUMBER,
                    modifier = Modifier.weight(1f).height(52.dp)
                )
            }

            // Row 4: ., 00, Swap, Done
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassButton(
                    text = ".",
                    onClick = { viewModel.onCurrencyDot() },
                    theme = theme,
                    type = CalcButtonType.NUMBER,
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f).height(52.dp)
                )
                LiquidGlassButton(
                    text = "00",
                    onClick = { viewModel.onCurrencyDigit("00") },
                    theme = theme,
                    type = CalcButtonType.NUMBER,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f).height(52.dp)
                )
                LiquidGlassButton(
                    text = "⇄",
                    onClick = {
                        swapRotation += 180f
                        viewModel.swapCurrencies()
                    },
                    theme = theme,
                    type = CalcButtonType.OPERATOR,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f).height(52.dp)
                )
                LiquidGlassButton(
                    text = "=",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.refreshCurrencyRates()
                    },
                    theme = theme,
                    type = CalcButtonType.EQUALS,
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f).height(52.dp)
                )
            }
        }
    }

    // ----------------------------------------------------
    // Currency Picker Bottom Sheet
    // ----------------------------------------------------
    if (currencyPickerTarget != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val isPickingFrom = currencyPickerTarget == "FROM"

        ModalBottomSheet(
            onDismissRequest = { currencyPickerTarget = null },
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
                    text = if (isPickingFrom) "Select Base Currency" else "Select Target Currency",
                    color = theme.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = currencySearchQuery,
                    onValueChange = { currencySearchQuery = it },
                    placeholder = { Text("Search currency or country...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = theme.textSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.primaryAccent,
                        unfocusedBorderColor = if (theme.isLight) Color(0x18000000) else Color(0x33FFFFFF)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                val filteredCurrencies = remember(currencySearchQuery) {
                    if (currencySearchQuery.isBlank()) CurrencyData.SUPPORTED_CURRENCIES
                    else {
                        CurrencyData.SUPPORTED_CURRENCIES.filter {
                            it.code.contains(currencySearchQuery, ignoreCase = true) ||
                                    it.name.contains(currencySearchQuery, ignoreCase = true)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCurrencies, key = { it.code }) { item ->
                        val isSelected = if (isPickingFrom) item.code == state.fromCurrency
                        else item.code == state.toCurrency

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
                                    if (isPickingFrom) viewModel.setFromCurrency(item.code)
                                    else viewModel.setToCurrency(item.code)
                                    currencyPickerTarget = null
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = item.flag, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.code,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = item.name,
                                        color = theme.textSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

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
