package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyApiService {
    @GET("v6/latest/{base}")
    suspend fun getExchangeRates(
        @Path("base") baseCurrency: String = "USD"
    ): CurrencyApiResponse
}
