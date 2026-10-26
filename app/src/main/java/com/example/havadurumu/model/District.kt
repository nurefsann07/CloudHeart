package com.example.havadurumu.model

data class District(
    val name: String,
    val province: String,
    val latitude: Double,
    val longitude: Double
) {
    val displayName: String get() = "$name, $province"
}

data class DistrictWeather(
    val district: District,
    val temperature: Int,
    val condition: WeatherCondition,
    val isDay: Boolean
) {
    val emoji: String get() = condition.emojiFor(isDay)
}
