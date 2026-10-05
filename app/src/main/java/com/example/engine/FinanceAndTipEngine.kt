package com.example.engine

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.ceil

data class TipResult(
    val tipAmount: Double,
    val totalAmount: Double,
    val tipPerPerson: Double,
    val totalPerPerson: Double
)

data class DiscountResult(
    val discountAmount: Double,
    val priceAfterDiscount: Double,
    val taxAmount: Double,
    val finalPrice: Double,
    val totalSavings: Double
)

object FinanceAndTipEngine {

    fun calculateTip(
        billAmount: Double,
        tipPercent: Double,
        splitCount: Int = 1,
        roundUp: Boolean = false
    ): TipResult {
        if (billAmount <= 0) {
            return TipResult(0.0, 0.0, 0.0, 0.0)
        }

        val people = if (splitCount < 1) 1 else splitCount
        val rawTip = billAmount * (tipPercent / 100.0)
        var total = billAmount + rawTip
        var tip = rawTip

        if (roundUp) {
            val roundedTotal = ceil(total)
            tip += (roundedTotal - total)
            total = roundedTotal
        }

        val totalPerPerson = total / people
        val tipPerPerson = tip / people

        return TipResult(
            tipAmount = round2(tip),
            totalAmount = round2(total),
            tipPerPerson = round2(tipPerPerson),
            totalPerPerson = round2(totalPerPerson)
        )
    }

    fun calculatePercentageOf(percent: Double, total: Double): Double {
        return round2(total * (percent / 100.0))
    }

    fun calculatePartOfTotal(part: Double, total: Double): Double {
        if (total == 0.0) return 0.0
        return round2((part / total) * 100.0)
    }

    fun calculatePercentageChange(oldValue: Double, newValue: Double): Double {
        if (oldValue == 0.0) return 0.0
        return round2(((newValue - oldValue) / oldValue) * 100.0)
    }

    fun calculateDiscountAndTax(
        originalPrice: Double,
        discountPercent: Double,
        taxPercent: Double
    ): DiscountResult {
        if (originalPrice <= 0.0) {
            return DiscountResult(0.0, 0.0, 0.0, 0.0, 0.0)
        }

        val discountAmount = originalPrice * (discountPercent.coerceIn(0.0, 100.0) / 100.0)
        val priceAfterDiscount = originalPrice - discountAmount
        val taxAmount = priceAfterDiscount * (taxPercent.coerceAtLeast(0.0) / 100.0)
        val finalPrice = priceAfterDiscount + taxAmount
        val totalSavings = discountAmount

        return DiscountResult(
            discountAmount = round2(discountAmount),
            priceAfterDiscount = round2(priceAfterDiscount),
            taxAmount = round2(taxAmount),
            finalPrice = round2(finalPrice),
            totalSavings = round2(totalSavings)
        )
    }

    private fun round2(value: Double): Double {
        if (value.isNaN() || value.isInfinite()) return 0.0
        return BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toDouble()
    }
}
