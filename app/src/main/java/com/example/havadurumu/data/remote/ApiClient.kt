package com.example.havadurumu.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
    private const val FORECAST_BASE_URL = "https://api.open-meteo.com/"
    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"

    val nominatimApi: NominatimApi by lazy { createApi(NOMINATIM_BASE_URL) }

    val geocodingApi: GeocodingApi by lazy { createApi(GEOCODING_BASE_URL) }

    val forecastApi: ForecastApi by lazy { createApi(FORECAST_BASE_URL) }

    private inline fun <reified T> createApi(baseUrl: String): T = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(T::class.java)
}
