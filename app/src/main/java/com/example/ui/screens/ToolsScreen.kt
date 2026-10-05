package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DateCalculatorEngine
import com.example.ui.components.CalcButtonType
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.CalculatorUiState
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.ui.viewmodel.DateMode
import com.example.ui.viewmodel.PercentMode
import com.example.ui.viewmodel.ToolType
import java.util.Calendar
import java.util.Locale

private data class ToolCardItem(
    val type: ToolType,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color
)

private val TOOLS_LIST = listOf(
    ToolCardItem(
        type = ToolType.PERCENTAGE,
        title = "Percentage",
        subtitle = "Calculate %, changes & values",
        icon = Icons.Default.Percent,
        iconBg = Color(0xFFE0F2FE),
        iconTint = Color(0xFF0284C7)
    ),
    ToolCardItem(
        type = ToolType.GST,
        title = "GST Calculator",
        subtitle = "Add or remove GST rates",
        icon = Icons.Default.ReceiptLong,
        iconBg = Color(0xFFFFEDD5),
        iconTint = Color(0xFFEA580C)
    ),
    ToolCardItem(
        type = ToolType.LOAN_EMI,
        title = "Loan EMI",
        subtitle = "Monthly payment & interest",
        icon = Icons.Default.AccountBalance,
        iconBg = Color(0xFFEDE9FE),
        iconTint = Color(0xFF7C3AED)
    ),
    ToolCardItem(
        type = ToolType.TIP,
        title = "Tip Calculator",
        subtitle = "Split bills & tip amounts",
        icon = Icons.Default.Restaurant,
        iconBg = Color(0xFFFCE7F3),
        iconTint = Color(0xFFDB2777)
    ),
    ToolCardItem(
        type = ToolType.AGE,
        title = "Age Calculator",
        subtitle = "Exact age & birthday countdown",
        icon = Icons.Default.Cake,
        iconBg = Color(0xFFCCFBF1),
        iconTint = Color(0xFF0D9488)
    ),
    ToolCardItem(
        type = ToolType.DATE,
        title = "Date Calculator",
        subtitle = "Date diff, add & subtract time",
        icon = Icons.Default.CalendarMonth,
        iconBg = Color(0xFFE0E7FF),
        iconTint = Color(0xFF4F46E5)
    ),
    ToolCardItem(
        type = ToolType.DISCOUNT,
        title = "Discount",
        subtitle = "Sales discount & savings",
        icon = Icons.Default.LocalOffer,
        iconBg = Color(0xFFFFE4E6),
        iconTint = Color(0xFFE11D48)
    ),
    ToolCardItem(
        type = ToolType.BMI,
        title = "BMI Calculator",
        subtitle = "Body mass index & health",
        icon = Icons.Default.FitnessCenter,
        iconBg = Color(0xFFDCFCE7),
        iconTint = Color(0xFF16A34A)
    )
)

@Composable
fun ToolsScreen(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val theme = state.theme
    val haptic = LocalHapticFeedback.current
    var selectedTool by remember { mutableStateOf<ToolType?>(null) }

    AnimatedContent(
        targetState = selectedTool,
        transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut())
            } else {
                (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { it / 3 } + fadeOut())
            }
        },
        label = "ToolDetailTransition",
        modifier = modifier.fillMaxSize()
    ) { activeTool ->
        if (activeTool == null) {
            // ----------------------------------------------------
            // Main Tools Grid Screen
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
                    .padding(top = 8.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Tools",
                    color = theme.textPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(TOOLS_LIST, key = { it.type.name }) { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 6.dp,
                                    shape = RoundedCornerShape(22.dp),
                                    spotColor = Color(0x18000000)
                                )
                                .clip(RoundedCornerShape(22.dp))
                                .background(if (theme.isLight) Color.White else Color(0x33FFFFFF))
                                .border(
                                    1.dp,
                                    if (theme.isLight) Color(0x0F000000) else Color(0x22FFFFFF),
                                    RoundedCornerShape(22.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedTool = item.type
                                }
                                .padding(16.dp)
                                .testTag("tool_card_${item.type.name.lowercase()}")
                        ) {
                            Column {
                                // Small colorful icon with soft background
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(item.iconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = item.iconTint,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = item.title,
                                    color = theme.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = item.subtitle,
                                    color = theme.textSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // ----------------------------------------------------
            // Active Tool Detailed View
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
                    .padding(top = 8.dp, bottom = 8.dp)
            ) {
                // Header with Back Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedTool = null
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (theme.isLight) Color.White else Color(0x33FFFFFF))
                            .border(1.dp, if (theme.isLight) Color(0x0F000000) else Color(0x22FFFFFF), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = theme.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = activeTool.title,
                        color = theme.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Tool Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (activeTool) {
                        ToolType.PERCENTAGE -> PercentageToolView(state, viewModel, theme)
                        ToolType.GST -> GstToolView(state, viewModel, theme)
                        ToolType.LOAN_EMI -> LoanEmiToolView(state, viewModel, theme)
                        ToolType.TIP -> TipToolView(state, viewModel, theme)
                        ToolType.AGE -> AgeToolView(state, viewModel, theme)
                        ToolType.DATE -> DateToolView(state, viewModel, theme)
                        ToolType.DISCOUNT -> DiscountToolView(state, viewModel, theme)
                        ToolType.BMI -> BmiToolView(state, viewModel, theme)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 1. Percentage Tool View
// ----------------------------------------------------
@Composable
private fun PercentageToolView(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    theme: ThemeMode
) {
    var activeField by remember { mutableStateOf("A") }

    LiquidGlassCard(theme = theme, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = "Result", color = theme.textSecondary, fontSize = 12.sp)
            Text(
                text = if (state.percentResult.isBlank()) "0" else state.percentResult,
                color = theme.primaryAccent,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(
            PercentMode.PERCENT_OF to "X% of Y",
            PercentMode.PART_OF to "X is % of Y",
            PercentMode.PERCENT_CHANGE to "% Change"
        ).forEach { (mode, label) ->
            val isSelected = state.percentMode == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) theme.primaryAccent else if (theme.isLight) Color.White else Color(0x22FFFFFF))
                    .clickable { viewModel.setPercentMode(mode) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else theme.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ToolNumericDisplayField(
            label = "Value A (X)",
            value = state.percentValA,
            isSelected = activeField == "A",
            onClick = { activeField = "A" },
            theme = theme,
            modifier = Modifier.weight(1f)
        )
        ToolNumericDisplayField(
            label = "Value B (Y)",
            value = state.percentValB,
            isSelected = activeField == "B",
            onClick = { activeField = "B" },
            theme = theme,
            modifier = Modifier.weight(1f)
        )
    }

    ToolCalculatorKeypad(
        theme = theme,
        onDigit = { digit ->
            if (activeField == "A") {
                viewModel.setPercentInputs(handleDigitInput(state.percentValA, digit), state.percentValB)
            } else {
                viewModel.setPercentInputs(state.percentValA, handleDigitInput(state.percentValB, digit))
            }
        },
        onDot = {
            if (activeField == "A") {
                viewModel.setPercentInputs(handleDotInput(state.percentValA), state.percentValB)
            } else {
                viewModel.setPercentInputs(state.percentValA, handleDotInput(state.percentValB))
            }
        },
        onBackspace = {
            if (activeField == "A") {
                viewModel.setPercentInputs(handleBackspace(state.percentValA), state.percentValB)
            } else {
                viewModel.setPercentInputs(state.percentValA, handleBackspace(state.percentValB))
            }
        },
        onClear = {
            if (activeField == "A") {
                viewModel.setPercentInputs("", state.percentValB)
            } else {
                viewModel.setPercentInputs(state.percentValA, "")
            }
        },
        onNext = {
            activeField = if (activeField == "A") "B" else "A"
        }
    )
}

// ----------------------------------------------------
// 2. GST Tool View
// ----------------------------------------------------
@Composable
private fun GstToolView(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    theme: ThemeMode
) {
    LiquidGlassCard(theme = theme, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Net Amount", color = theme.textSecondary, fontSize = 12.sp)
                    Text("$${if (state.gstAmount.isBlank()) "0" else state.gstAmount}", color = theme.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("GST Tax (${state.gstRate.toInt()}%)", color = theme.textSecondary, fontSize = 12.sp)
                    Text("$${String.format(Locale.US, "%.2f", state.gstTax)}", color = theme.primaryAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Total Amount", color = theme.textSecondary, fontSize = 12.sp)
            Text(
                text = "$${String.format(Locale.US, "%.2f", state.gstTotal)}",
                color = theme.primaryAccent,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    ToolNumericDisplayField(
        label = "Original Amount ($)",
        value = state.gstAmount,
        isSelected = true,
        onClick = {},
        theme = theme,
        modifier = Modifier.fillMaxWidth()
    )

    Text("Select GST Rate", color = theme.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(5.0, 12.0, 18.0, 28.0).forEach { rate ->
            val isSel = state.gstRate == rate
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSel) theme.primaryAccent else if (theme.isLight) Color.White else Color(0x22FFFFFF))
                    .clickable { viewModel.setGstParams(state.gstAmount, rate, state.gstIsExclusive) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${rate.toInt()}%",
                    color = if (isSel) Color.White else theme.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (state.gstIsExclusive) theme.primaryAccent else if (theme.isLight) Color.White else Color(0x22FFFFFF))
                .clickable { viewModel.setGstParams(state.gstAmount, state.gstRate, true) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Add GST (+)", color = if (state.gstIsExclusive) Color.White else theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (!state.gstIsExclusive) theme.primaryAccent else if (theme.isLight) Color.White else Color(0x22FFFFFF))
                .clickable { viewModel.setGstParams(state.gstAmount, state.gstRate, false) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Remove GST (-)", color = if (!state.gstIsExclusive) Color.White else theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }

    ToolCalculatorKeypad(
        theme = theme,
        onDigit = { viewModel.setGstParams(handleDigitInput(state.gstAmount, it), state.gstRate, state.gstIsExclusive) },
        onDot = { viewModel.setGstParams(handleDotInput(state.gstAmount), state.gstRate, state.gstIsExclusive) },
        onBackspace = { viewModel.setGstParams(handleBackspace(state.gstAmount), state.gstRate, state.gstIsExclusive) },
        onClear = { viewModel.setGstParams("", state.gstRate, state.gstIsExclusive) }
    )
}

// ----------------------------------------------------
// 3. Loan EMI Tool View
// ----------------------------------------------------
@Composable
private fun LoanEmiToolView(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    theme: ThemeMode
) {
    var activeField by remember { mutableStateOf("principal") }

    LiquidGlassCard(theme = theme, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Monthly EMI", color = theme.textSecondary, fontSize = 12.sp)
            Text(
                text = "$${String.format(Locale.US, "%.2f", state.emiMonthlyPayment)}",
                color = theme.primaryAccent,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total Interest", color = theme.textSecondary, fontSize = 12.sp)
                    Text("$${String.format(Locale.US, "%.2f", state.emiTotalInterest)}", color = theme.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Payment", color = theme.textSecondary, fontSize = 12.sp)
                    Text("$${String.format(Locale.US, "%.2f", state.emiTotalPayment)}", color = theme.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    ToolNumericDisplayField(
        label = "Loan Amount ($)",
        value = state.emiPrincipal,
        isSelected = activeField == "principal",
        onClick = { activeField = "principal" },
        theme = theme,
        modifier = Modifier.fillMaxWidth()
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ToolNumericDisplayField(
            label = "Rate (% / yr)",
            value = state.emiRate,
            isSelected = activeField == "rate",
            onClick = { activeField = "rate" },
            theme = theme,
            modifier = Modifier.weight(1f)
        )
        ToolNumericDisplayField(
            label = "Tenure (Months)",
            value = state.emiTenureMonths,
            isSelected = activeField == "tenure",
            onClick = { activeField = "tenure" },
            theme = theme,
            modifier = Modifier.weight(1f)
        )
    }

    ToolCalculatorKeypad(
        theme = theme,
        onDigit = { digit ->
            when (activeField) {
                "principal" -> viewModel.setEmiParams(handleDigitInput(state.emiPrincipal, digit), state.emiRate, state.emiTenureMonths)
                "rate" -> viewModel.setEmiParams(state.emiPrincipal, handleDigitInput(state.emiRate, digit), state.emiTenureMonths)
                "tenure" -> viewModel.setEmiParams(state.emiPrincipal, state.emiRate, handleDigitInput(state.emiTenureMonths, digit))
            }
        },
        onDot = {
            when (activeField) {
                "principal" -> viewModel.setEmiParams(handleDotInput(state.emiPrincipal), state.emiRate, state.emiTenureMonths)
                "rate" -> viewModel.setEmiParams(state.emiPrincipal, handleDotInput(state.emiRate), state.emiTenureMonths)
                "tenure" -> viewModel.setEmiParams(state.emiPrincipal, state.emiRate, handleDotInput(state.emiTenureMonths))
            }
        },
        onBackspace = {
            when (activeField) {
                "principal" -> viewModel.setEmiParams(handleBackspace(state.emiPrincipal), state.emiRate, state.emiTenureMonths)
                "rate" -> viewModel.setEmiParams(state.emiPrincipal, handleBackspace(state.emiRate), state.emiTenureMonths)
                "tenure" -> viewModel.setEmiParams(state.emiPrincipal, state.emiRate, handleBackspace(state.emiTenureMonths))
            }
        },
        onClear = {
            when (activeField) {
                "principal" -> viewModel.setEmiParams("", state.emiRate, state.emiTenureMonths)
                "rate" -> viewModel.setEmiParams(state.emiPrincipal, "", state.emiTenureMonths)
                "tenure" -> viewModel.setEmiParams(state.emiPrincipal, state.emiRate, "")
            }
        },
        onNext = {
            activeField = when (activeField) {
                "principal" -> "rate"
                "rate" -> "tenure"
                else -> "principal"
            }
        }
    )
}

// ----------------------------------------------------
// 4. Tip Tool View
// ----------------------------------------------------
@Composable
private fun TipToolView(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    theme: ThemeMode
) {
    LiquidGlassCard(theme = theme, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total Per Person", color = theme.textSecondary, fontSize = 12.sp)
                    Text("$${String.format(Locale.US, "%.2f", state.tipResult.totalPerPerson)}", color = theme.primaryAccent, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Tip Per Person", color = theme.textSecondary, fontSize = 12.sp)
                    Text("$${String.format(Locale.US, "%.2f", state.tipResult.tipPerPerson)}", color = theme.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Tip: $${String.format(Locale.US, "%.2f", state.tipResult.tipAmount)}  •  Total Bill: $${String.format(Locale.US, "%.2f", state.tipResult.totalAmount)}", color = theme.textSecondary, fontSize = 12.sp)
        }
    }

    ToolNumericDisplayField(
        label = "Bill Amount ($)",
        value = state.tipBill,
        isSelected = true,
        onClick = {},
        theme = theme,
        modifier = Modifier.fillMaxWidth()
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(10.0, 15.0, 18.0, 20.0).forEach { p ->
            val isSel = state.tipPercent == p
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSel) theme.primaryAccent else if (theme.isLight) Color.White else Color(0x22FFFFFF))
                    .clickable { viewModel.setTipParams(state.tipBill, p, state.tipPeopleCount, state.tipRoundUp) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("${p.toInt()}%", color = if (isSel) Color.White else theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Split between ${state.tipPeopleCount} people", color = theme.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = { viewModel.setTipParams(state.tipBill, state.tipPercent, (state.tipPeopleCount - 1).coerceAtLeast(1), state.tipRoundUp) },
                modifier = Modifier.size(34.dp).clip(CircleShape).background(if (theme.isLight) Color.White else Color(0x22FFFFFF))
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = theme.textPrimary, modifier = Modifier.size(16.dp))
            }
            Text("${state.tipPeopleCount}", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            IconButton(
                onClick = { viewModel.setTipParams(state.tipBill, state.tipPercent, state.tipPeopleCount + 1, state.tipRoundUp) },
                modifier = Modifier.size(34.dp).clip(CircleShape).background(if (theme.isLight) Color.White else Color(0x22FFFFFF))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = theme.textPrimary, modifier = Modifier.size(16.dp))
            }
        }
    }

    ToolCalculatorKeypad(
        theme = theme,
        onDigit = { viewModel.setTipParams(handleDigitInput(state.tipBill, it), state.tipPercent, state.tipPeopleCount, state.tipRoundUp) },
        onDot = { viewModel.setTipParams(handleDotInput(state.tipBill), state.tipPercent, state.tipPeopleCount, state.tipRoundUp) },
        onBackspace = { viewModel.setTipParams(handleBackspace(state.tipBill), state.tipPercent, state.tipPeopleCount, state.tipRoundUp) },
        onClear = { viewModel.setTipParams("", state.tipPercent, state.tipPeopleCount, state.tipRoundUp) }
    )
}

// ----------------------------------------------------
// 5. Age Tool View
// ----------------------------------------------------
@Composable
private fun AgeToolView(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    theme: ThemeMode
) {
    val context = LocalContext.current

    LiquidGlassCard(theme = theme, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Exact Age", color = theme.textSecondary, fontSize = 12.sp)
            Text(
                text = "${state.ageYears} Years, ${state.ageMonths} Months",
                color = theme.primaryAccent,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("Next Birthday in ${state.ageNextBirthdayDays} days", color = theme.textSecondary, fontSize = 13.sp)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (theme.isLight) Color.White else Color(0x22FFFFFF))
            .clickable {
                val cal = Calendar.getInstance().apply { timeInMillis = state.ageBirthMillis }
                DatePickerDialog(
                    context,
                    { _, y, m, d ->
                        val newCal = Calendar.getInstance().apply { set(y, m, d) }
                        viewModel.setAgeBirthMillis(newCal.timeInMillis)
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Date of Birth", color = theme.textSecondary, fontSize = 12.sp)
                Text(DateCalculatorEngine.formatDate(state.ageBirthMillis), color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = theme.primaryAccent)
        }
    }
}

// ----------------------------------------------------
// 6. Date Tool View
// ----------------------------------------------------
@Composable
private fun DateToolView(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    theme: ThemeMode
) {
    val context = LocalContext.current

    LiquidGlassCard(theme = theme, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Difference", color = theme.textSecondary, fontSize = 12.sp)
            Text(
                text = "${state.dateDiffResult.totalDays} Days",
                color = theme.primaryAccent,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("${state.dateDiffResult.years} Years, ${state.dateDiffResult.months} Months, ${state.dateDiffResult.days} Days", color = theme.textSecondary, fontSize = 13.sp)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (theme.isLight) Color.White else Color(0x22FFFFFF))
            .clickable {
                val cal = Calendar.getInstance().apply { timeInMillis = state.dateStartMillis }
                DatePickerDialog(context, { _, y, m, d ->
                    val newCal = Calendar.getInstance().apply { set(y, m, d) }
                    viewModel.setDateRange(newCal.timeInMillis, state.dateEndMillis)
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            .padding(16.dp)
    ) {
        Column {
            Text("Start Date", color = theme.textSecondary, fontSize = 12.sp)
            Text(DateCalculatorEngine.formatDate(state.dateStartMillis), color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (theme.isLight) Color.White else Color(0x22FFFFFF))
            .clickable {
                val cal = Calendar.getInstance().apply { timeInMillis = state.dateEndMillis }
                DatePickerDialog(context, { _, y, m, d ->
                    val newCal = Calendar.getInstance().apply { set(y, m, d) }
                    viewModel.setDateRange(state.dateStartMillis, newCal.timeInMillis)
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            .padding(16.dp)
    ) {
        Column {
            Text("End Date", color = theme.textSecondary, fontSize = 12.sp)
            Text(DateCalculatorEngine.formatDate(state.dateEndMillis), color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ----------------------------------------------------
// 7. Discount Tool View
// ----------------------------------------------------
@Composable
private fun DiscountToolView(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    theme: ThemeMode
) {
    var activeField by remember { mutableStateOf("original") }

    LiquidGlassCard(theme = theme, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Final Price", color = theme.textSecondary, fontSize = 12.sp)
            Text(
                text = "$${String.format(Locale.US, "%.2f", state.discountResult.finalPrice)}",
                color = theme.primaryAccent,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("You Save: $${String.format(Locale.US, "%.2f", state.discountResult.discountAmount)}", color = theme.textSecondary, fontSize = 13.sp)
        }
    }

    ToolNumericDisplayField(
        label = "Original Price ($)",
        value = state.discountOriginal,
        isSelected = activeField == "original",
        onClick = { activeField = "original" },
        theme = theme,
        modifier = Modifier.fillMaxWidth()
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ToolNumericDisplayField(
            label = "Discount (%)",
            value = state.discountPercent,
            isSelected = activeField == "discount",
            onClick = { activeField = "discount" },
            theme = theme,
            modifier = Modifier.weight(1f)
        )
        ToolNumericDisplayField(
            label = "Tax (%)",
            value = state.discountTax,
            isSelected = activeField == "tax",
            onClick = { activeField = "tax" },
            theme = theme,
            modifier = Modifier.weight(1f)
        )
    }

    ToolCalculatorKeypad(
        theme = theme,
        onDigit = { digit ->
            when (activeField) {
                "original" -> viewModel.setDiscountParams(handleDigitInput(state.discountOriginal, digit), state.discountPercent, state.discountTax)
                "discount" -> viewModel.setDiscountParams(state.discountOriginal, handleDigitInput(state.discountPercent, digit), state.discountTax)
                "tax" -> viewModel.setDiscountParams(state.discountOriginal, state.discountPercent, handleDigitInput(state.discountTax, digit))
            }
        },
        onDot = {
            when (activeField) {
                "original" -> viewModel.setDiscountParams(handleDotInput(state.discountOriginal), state.discountPercent, state.discountTax)
                "discount" -> viewModel.setDiscountParams(state.discountOriginal, handleDotInput(state.discountPercent), state.discountTax)
                "tax" -> viewModel.setDiscountParams(state.discountOriginal, state.discountPercent, handleDotInput(state.discountTax))
            }
        },
        onBackspace = {
            when (activeField) {
                "original" -> viewModel.setDiscountParams(handleBackspace(state.discountOriginal), state.discountPercent, state.discountTax)
                "discount" -> viewModel.setDiscountParams(state.discountOriginal, handleBackspace(state.discountPercent), state.discountTax)
                "tax" -> viewModel.setDiscountParams(state.discountOriginal, state.discountPercent, handleBackspace(state.discountTax))
            }
        },
        onClear = {
            when (activeField) {
                "original" -> viewModel.setDiscountParams("", state.discountPercent, state.discountTax)
                "discount" -> viewModel.setDiscountParams(state.discountOriginal, "", state.discountTax)
                "tax" -> viewModel.setDiscountParams(state.discountOriginal, state.discountPercent, "")
            }
        },
        onNext = {
            activeField = when (activeField) {
                "original" -> "discount"
                "discount" -> "tax"
                else -> "original"
            }
        }
    )
}

// ----------------------------------------------------
// 8. BMI Tool View
// ----------------------------------------------------
@Composable
private fun BmiToolView(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    theme: ThemeMode
) {
    var activeField by remember { mutableStateOf("weight") }

    LiquidGlassCard(theme = theme, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Your BMI", color = theme.textSecondary, fontSize = 12.sp)
            Text(
                text = String.format(Locale.US, "%.1f", state.bmiScore),
                color = theme.primaryAccent,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.bmiCategory,
                color = theme.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ToolNumericDisplayField(
            label = "Weight (kg)",
            value = state.bmiWeightKg,
            isSelected = activeField == "weight",
            onClick = { activeField = "weight" },
            theme = theme,
            modifier = Modifier.weight(1f)
        )
        ToolNumericDisplayField(
            label = "Height (cm)",
            value = state.bmiHeightCm,
            isSelected = activeField == "height",
            onClick = { activeField = "height" },
            theme = theme,
            modifier = Modifier.weight(1f)
        )
    }

    ToolCalculatorKeypad(
        theme = theme,
        onDigit = { digit ->
            if (activeField == "weight") {
                viewModel.setBmiParams(handleDigitInput(state.bmiWeightKg, digit), state.bmiHeightCm)
            } else {
                viewModel.setBmiParams(state.bmiWeightKg, handleDigitInput(state.bmiHeightCm, digit))
            }
        },
        onDot = {
            if (activeField == "weight") {
                viewModel.setBmiParams(handleDotInput(state.bmiWeightKg), state.bmiHeightCm)
            } else {
                viewModel.setBmiParams(state.bmiWeightKg, handleDotInput(state.bmiHeightCm))
            }
        },
        onBackspace = {
            if (activeField == "weight") {
                viewModel.setBmiParams(handleBackspace(state.bmiWeightKg), state.bmiHeightCm)
            } else {
                viewModel.setBmiParams(state.bmiWeightKg, handleBackspace(state.bmiHeightCm))
            }
        },
        onClear = {
            if (activeField == "weight") {
                viewModel.setBmiParams("", state.bmiHeightCm)
            } else {
                viewModel.setBmiParams(state.bmiWeightKg, "")
            }
        },
        onNext = {
            activeField = if (activeField == "weight") "height" else "weight"
        }
    )
}

// ----------------------------------------------------
// Custom Numeric Display Field (No Android Keyboard!)
// ----------------------------------------------------
@Composable
private fun ToolNumericDisplayField(
    label: String,
    value: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    theme: ThemeMode,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val borderColor = if (isSelected) theme.primaryAccent else if (theme.isLight) Color(0x18000000) else Color(0x33FFFFFF)
    val bgColor = if (isSelected) {
        theme.primaryAccent.copy(alpha = if (theme.isLight) 0.08f else 0.16f)
    } else {
        if (theme.isLight) Color.White else Color(0x1AFFFFFF)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = label,
                color = if (isSelected) theme.primaryAccent else theme.textSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (value.isBlank()) "0" else value,
                    color = if (value.isBlank()) theme.textSecondary.copy(alpha = 0.5f) else theme.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (isSelected) {
                    Text(
                        text = " |",
                        color = theme.primaryAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// Custom Calculator Number Keypad for Tools
// ----------------------------------------------------
@Composable
private fun ToolCalculatorKeypad(
    theme: ThemeMode,
    onDigit: (String) -> Unit,
    onDot: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onNext: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
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
                    onClick = { onDigit(digit) },
                    theme = theme,
                    type = CalcButtonType.NUMBER,
                    modifier = Modifier.weight(1f).height(46.dp)
                )
            }
            LiquidGlassButton(
                text = "⌫",
                onClick = onBackspace,
                theme = theme,
                type = CalcButtonType.FUNCTION,
                icon = Icons.AutoMirrored.Filled.Backspace,
                modifier = Modifier.weight(1f).height(46.dp)
            )
        }

        // Row 2: 4, 5, 6, C
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("4", "5", "6").forEach { digit ->
                LiquidGlassButton(
                    text = digit,
                    onClick = { onDigit(digit) },
                    theme = theme,
                    type = CalcButtonType.NUMBER,
                    modifier = Modifier.weight(1f).height(46.dp)
                )
            }
            LiquidGlassButton(
                text = "C",
                onClick = onClear,
                theme = theme,
                type = CalcButtonType.FUNCTION,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f).height(46.dp)
            )
        }

        // Row 3: 1, 2, 3, Next ⇥ or AC
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("1", "2", "3").forEach { digit ->
                LiquidGlassButton(
                    text = digit,
                    onClick = { onDigit(digit) },
                    theme = theme,
                    type = CalcButtonType.NUMBER,
                    modifier = Modifier.weight(1f).height(46.dp)
                )
            }
            if (onNext != null) {
                LiquidGlassButton(
                    text = "Next ⇥",
                    onClick = onNext,
                    theme = theme,
                    type = CalcButtonType.ACTION,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f).height(46.dp)
                )
            } else {
                LiquidGlassButton(
                    text = "AC",
                    onClick = onClear,
                    theme = theme,
                    type = CalcButtonType.FUNCTION,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f).height(46.dp)
                )
            }
        }

        // Row 4: 00, 0, ., Done ✓
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LiquidGlassButton(
                text = "00",
                onClick = { onDigit("00") },
                theme = theme,
                type = CalcButtonType.NUMBER,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f).height(46.dp)
            )
            LiquidGlassButton(
                text = "0",
                onClick = { onDigit("0") },
                theme = theme,
                type = CalcButtonType.NUMBER,
                modifier = Modifier.weight(1f).height(46.dp)
            )
            LiquidGlassButton(
                text = ".",
                onClick = onDot,
                theme = theme,
                type = CalcButtonType.NUMBER,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f).height(46.dp)
            )
            LiquidGlassButton(
                text = "✓",
                onClick = { onDone?.invoke() },
                theme = theme,
                type = CalcButtonType.EQUALS,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f).height(46.dp)
            )
        }
    }
}

// ----------------------------------------------------
// Keypad Input Helper Functions
// ----------------------------------------------------
private fun handleDigitInput(current: String, digit: String): String {
    if (current == "0" || current.isEmpty()) {
        return if (digit == "00") "0" else digit
    }
    if (current.length >= 10) return current
    return current + digit
}

private fun handleDotInput(current: String): String {
    if (current.isEmpty()) return "0."
    if (!current.contains(".")) return "$current."
    return current
}

private fun handleBackspace(current: String): String {
    if (current.length <= 1) return ""
    return current.dropLast(1)
}
