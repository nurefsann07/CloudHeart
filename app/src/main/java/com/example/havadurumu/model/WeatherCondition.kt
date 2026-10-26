package com.example.havadurumu.model

enum class WeatherCondition(
    val emoji: String,
    val label: String,
    val nightEmoji: String = emoji
) {
    CLEAR("☀️", "Açık", nightEmoji = "🌙"),
    PARTLY_CLOUDY("⛅", "Parçalı bulutlu", nightEmoji = "☁️"),
    CLOUDY("☁️", "Kapalı"),
    FOG("🌫️", "Sisli"),
    DRIZZLE("🌦️", "Çiseleme"),
    RAIN("🌧️", "Yağmurlu"),
    SNOW("❄️", "Karlı"),
    SHOWERS("🌧️", "Sağanak"),
    SNOW_SHOWERS("🌨️", "Kar sağanağı"),
    STORM("⛈️", "Fırtına"),
    UNKNOWN("🌈", "Bilinmiyor");

    val isWet: Boolean get() = this == DRIZZLE || this == RAIN || this == SHOWERS || this == STORM

    val isSnowy: Boolean get() = this == SNOW || this == SNOW_SHOWERS

    fun emojiFor(isDay: Boolean): String = if (isDay) emoji else nightEmoji

    companion object {
        fun fromWeatherCode(code: Int): WeatherCondition = when (code) {
            0 -> CLEAR
            1, 2 -> PARTLY_CLOUDY
            3 -> CLOUDY
            45, 48 -> FOG
            in 51..57 -> DRIZZLE
            in 61..67 -> RAIN
            in 71..77 -> SNOW
            in 80..82 -> SHOWERS
            85, 86 -> SNOW_SHOWERS
            in 95..99 -> STORM
            else -> UNKNOWN
        }
    }
}
