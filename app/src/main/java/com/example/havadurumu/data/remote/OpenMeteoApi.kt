package com.example.havadurumu.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "tr"
    ): GeocodingResponse
}

interface ForecastApi {
    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("hourly") hourly: String = HOURLY_FIELDS,
        @Query("daily") daily: String = DAILY_FIELDS,
        @Query("forecast_days") days: Int = FORECAST_DAYS,
        @Query("timezone") timezone: String = "auto"
    ): ForecastResponse

    @GET("v1/forecast")
    suspend fun currentMany(
        @Query("latitude", encoded = true) latitudes: String,
        @Query("longitude", encoded = true) longitudes: String,
        @Query("current") current: String = "temperature_2m,weather_code,is_day",
        @Query("timezone") timezone: String = "auto"
    ): List<MultiCurrentResponse>

    companion object {
        const val FORECAST_DAYS = 7

        const val CURRENT_FIELDS =
            "temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code,is_day"

        const val HOURLY_FIELDS = "temperature_2m,weather_code"

        const val DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min," +
                "precipitation_probability_max,wind_speed_10m_max,precipitation_sum"
    }
}
