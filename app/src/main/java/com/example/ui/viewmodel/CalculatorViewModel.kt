package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CalculationEntity
import com.example.data.local.CalculationRepository
import com.example.data.remote.CurrencyData
import com.example.data.remote.CurrencyRepository
import com.example.engine.DateCalculatorEngine
import com.example.engine.DateDiffResult
import com.example.engine.DiscountResult
import com.example.engine.ExpressionEvaluator
import com.example.engine.FinanceAndTipEngine
import com.example.engine.TipResult
import com.example.engine.UnitCategory
import com.example.engine.UnitConversionEngine
import com.example.engine.UnitItem
import com.example.ui.components.AppNavTab
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class CalculatorUiState(
    val currentTab: AppNavTab = AppNavTab.CALCULATOR,
    val theme: ThemeMode = ThemeMode.AURORA,
    
    // Calculator
    val expression: String = "",
    val liveResult: String = "",
    val evaluatedResult: String = "",
    val angleMode: ExpressionEvaluator.AngleMode = ExpressionEvaluator.AngleMode.DEG,
    val isScientificExpanded: Boolean = false,
    val memoryValue: Double = 0.0,
    val hasMemory: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = "",

    // Currency
    val fromCurrency: String = "USD",
    val toCurrency: String = "EUR",
    val currencyAmount: String = "100",
    val convertedCurrencyAmount: Double = 92.0,
    val currencyRates: Map<String, Double> = CurrencyData.DEFAULT_RATES,
    val currencyLastUpdated: String = "Rates loaded",
    val isCurrencyLoading: Boolean = false,

    // Unit Converter
    val unitCategory: UnitCategory = UnitCategory.LENGTH,
    val fromUnit: UnitItem = UnitConversionEngine.UNITS[UnitCategory.LENGTH]!![0], // Meter
    val toUnit: UnitItem = UnitConversionEngine.UNITS[UnitCategory.LENGTH]!![1],   // Kilometer
    val unitInputValue: String = "1",
    val unitOutputValue: String = "0.001",

    // Tools - Active subtab
    val toolsSubTab: ToolsSubTab = ToolsSubTab.TIP,

    // Tip Calculator
    val tipBill: String = "50",
    val tipPercent: Double = 15.0,
    val tipPeopleCount: Int = 2,
    val tipRoundUp: Boolean = false,
    val tipResult: TipResult = TipResult(7.5, 57.5, 3.75, 28.75),

    // Percentage Calculator
    val percentMode: PercentMode = PercentMode.PERCENT_OF,
    val percentValA: String = "20",
    val percentValB: String = "150",
    val percentResult: String = "30",
    val discountOriginal: String = "120",
    val discountPercent: String = "25",
    val discountTax: String = "8",
    val discountResult: DiscountResult = DiscountResult(30.0, 90.0, 7.2, 97.2, 30.0),

    // Date Calculator
    val dateMode: DateMode = DateMode.DIFFERENCE,
    val dateStartMillis: Long = System.currentTimeMillis(),
    val dateEndMillis: Long = System.currentTimeMillis() + 86400000L * 30,
    val dateDiffResult: DateDiffResult = DateCalculatorEngine.calculateDifference(
        System.currentTimeMillis(),
        System.currentTimeMillis() + 86400000L * 30
    ),
    val dateAddYears: Int = 0,
    val dateAddMonths: Int = 1,
    val dateAddWeeks: Int = 0,
    val dateAddDays: Int = 0,
    val dateIsAdd: Boolean = true,
    val dateCalculatedTargetMillis: Long = System.currentTimeMillis() + 86400000L * 30,

    // GST Calculator
    val gstAmount: String = "1000",
    val gstRate: Double = 18.0,
    val gstIsExclusive: Boolean = true,
    val gstTax: Double = 180.0,
    val gstTotal: Double = 1180.0,

    // Loan EMI Calculator
    val emiPrincipal: String = "100000",
    val emiRate: String = "8.5",
    val emiTenureMonths: String = "24",
    val emiMonthlyPayment: Double = 4545.64,
    val emiTotalInterest: Double = 9095.36,
    val emiTotalPayment: Double = 109095.36,

    // Age Calculator
    val ageBirthMillis: Long = System.currentTimeMillis() - 86400000L * 365 * 25,
    val ageYears: Int = 25,
    val ageMonths: Int = 0,
    val ageDays: Int = 0,
    val ageNextBirthdayDays: Int = 180,

    // BMI Calculator
    val bmiWeightKg: String = "70",
    val bmiHeightCm: String = "175",
    val bmiScore: Double = 22.86,
    val bmiCategory: String = "Normal weight",

    // Active tool dialog/sheet
    val activeToolDialog: ToolType? = null,

    // Settings
    val hapticFeedbackEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val currentLanguage: String = "English",
    val showSplash: Boolean = true,
    val isSettingsOpen: Boolean = false,
    val isThemesOpen: Boolean = false,
    val isAboutOpen: Boolean = false
)

enum class ToolType(val title: String) {
    PERCENTAGE("Percentage Calculator"),
    GST("GST Calculator"),
    LOAN_EMI("Loan EMI Calculator"),
    TIP("Tip Calculator"),
    AGE("Age Calculator"),
    DATE("Date Calculator"),
    DISCOUNT("Discount Calculator"),
    BMI("BMI Calculator")
}

enum class ToolsSubTab(val title: String) {
    TIP("Tip Splitter"),
    PERCENT("Percentages"),
    DATE("Date Calc"),
    THEMES("Themes")
}

enum class PercentMode(val title: String) {
    PERCENT_OF("X% of Y"),
    PART_OF("X is what % of Y"),
    PERCENT_CHANGE("% Change"),
    DISCOUNT("Discount & Tax")
}

enum class DateMode(val title: String) {
    DIFFERENCE("Date Difference"),
    ADD_SUBTRACT("Add / Subtract Date")
}

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("smart_calculator_prefs", Context.MODE_PRIVATE)
    private val db = AppDatabase.getDatabase(application)
    private val calculationRepository = CalculationRepository(db.calculationDao())
    private val currencyRepository = CurrencyRepository()
    private val clipboardManager = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val historyList: StateFlow<List<CalculationEntity>> = calculationRepository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val savedThemeName = prefs.getString("pref_theme", ThemeMode.AURORA.name)
        val loadedTheme = try {
            ThemeMode.valueOf(savedThemeName ?: ThemeMode.AURORA.name)
        } catch (e: Exception) {
            ThemeMode.AURORA
        }

        val savedAngleMode = prefs.getString("pref_angle_mode", ExpressionEvaluator.AngleMode.DEG.name)
        val loadedAngleMode = try {
            ExpressionEvaluator.AngleMode.valueOf(savedAngleMode ?: ExpressionEvaluator.AngleMode.DEG.name)
        } catch (e: Exception) {
            ExpressionEvaluator.AngleMode.DEG
        }

        val savedFromCurr = prefs.getString("pref_from_currency", "USD") ?: "USD"
        val savedToCurr = prefs.getString("pref_to_currency", "EUR") ?: "EUR"
        val savedCurrAmount = prefs.getString("pref_currency_amount", "100") ?: "100"

        val savedMemoryVal = prefs.getFloat("pref_memory_value", 0f).toDouble()
        val savedHasMemory = prefs.getBoolean("pref_has_memory", false)

        _uiState.update {
            it.copy(
                theme = loadedTheme,
                angleMode = loadedAngleMode,
                fromCurrency = savedFromCurr,
                toCurrency = savedToCurr,
                currencyAmount = savedCurrAmount,
                memoryValue = savedMemoryVal,
                hasMemory = savedHasMemory
            )
        }

        // Initial live rate fetch
        refreshCurrencyRates()
        recalculateCurrency()
        // Initialize tool calculations
        updateTipCalculation()
        updatePercentageCalculation()
        updateDateCalculation()
    }

    // ----------------------------------------------------
    // Tab & Theme Navigation
    // ----------------------------------------------------
    fun selectTab(tab: AppNavTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setTheme(theme: ThemeMode) {
        prefs.edit().putString("pref_theme", theme.name).apply()
        _uiState.update { it.copy(theme = theme) }
    }

    fun setToolsSubTab(subTab: ToolsSubTab) {
        _uiState.update { it.copy(toolsSubTab = subTab) }
    }

    // ----------------------------------------------------
    // Calculator Operations
    // ----------------------------------------------------
    fun toggleScientific() {
        _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
    }

    fun toggleAngleMode() {
        _uiState.update {
            val newMode = if (it.angleMode == ExpressionEvaluator.AngleMode.DEG)
                ExpressionEvaluator.AngleMode.RAD
            else ExpressionEvaluator.AngleMode.DEG
            prefs.edit().putString("pref_angle_mode", newMode.name).apply()
            it.copy(angleMode = newMode)
        }
        recomputeLiveResult()
    }

    fun onInput(charOrFunc: String) {
        val currentExp = _uiState.value.expression
        val operators = setOf("+", "-", "−", "×", "÷", "%", "^", ".")
        val isNewOperator = charOrFunc in operators

        var baseExp = currentExp
        if (isNewOperator && currentExp.isNotEmpty()) {
            val lastChar = currentExp.last().toString()
            if (lastChar in operators) {
                val isNegativeAfterMulDiv = (charOrFunc == "-" || charOrFunc == "−") && (lastChar == "×" || lastChar == "÷")
                if (!isNegativeAfterMulDiv) {
                    baseExp = currentExp.dropLast(1)
                }
            }
        }

        val newExp = when (charOrFunc) {
            "sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "sqrt", "cbrt", "abs" -> {
                "$baseExp$charOrFunc("
            }
            "1/x" -> {
                if (baseExp.isEmpty()) "1/(" else "$baseExp*(1/("
            }
            "x²" -> "$baseExp^2"
            "x³" -> "$baseExp^3"
            "x^y" -> "$baseExp^"
            "+/-" -> {
                if (baseExp.startsWith("-(")) {
                    baseExp.removePrefix("-(").removeSuffix(")")
                } else {
                    "-($baseExp)"
                }
            }
            else -> "$baseExp$charOrFunc"
        }

        _uiState.update { it.copy(expression = newExp, isError = false, errorMessage = "") }
        recomputeLiveResult()
    }

    fun onBackspace() {
        val currentExp = _uiState.value.expression
        if (currentExp.isNotEmpty()) {
            val updated = when {
                currentExp.endsWith("sqrt(") || currentExp.endsWith("cbrt(") || currentExp.endsWith("asin(") ||
                        currentExp.endsWith("acos(") || currentExp.endsWith("atan(") -> {
                    currentExp.dropLast(5)
                }
                currentExp.endsWith("sin(") || currentExp.endsWith("cos(") || currentExp.endsWith("tan(") ||
                        currentExp.endsWith("log(") || currentExp.endsWith("abs(") -> {
                    currentExp.dropLast(4)
                }
                currentExp.endsWith("ln(") -> currentExp.dropLast(3)
                else -> currentExp.dropLast(1)
            }
            _uiState.update { it.copy(expression = updated, isError = false, errorMessage = "") }
            recomputeLiveResult()
        }
    }

    fun onClear() {
        _uiState.update {
            it.copy(
                expression = "",
                liveResult = "",
                evaluatedResult = "",
                isError = false,
                errorMessage = ""
            )
        }
    }

    fun onEquals() {
        val currentExp = _uiState.value.expression
        if (currentExp.isBlank()) return

        when (val result = ExpressionEvaluator.evaluate(currentExp, _uiState.value.angleMode)) {
            is ExpressionEvaluator.EvalResult.Success -> {
                _uiState.update {
                    it.copy(
                        evaluatedResult = result.formatted,
                        liveResult = "",
                        isError = false,
                        errorMessage = ""
                    )
                }
                // Save to Room DB
                viewModelScope.launch {
                    val calcType = if (_uiState.value.isScientificExpanded) "SCIENTIFIC" else "BASIC"
                    calculationRepository.insert(currentExp, result.formatted, calcType)
                }
            }
            is ExpressionEvaluator.EvalResult.Error -> {
                _uiState.update {
                    it.copy(
                        isError = true,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private fun recomputeLiveResult() {
        val exp = _uiState.value.expression
        if (exp.isBlank()) {
            _uiState.update { it.copy(liveResult = "") }
            return
        }

        when (val eval = ExpressionEvaluator.evaluate(exp, _uiState.value.angleMode)) {
            is ExpressionEvaluator.EvalResult.Success -> {
                _uiState.update { it.copy(liveResult = eval.formatted) }
            }
            is ExpressionEvaluator.EvalResult.Error -> {
                // If it's a partial expression being typed, don't show full error in live preview
                _uiState.update { it.copy(liveResult = "") }
            }
        }
    }

    // Memory operations (MC, MR, M+, M-, MS)
    fun memoryClear() {
        prefs.edit().putFloat("pref_memory_value", 0f).putBoolean("pref_has_memory", false).apply()
        _uiState.update { it.copy(memoryValue = 0.0, hasMemory = false) }
        showToast("Memory cleared")
    }

    fun memoryRecall() {
        if (_uiState.value.hasMemory) {
            val memFormatted = ExpressionEvaluator.formatResult(_uiState.value.memoryValue)
            onInput(memFormatted)
        }
    }

    fun memoryAdd() {
        val currentVal = getCurrentEvaluationValue()
        val newMem = _uiState.value.memoryValue + currentVal
        prefs.edit().putFloat("pref_memory_value", newMem.toFloat()).putBoolean("pref_has_memory", true).apply()
        _uiState.update { it.copy(memoryValue = newMem, hasMemory = true) }
        showToast("Added to Memory: ${ExpressionEvaluator.formatResult(newMem)}")
    }

    fun memorySubtract() {
        val currentVal = getCurrentEvaluationValue()
        val newMem = _uiState.value.memoryValue - currentVal
        prefs.edit().putFloat("pref_memory_value", newMem.toFloat()).putBoolean("pref_has_memory", true).apply()
        _uiState.update { it.copy(memoryValue = newMem, hasMemory = true) }
        showToast("Subtracted from Memory: ${ExpressionEvaluator.formatResult(newMem)}")
    }

    fun memoryStore() {
        val currentVal = getCurrentEvaluationValue()
        prefs.edit().putFloat("pref_memory_value", currentVal.toFloat()).putBoolean("pref_has_memory", true).apply()
        _uiState.update { it.copy(memoryValue = currentVal, hasMemory = true) }
        showToast("Stored in Memory: ${ExpressionEvaluator.formatResult(currentVal)}")
    }

    private fun getCurrentEvaluationValue(): Double {
        val exp = _uiState.value.expression
        if (exp.isNotBlank()) {
            when (val eval = ExpressionEvaluator.evaluate(exp, _uiState.value.angleMode)) {
                is ExpressionEvaluator.EvalResult.Success -> return eval.value
                else -> {}
            }
        }
        return _uiState.value.evaluatedResult.toDoubleOrNull() ?: 0.0
    }

    // ----------------------------------------------------
    // History Actions
    // ----------------------------------------------------
    fun reuseHistory(item: CalculationEntity, asExpression: Boolean) {
        if (asExpression) {
            _uiState.update {
                it.copy(
                    expression = item.expression,
                    currentTab = AppNavTab.CALCULATOR,
                    isError = false
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    expression = item.result,
                    currentTab = AppNavTab.CALCULATOR,
                    isError = false
                )
            }
        }
        recomputeLiveResult()
        showToast("Loaded calculation")
    }

    fun deleteHistoryItem(entity: CalculationEntity) {
        viewModelScope.launch {
            calculationRepository.delete(entity)
            showToast("Calculation deleted")
        }
    }

    fun toggleFavoriteHistory(entity: CalculationEntity) {
        viewModelScope.launch {
            calculationRepository.toggleFavorite(entity.id, entity.isFavorite)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            calculationRepository.clearAll()
            showToast("History cleared")
        }
    }

    // ----------------------------------------------------
    // Currency Converter Operations
    // ----------------------------------------------------
    fun refreshCurrencyRates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCurrencyLoading = true) }
            val result = currencyRepository.fetchRates("USD")
            result.onSuccess { (rates, lastUpdated) ->
                _uiState.update {
                    it.copy(
                        currencyRates = rates,
                        currencyLastUpdated = lastUpdated,
                        isCurrencyLoading = false
                    )
                }
                recalculateCurrency()
            }.onFailure {
                _uiState.update { it.copy(isCurrencyLoading = false) }
            }
        }
    }

    fun setFromCurrency(code: String) {
        prefs.edit().putString("pref_from_currency", code).apply()
        _uiState.update { it.copy(fromCurrency = code) }
        recalculateCurrency()
    }

    fun setToCurrency(code: String) {
        prefs.edit().putString("pref_to_currency", code).apply()
        _uiState.update { it.copy(toCurrency = code) }
        recalculateCurrency()
    }

    fun swapCurrencies() {
        _uiState.update {
            val newFrom = it.toCurrency
            val newTo = it.fromCurrency
            prefs.edit().putString("pref_from_currency", newFrom).putString("pref_to_currency", newTo).apply()
            it.copy(
                fromCurrency = newFrom,
                toCurrency = newTo
            )
        }
        recalculateCurrency()
    }

    fun setCurrencyAmount(amount: String) {
        prefs.edit().putString("pref_currency_amount", amount).apply()
        _uiState.update { it.copy(currencyAmount = amount) }
        recalculateCurrency()
    }

    fun onCurrencyDigit(digit: String) {
        val current = _uiState.value.currencyAmount
        val updated = if (current == "0") digit else current + digit
        setCurrencyAmount(updated)
    }

    fun onCurrencyBackspace() {
        val current = _uiState.value.currencyAmount
        val updated = if (current.length <= 1) "0" else current.dropLast(1)
        setCurrencyAmount(updated)
    }

    fun onCurrencyClear() {
        setCurrencyAmount("0")
    }

    fun onCurrencyDot() {
        val current = _uiState.value.currencyAmount
        if (!current.contains(".")) {
            setCurrencyAmount("$current.")
        }
    }

    private fun recalculateCurrency() {
        val amount = _uiState.value.currencyAmount.toDoubleOrNull() ?: 0.0
        val converted = currencyRepository.convert(
            amount = amount,
            fromCode = _uiState.value.fromCurrency,
            toCode = _uiState.value.toCurrency,
            rates = _uiState.value.currencyRates
        )
        _uiState.update { it.copy(convertedCurrencyAmount = converted) }
    }

    // ----------------------------------------------------
    // Unit Converter Operations
    // ----------------------------------------------------
    fun selectUnitCategory(category: UnitCategory) {
        val units = UnitConversionEngine.UNITS[category] ?: return
        val from = units.firstOrNull() ?: return
        val to = units.getOrNull(1) ?: from

        _uiState.update {
            it.copy(
                unitCategory = category,
                fromUnit = from,
                toUnit = to
            )
        }
        recalculateUnit()
    }

    fun setFromUnit(unit: UnitItem) {
        _uiState.update { it.copy(fromUnit = unit) }
        recalculateUnit()
    }

    fun setToUnit(unit: UnitItem) {
        _uiState.update { it.copy(toUnit = unit) }
        recalculateUnit()
    }

    fun swapUnits() {
        _uiState.update {
            it.copy(
                fromUnit = it.toUnit,
                toUnit = it.fromUnit
            )
        }
        recalculateUnit()
    }

    fun setUnitInputValue(value: String) {
        _uiState.update { it.copy(unitInputValue = value) }
        recalculateUnit()
    }

    fun onUnitDigit(digit: String) {
        val current = _uiState.value.unitInputValue
        val updated = if (current == "0") digit else current + digit
        setUnitInputValue(updated)
    }

    fun onUnitBackspace() {
        val current = _uiState.value.unitInputValue
        val updated = if (current.length <= 1) "0" else current.dropLast(1)
        setUnitInputValue(updated)
    }

    fun onUnitClear() {
        setUnitInputValue("0")
    }

    fun onUnitDot() {
        val current = _uiState.value.unitInputValue
        if (!current.contains(".")) {
            setUnitInputValue("$current.")
        }
    }

    fun onUnitNegate() {
        val current = _uiState.value.unitInputValue
        val updated = if (current.startsWith("-")) current.drop(1) else "-$current"
        setUnitInputValue(updated)
    }

    private fun recalculateUnit() {
        val input = _uiState.value.unitInputValue.toDoubleOrNull() ?: 0.0
        val converted = UnitConversionEngine.convert(
            value = input,
            fromUnit = _uiState.value.fromUnit,
            toUnit = _uiState.value.toUnit
        )
        _uiState.update {
            it.copy(unitOutputValue = UnitConversionEngine.formatUnitValue(converted))
        }
    }

    // ----------------------------------------------------
    // Tip Splitter Operations
    // ----------------------------------------------------
    fun setTipBill(bill: String) {
        _uiState.update { it.copy(tipBill = bill) }
        updateTipCalculation()
    }

    fun setTipPercent(percent: Double) {
        _uiState.update { it.copy(tipPercent = percent) }
        updateTipCalculation()
    }

    fun setTipPeopleCount(count: Int) {
        val validCount = count.coerceIn(1, 100)
        _uiState.update { it.copy(tipPeopleCount = validCount) }
        updateTipCalculation()
    }

    fun toggleTipRoundUp() {
        _uiState.update { it.copy(tipRoundUp = !it.tipRoundUp) }
        updateTipCalculation()
    }

    private fun updateTipCalculation() {
        val bill = _uiState.value.tipBill.toDoubleOrNull() ?: 0.0
        val result = FinanceAndTipEngine.calculateTip(
            billAmount = bill,
            tipPercent = _uiState.value.tipPercent,
            splitCount = _uiState.value.tipPeopleCount,
            roundUp = _uiState.value.tipRoundUp
        )
        _uiState.update { it.copy(tipResult = result) }
    }

    // ----------------------------------------------------
    // Percentage Calculator Operations
    // ----------------------------------------------------
    fun setPercentMode(mode: PercentMode) {
        _uiState.update { it.copy(percentMode = mode) }
        updatePercentageCalculation()
    }

    fun setPercentValA(valA: String) {
        _uiState.update { it.copy(percentValA = valA) }
        updatePercentageCalculation()
    }

    fun setPercentValB(valB: String) {
        _uiState.update { it.copy(percentValB = valB) }
        updatePercentageCalculation()
    }

    fun setDiscountInputs(original: String, discount: String, tax: String) {
        _uiState.update {
            it.copy(
                discountOriginal = original,
                discountPercent = discount,
                discountTax = tax
            )
        }
        updatePercentageCalculation()
    }

    private fun updatePercentageCalculation() {
        val a = _uiState.value.percentValA.toDoubleOrNull() ?: 0.0
        val b = _uiState.value.percentValB.toDoubleOrNull() ?: 0.0

        val res = when (_uiState.value.percentMode) {
            PercentMode.PERCENT_OF -> "${FinanceAndTipEngine.calculatePercentageOf(a, b)}"
            PercentMode.PART_OF -> "${FinanceAndTipEngine.calculatePartOfTotal(a, b)}%"
            PercentMode.PERCENT_CHANGE -> {
                val change = FinanceAndTipEngine.calculatePercentageChange(a, b)
                val sign = if (change > 0) "+" else ""
                "$sign$change%"
            }
            PercentMode.DISCOUNT -> ""
        }

        val orig = _uiState.value.discountOriginal.toDoubleOrNull() ?: 0.0
        val disc = _uiState.value.discountPercent.toDoubleOrNull() ?: 0.0
        val tax = _uiState.value.discountTax.toDoubleOrNull() ?: 0.0
        val discRes = FinanceAndTipEngine.calculateDiscountAndTax(orig, disc, tax)

        _uiState.update {
            it.copy(
                percentResult = res,
                discountResult = discRes
            )
        }
    }

    // ----------------------------------------------------
    // Date Calculator Operations
    // ----------------------------------------------------
    fun setDateMode(mode: DateMode) {
        _uiState.update { it.copy(dateMode = mode) }
        updateDateCalculation()
    }

    fun setDateStart(millis: Long) {
        _uiState.update { it.copy(dateStartMillis = millis) }
        updateDateCalculation()
    }

    fun setDateEnd(millis: Long) {
        _uiState.update { it.copy(dateEndMillis = millis) }
        updateDateCalculation()
    }

    fun setDateAddParams(years: Int, months: Int, weeks: Int, days: Int, isAdd: Boolean) {
        _uiState.update {
            it.copy(
                dateAddYears = years,
                dateAddMonths = months,
                dateAddWeeks = weeks,
                dateAddDays = days,
                dateIsAdd = isAdd
            )
        }
        updateDateCalculation()
    }

    private fun updateDateCalculation() {
        val diff = DateCalculatorEngine.calculateDifference(
            _uiState.value.dateStartMillis,
            _uiState.value.dateEndMillis
        )

        val target = DateCalculatorEngine.modifyDate(
            baseMillis = _uiState.value.dateStartMillis,
            years = _uiState.value.dateAddYears,
            months = _uiState.value.dateAddMonths,
            weeks = _uiState.value.dateAddWeeks,
            days = _uiState.value.dateAddDays,
            isAdd = _uiState.value.dateIsAdd
        )

        _uiState.update {
            it.copy(
                dateDiffResult = diff,
                dateCalculatedTargetMillis = target
            )
        }
    }

    // ----------------------------------------------------
    // Tool Dialog & Navigation
    // ----------------------------------------------------
    fun openTool(tool: ToolType) {
        _uiState.update { it.copy(activeToolDialog = tool) }
    }

    fun closeTool() {
        _uiState.update { it.copy(activeToolDialog = null) }
    }

    // ----------------------------------------------------
    // GST Calculator Operations
    // ----------------------------------------------------
    fun setGstParams(amount: String, rate: Double, isExclusive: Boolean) {
        val amt = amount.toDoubleOrNull() ?: 0.0
        val tax = if (isExclusive) {
            amt * (rate / 100.0)
        } else {
            amt - (amt / (1 + rate / 100.0))
        }
        val total = if (isExclusive) amt + tax else amt

        _uiState.update {
            it.copy(
                gstAmount = amount,
                gstRate = rate,
                gstIsExclusive = isExclusive,
                gstTax = tax,
                gstTotal = total
            )
        }
    }

    // ----------------------------------------------------
    // Loan EMI Calculator Operations
    // ----------------------------------------------------
    fun setEmiParams(principal: String, rate: String, tenureMonths: String) {
        val p = principal.toDoubleOrNull() ?: 0.0
        val r = (rate.toDoubleOrNull() ?: 0.0) / (12 * 100.0)
        val n = tenureMonths.toDoubleOrNull() ?: 1.0

        val emi = if (p > 0 && r > 0 && n > 0) {
            val factor = Math.pow(1 + r, n)
            (p * r * factor) / (factor - 1)
        } else if (n > 0) {
            p / n
        } else 0.0

        val totalPayment = emi * n
        val totalInterest = (totalPayment - p).coerceAtLeast(0.0)

        _uiState.update {
            it.copy(
                emiPrincipal = principal,
                emiRate = rate,
                emiTenureMonths = tenureMonths,
                emiMonthlyPayment = emi,
                emiTotalInterest = totalInterest,
                emiTotalPayment = totalPayment
            )
        }
    }

    // ----------------------------------------------------
    // Age Calculator Operations
    // ----------------------------------------------------
    fun setAgeBirthDate(birthMillis: Long) {
        val birthCal = Calendar.getInstance().apply { timeInMillis = birthMillis }
        val nowCal = Calendar.getInstance()

        var years = nowCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
        var months = nowCal.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH)
        var days = nowCal.get(Calendar.DAY_OF_MONTH) - birthCal.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months--
            val prevMonth = (nowCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        if (months < 0) {
            years--
            months += 12
        }

        val nextBirthday = Calendar.getInstance().apply {
            set(Calendar.MONTH, birthCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, birthCal.get(Calendar.DAY_OF_MONTH))
            if (before(nowCal)) add(Calendar.YEAR, 1)
        }
        val diffNext = ((nextBirthday.timeInMillis - nowCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        _uiState.update {
            it.copy(
                ageBirthMillis = birthMillis,
                ageYears = years.coerceAtLeast(0),
                ageMonths = months.coerceAtLeast(0),
                ageDays = days.coerceAtLeast(0),
                ageNextBirthdayDays = diffNext.coerceAtLeast(0)
            )
        }
    }

    // ----------------------------------------------------
    // BMI Calculator Operations
    // ----------------------------------------------------
    fun setBmiParams(weightKg: String, heightCm: String) {
        val w = weightKg.toDoubleOrNull() ?: 0.0
        val hM = (heightCm.toDoubleOrNull() ?: 0.0) / 100.0

        val bmi = if (w > 0 && hM > 0) w / (hM * hM) else 0.0
        val category = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }

        _uiState.update {
            it.copy(
                bmiWeightKg = weightKg,
                bmiHeightCm = heightCm,
                bmiScore = bmi,
                bmiCategory = category
            )
        }
    }

    fun setPercentInputs(a: String, b: String) {
        _uiState.update { it.copy(percentValA = a, percentValB = b) }
        updatePercentageCalculation()
    }

    fun setTipParams(bill: String, percent: Double, people: Int, roundUp: Boolean) {
        _uiState.update {
            it.copy(
                tipBill = bill,
                tipPercent = percent,
                tipPeopleCount = people.coerceIn(1, 100),
                tipRoundUp = roundUp
            )
        }
        updateTipCalculation()
    }

    fun setAgeBirthMillis(millis: Long) {
        setAgeBirthDate(millis)
    }

    fun setDateRange(startMillis: Long, endMillis: Long) {
        _uiState.update { it.copy(dateStartMillis = startMillis, dateEndMillis = endMillis) }
        updateDateCalculation()
    }

    fun setDiscountParams(original: String, discount: String, tax: String) {
        setDiscountInputs(original, discount, tax)
    }

    // ----------------------------------------------------
    // Settings & Modal Screen Handlers
    // ----------------------------------------------------
    fun toggleHapticFeedback() {
        _uiState.update { it.copy(hapticFeedbackEnabled = !it.hapticFeedbackEnabled) }
    }

    fun toggleSound() {
        _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun toggleVibration() {
        _uiState.update { it.copy(vibrationEnabled = !it.vibrationEnabled) }
    }

    fun setLanguage(lang: String) {
        _uiState.update { it.copy(currentLanguage = lang) }
    }

    fun dismissSplash() {
        _uiState.update { it.copy(showSplash = false) }
    }

    fun openSettings() {
        _uiState.update { it.copy(isSettingsOpen = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(isSettingsOpen = false) }
    }

    fun openThemes() {
        _uiState.update { it.copy(isThemesOpen = true) }
    }

    fun closeThemes() {
        _uiState.update { it.copy(isThemesOpen = false) }
    }

    fun openAbout() {
        _uiState.update { it.copy(isAboutOpen = true) }
    }

    fun closeAbout() {
        _uiState.update { it.copy(isAboutOpen = false) }
    }

    // ----------------------------------------------------
    // Clipboard Utility
    // ----------------------------------------------------
    fun copyToClipboard(text: String, label: String = "Smart Calculator") {
        if (text.isNotBlank()) {
            val clip = ClipData.newPlainText(label, text)
            clipboardManager.setPrimaryClip(clip)
            showToast("Copied to clipboard: $text")
        }
    }

    private fun showToast(message: String) {
        viewModelScope.launch {
            _toastEvent.emit(message)
        }
    }
}
