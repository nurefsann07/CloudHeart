package com.example.havadurumu.model

data class Place(
    val name: String,
    val region: String?,
    val latitude: Double,
    val longitude: Double
) {
    val displayName: String
        get() = listOfNotNull(name, region).distinct().joinToString(", ")
}

data class CurrentWeather(
    val temperature: Int,
    val humidity: Int,
    val windSpeed: Double,
    val condition: WeatherCondition,
    val isDay: Boolean
) {
    val emoji: String get() = condition.emojiFor(isDay)
}

data class HourlyForecast(
    val label: String,
    val temperature: Int,
    val condition: WeatherCondition
)

data class DailyForecast(
    val dateIso: String,
    val max: Int,
    val min: Int,
    val condition: WeatherCondition,
    val precipitationProbability: Int,
    val windMax: Double,
    val precipitationSum: Double = 0.0
)

data class Forecast(
    val place: Place,
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>
) {
    val today: DailyForecast? get() = daily.firstOrNull()
}
