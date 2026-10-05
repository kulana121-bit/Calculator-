package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CurrencyApiResponse(
    val result: String? = null,
    val provider: String? = null,
    @Json(name = "base_code") val baseCode: String? = "USD",
    @Json(name = "time_last_update_utc") val timeLastUpdateUtc: String? = null,
    val rates: Map<String, Double>? = null
)

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val flag: String
)

object CurrencyData {
    val SUPPORTED_CURRENCIES: List<CurrencyInfo> get() = CURRENCIES

    val CURRENCIES = listOf(
        CurrencyInfo("USD", "United States Dollar", "$", "🇺🇸"),
        CurrencyInfo("EUR", "Euro", "€", "🇪🇺"),
        CurrencyInfo("GBP", "British Pound", "£", "🇬🇧"),
        CurrencyInfo("JPY", "Japanese Yen", "¥", "🇯🇵"),
        CurrencyInfo("CAD", "Canadian Dollar", "CA$", "🇨🇦"),
        CurrencyInfo("AUD", "Australian Dollar", "A$", "🇦🇺"),
        CurrencyInfo("CHF", "Swiss Franc", "CHF", "🇨🇭"),
        CurrencyInfo("CNY", "Chinese Yuan", "¥", "🇨🇳"),
        CurrencyInfo("INR", "Indian Rupee", "₹", "🇮🇳"),
        CurrencyInfo("KRW", "South Korean Won", "₩", "🇰🇷"),
        CurrencyInfo("SGD", "Singapore Dollar", "S$", "🇸🇬"),
        CurrencyInfo("BRL", "Brazilian Real", "R$", "🇧🇷"),
        CurrencyInfo("NZD", "New Zealand Dollar", "NZ$", "🇳🇿"),
        CurrencyInfo("MXN", "Mexican Peso", "Mex$", "🇲🇽"),
        CurrencyInfo("HKD", "Hong Kong Dollar", "HK$", "🇭🇰"),
        CurrencyInfo("SEK", "Swedish Krona", "kr", "🇸🇪"),
        CurrencyInfo("NOK", "Norwegian Krone", "kr", "🇳🇴"),
        CurrencyInfo("TRY", "Turkish Lira", "₺", "🇹🇷"),
        CurrencyInfo("ZAR", "South African Rand", "R", "🇿🇦"),
        CurrencyInfo("AED", "UAE Dirham", "د.إ", "🇦🇪"),
        CurrencyInfo("SAR", "Saudi Riyal", "﷼", "🇸🇦"),
        CurrencyInfo("THB", "Thai Baht", "฿", "🇹🇭"),
        CurrencyInfo("IDR", "Indonesian Rupiah", "Rp", "🇮🇩"),
        CurrencyInfo("MYR", "Malaysian Ringgit", "RM", "🇲🇾"),
        CurrencyInfo("PHP", "Philippine Peso", "₱", "🇵🇭"),
        CurrencyInfo("PLN", "Polish Zloty", "zł", "🇵🇱"),
        CurrencyInfo("ILS", "Israeli Shekel", "₪", "🇮🇱"),
        CurrencyInfo("DKK", "Danish Krone", "kr", "🇩🇰"),
        CurrencyInfo("CZK", "Czech Koruna", "Kč", "🇨🇿"),
        CurrencyInfo("HUF", "Hungarian Forint", "Ft", "🇭🇺"),
        CurrencyInfo("CLP", "Chilean Peso", "CLP$", "🇨🇱"),
        CurrencyInfo("TWD", "New Taiwan Dollar", "NT$", "🇹🇼"),
        CurrencyInfo("VND", "Vietnamese Dong", "₫", "🇻🇳"),
        CurrencyInfo("EGP", "Egyptian Pound", "E£", "🇪🇬"),
        CurrencyInfo("NGN", "Nigerian Naira", "₦", "🇳🇬")
    )

    // Robust offline fallback exchange rates (relative to USD base)
    val DEFAULT_RATES = mapOf(
        "USD" to 1.0,
        "EUR" to 0.92,
        "GBP" to 0.79,
        "JPY" to 153.5,
        "CAD" to 1.37,
        "AUD" to 1.54,
        "CHF" to 0.88,
        "CNY" to 7.24,
        "INR" to 85.35,
        "KRW" to 1380.0,
        "SGD" to 1.34,
        "BRL" to 5.65,
        "NZD" to 1.68,
        "MXN" to 19.8,
        "HKD" to 7.78,
        "SEK" to 10.6,
        "NOK" to 10.9,
        "TRY" to 34.2,
        "ZAR" to 17.6,
        "AED" to 3.67,
        "SAR" to 3.75,
        "THB" to 34.5,
        "IDR" to 15800.0,
        "MYR" to 4.45,
        "PHP" to 58.5,
        "PLN" to 4.02,
        "ILS" to 3.72,
        "DKK" to 6.87,
        "CZK" to 23.4,
        "HUF" to 370.0,
        "CLP" to 950.0,
        "TWD" to 32.2,
        "VND" to 25200.0,
        "EGP" to 49.2,
        "NGN" to 1640.0
    )

    fun getCurrency(code: String): CurrencyInfo {
        return CURRENCIES.find { it.code.equals(code, ignoreCase = true) }
            ?: CurrencyInfo(code, code, code, "🌐")
    }
}
