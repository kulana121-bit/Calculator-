package com.example.data.remote

import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class CurrencyRepository {

    private val apiService: CurrencyApiService by lazy {
        val moshi = Moshi.Builder()
            .build()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://open.er-api.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CurrencyApiService::class.java)
    }

    private var cachedRates: Map<String, Double> = CurrencyData.DEFAULT_RATES
    private var lastUpdatedTime: String = "Offline fallback"

    suspend fun fetchRates(base: String = "USD"): Result<Pair<Map<String, Double>, String>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getExchangeRates(base)
            if (response.rates != null && response.rates.isNotEmpty()) {
                cachedRates = response.rates
                lastUpdatedTime = response.timeLastUpdateUtc ?: "Updated just now"
                Result.success(Pair(cachedRates, lastUpdatedTime))
            } else {
                Result.success(Pair(cachedRates, lastUpdatedTime))
            }
        } catch (e: Exception) {
            // Graceful fallback to cached / offline default rates
            Result.success(Pair(cachedRates, "$lastUpdatedTime (Offline)"))
        }
    }

    fun convert(amount: Double, fromCode: String, toCode: String, rates: Map<String, Double>): Double {
        val fromRate = rates[fromCode] ?: CurrencyData.DEFAULT_RATES[fromCode] ?: 1.0
        val toRate = rates[toCode] ?: CurrencyData.DEFAULT_RATES[toCode] ?: 1.0

        if (fromRate == 0.0) return 0.0
        // Base rate is USD
        val inUsd = amount / fromRate
        return inUsd * toRate
    }
}
