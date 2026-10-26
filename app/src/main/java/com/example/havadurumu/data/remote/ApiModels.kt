package com.example.havadurumu.data.remote

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    val results: List<GeocodingResult>?
)

data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("country_code") val countryCode: String?,
    @SerializedName("admin1") val region: String?,
    @SerializedName("feature_code") val featureCode: String? = null
)

data class MultiCurrentResponse(
    val current: SimpleCurrentResponse?
)

data class SimpleCurrentResponse(
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("is_day") val isDay: Int?
)

data class ForecastResponse(
    val current: CurrentResponse?,
    val hourly: HourlyResponse?,
    val daily: DailyResponse?
)

data class CurrentResponse(
    val time: String,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("is_day") val isDay: Int
)

data class HourlyResponse(
    val time: List<String>,
    @SerializedName("temperature_2m") val temperatures: List<Double?>,
    @SerializedName("weather_code") val weatherCodes: List<Int?>
)

data class DailyResponse(
    val time: List<String>,
    @SerializedName("weather_code") val weatherCodes: List<Int?>,
    @SerializedName("temperature_2m_max") val maxTemperatures: List<Double?>,
    @SerializedName("temperature_2m_min") val minTemperatures: List<Double?>,
    @SerializedName("precipitation_probability_max") val precipitationProbabilities: List<Int?>?,
    @SerializedName("wind_speed_10m_max") val maxWindSpeeds: List<Double?>?,
    @SerializedName("precipitation_sum") val precipitationSums: List<Double?>? = null
)
