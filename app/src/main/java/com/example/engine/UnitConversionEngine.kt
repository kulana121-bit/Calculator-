package com.example.engine

import java.math.BigDecimal
import java.math.RoundingMode

enum class UnitCategory(val title: String, val iconName: String) {
    LENGTH("Length", "straighten"),
    WEIGHT("Weight", "scale"),
    TEMPERATURE("Temperature", "thermostat"),
    VOLUME("Volume", "water_drop"),
    SPEED("Speed", "speed"),
    AREA("Area", "crop_free"),
    DIGITAL("Data", "memory"),
    TIME("Time", "schedule")
}

data class UnitItem(
    val id: String,
    val name: String,
    val symbol: String,
    val category: UnitCategory,
    val toBaseRatio: Double = 1.0 // ratio multiplying to convert to base unit
)

object UnitConversionEngine {

    fun getUnitsForCategory(category: UnitCategory): List<UnitItem> = UNITS[category] ?: emptyList()

    val CATEGORIES = UnitCategory.values().toList()

    val UNITS: Map<UnitCategory, List<UnitItem>> = mapOf(
        UnitCategory.LENGTH to listOf(
            UnitItem("m", "Meter", "m", UnitCategory.LENGTH, 1.0),
            UnitItem("km", "Kilometer", "km", UnitCategory.LENGTH, 1000.0),
            UnitItem("cm", "Centimeter", "cm", UnitCategory.LENGTH, 0.01),
            UnitItem("mm", "Millimeter", "mm", UnitCategory.LENGTH, 0.001),
            UnitItem("um", "Micrometer", "μm", UnitCategory.LENGTH, 1e-6),
            UnitItem("nm", "Nanometer", "nm", UnitCategory.LENGTH, 1e-9),
            UnitItem("mi", "Mile", "mi", UnitCategory.LENGTH, 1609.344),
            UnitItem("yd", "Yard", "yd", UnitCategory.LENGTH, 0.9144),
            UnitItem("ft", "Foot", "ft", UnitCategory.LENGTH, 0.3048),
            UnitItem("in", "Inch", "in", UnitCategory.LENGTH, 0.0254),
            UnitItem("nmi", "Nautical Mile", "nmi", UnitCategory.LENGTH, 1852.0)
        ),
        UnitCategory.WEIGHT to listOf(
            UnitItem("kg", "Kilogram", "kg", UnitCategory.WEIGHT, 1.0),
            UnitItem("g", "Gram", "g", UnitCategory.WEIGHT, 0.001),
            UnitItem("mg", "Milligram", "mg", UnitCategory.WEIGHT, 1e-6),
            UnitItem("t", "Metric Ton", "t", UnitCategory.WEIGHT, 1000.0),
            UnitItem("lb", "Pound", "lb", UnitCategory.WEIGHT, 0.45359237),
            UnitItem("oz", "Ounce", "oz", UnitCategory.WEIGHT, 0.028349523125),
            UnitItem("st", "Stone", "st", UnitCategory.WEIGHT, 6.35029318)
        ),
        UnitCategory.TEMPERATURE to listOf(
            UnitItem("C", "Celsius", "°C", UnitCategory.TEMPERATURE, 1.0),
            UnitItem("F", "Fahrenheit", "°F", UnitCategory.TEMPERATURE, 1.0),
            UnitItem("K", "Kelvin", "K", UnitCategory.TEMPERATURE, 1.0)
        ),
        UnitCategory.VOLUME to listOf(
            UnitItem("L", "Liter", "L", UnitCategory.VOLUME, 1.0),
            UnitItem("mL", "Milliliter", "mL", UnitCategory.VOLUME, 0.001),
            UnitItem("m3", "Cubic Meter", "m³", UnitCategory.VOLUME, 1000.0),
            UnitItem("gal", "US Gallon", "gal", UnitCategory.VOLUME, 3.785411784),
            UnitItem("qt", "US Quart", "qt", UnitCategory.VOLUME, 0.946352946),
            UnitItem("pt", "US Pint", "pt", UnitCategory.VOLUME, 0.473176473),
            UnitItem("cup", "US Cup", "cup", UnitCategory.VOLUME, 0.2365882365),
            UnitItem("floz", "US Fl Ounce", "fl oz", UnitCategory.VOLUME, 0.0295735295625)
        ),
        UnitCategory.SPEED to listOf(
            UnitItem("mps", "Meter/second", "m/s", UnitCategory.SPEED, 1.0),
            UnitItem("kmh", "Kilometer/hour", "km/h", UnitCategory.SPEED, 0.2777777778),
            UnitItem("mph", "Mile/hour", "mph", UnitCategory.SPEED, 0.44704),
            UnitItem("knot", "Knot", "kn", UnitCategory.SPEED, 0.5144444444),
            UnitItem("fps", "Foot/second", "ft/s", UnitCategory.SPEED, 0.3048)
        ),
        UnitCategory.AREA to listOf(
            UnitItem("sqm", "Square Meter", "m²", UnitCategory.AREA, 1.0),
            UnitItem("sqkm", "Square Kilometer", "km²", UnitCategory.AREA, 1_000_000.0),
            UnitItem("sqft", "Square Foot", "ft²", UnitCategory.AREA, 0.09290304),
            UnitItem("sqyd", "Square Yard", "yd²", UnitCategory.AREA, 0.83612736),
            UnitItem("acre", "Acre", "ac", UnitCategory.AREA, 4046.8564224),
            UnitItem("ha", "Hectare", "ha", UnitCategory.AREA, 10000.0)
        ),
        UnitCategory.DIGITAL to listOf(
            UnitItem("B", "Byte", "B", UnitCategory.DIGITAL, 1.0),
            UnitItem("KB", "Kilobyte", "KB", UnitCategory.DIGITAL, 1024.0),
            UnitItem("MB", "Megabyte", "MB", UnitCategory.DIGITAL, 1024.0 * 1024.0),
            UnitItem("GB", "Gigabyte", "GB", UnitCategory.DIGITAL, 1024.0 * 1024.0 * 1024.0),
            UnitItem("TB", "Terabyte", "TB", UnitCategory.DIGITAL, 1024.0 * 1024.0 * 1024.0 * 1024.0),
            UnitItem("PB", "Petabyte", "PB", UnitCategory.DIGITAL, 1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0)
        ),
        UnitCategory.TIME to listOf(
            UnitItem("s", "Second", "s", UnitCategory.TIME, 1.0),
            UnitItem("ms", "Millisecond", "ms", UnitCategory.TIME, 0.001),
            UnitItem("min", "Minute", "min", UnitCategory.TIME, 60.0),
            UnitItem("h", "Hour", "h", UnitCategory.TIME, 3600.0),
            UnitItem("d", "Day", "d", UnitCategory.TIME, 86400.0),
            UnitItem("wk", "Week", "wk", UnitCategory.TIME, 604800.0),
            UnitItem("mo", "Month", "mo", UnitCategory.TIME, 2629746.0), // 30.436875 days
            UnitItem("yr", "Year", "yr", UnitCategory.TIME, 31556952.0)  // 365.2425 days
        )
    )

    fun convert(value: Double, fromUnit: UnitItem, toUnit: UnitItem): Double {
        if (fromUnit.id == toUnit.id) return value

        if (fromUnit.category == UnitCategory.TEMPERATURE && toUnit.category == UnitCategory.TEMPERATURE) {
            // Convert fromUnit to Celsius
            val celsius = when (fromUnit.id) {
                "C" -> value
                "F" -> (value - 32.0) * (5.0 / 9.0)
                "K" -> value - 273.15
                else -> value
            }
            // Convert Celsius to toUnit
            return when (toUnit.id) {
                "C" -> celsius
                "F" -> (celsius * 9.0 / 5.0) + 32.0
                "K" -> celsius + 273.15
                else -> celsius
            }
        }

        // Standard linear ratio conversion
        val baseValue = value * fromUnit.toBaseRatio
        return baseValue / toUnit.toBaseRatio
    }

    fun formatUnitValue(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        if (value == kotlin.math.floor(value) && kotlin.math.abs(value) < 1e12) {
            return value.toLong().toString()
        }
        val bd = BigDecimal(value).setScale(8, RoundingMode.HALF_UP).stripTrailingZeros()
        return bd.toPlainString()
    }
}
