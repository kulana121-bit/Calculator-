package com.example.engine

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

data class DateDiffResult(
    val totalDays: Long,
    val years: Int,
    val months: Int,
    val days: Int,
    val totalWeeks: Long,
    val remainingDaysOfWeek: Long,
    val workingDays: Long,
    val weekendDays: Long
)

object DateCalculatorEngine {

    val DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val DATE_ISO_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun formatDate(timeMillis: Long): String {
        return DATE_FORMAT.format(Date(timeMillis))
    }

    fun calculateDifference(startMillis: Long, endMillis: Long): DateDiffResult {
        val (earlier, later) = if (startMillis <= endMillis) {
            startMillis to endMillis
        } else {
            endMillis to startMillis
        }

        val calStart = Calendar.getInstance().apply {
            timeInMillis = earlier
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val calEnd = Calendar.getInstance().apply {
            timeInMillis = later
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMillis = calEnd.timeInMillis - calStart.timeInMillis
        val totalDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

        // Working days vs weekend calculation
        var workingDays = 0L
        var weekendDays = 0L
        val tempCal = calStart.clone() as Calendar

        while (tempCal.before(calEnd)) {
            val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                weekendDays++
            } else {
                workingDays++
            }
            tempCal.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Years, Months, Days breakdown
        var years = 0
        var months = 0
        var days = 0

        val breakdownCal = calStart.clone() as Calendar
        while (true) {
            val nextYear = breakdownCal.clone() as Calendar
            nextYear.add(Calendar.YEAR, 1)
            if (!nextYear.after(calEnd)) {
                breakdownCal.add(Calendar.YEAR, 1)
                years++
            } else {
                break
            }
        }

        while (true) {
            val nextMonth = breakdownCal.clone() as Calendar
            nextMonth.add(Calendar.MONTH, 1)
            if (!nextMonth.after(calEnd)) {
                breakdownCal.add(Calendar.MONTH, 1)
                months++
            } else {
                break
            }
        }

        while (true) {
            val nextDay = breakdownCal.clone() as Calendar
            nextDay.add(Calendar.DAY_OF_MONTH, 1)
            if (!nextDay.after(calEnd)) {
                breakdownCal.add(Calendar.DAY_OF_MONTH, 1)
                days++
            } else {
                break
            }
        }

        val totalWeeks = totalDays / 7
        val remainingDaysOfWeek = totalDays % 7

        return DateDiffResult(
            totalDays = totalDays,
            years = years,
            months = months,
            days = days,
            totalWeeks = totalWeeks,
            remainingDaysOfWeek = remainingDaysOfWeek,
            workingDays = workingDays,
            weekendDays = weekendDays
        )
    }

    fun modifyDate(
        baseMillis: Long,
        years: Int,
        months: Int,
        weeks: Int,
        days: Int,
        isAdd: Boolean
    ): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = baseMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val multiplier = if (isAdd) 1 else -1
        if (years != 0) cal.add(Calendar.YEAR, years * multiplier)
        if (months != 0) cal.add(Calendar.MONTH, months * multiplier)
        if (weeks != 0) cal.add(Calendar.WEEK_OF_YEAR, weeks * multiplier)
        if (days != 0) cal.add(Calendar.DAY_OF_MONTH, days * multiplier)

        return cal.timeInMillis
    }
}
