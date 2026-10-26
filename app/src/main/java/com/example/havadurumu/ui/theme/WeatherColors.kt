package com.example.havadurumu.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.havadurumu.model.WeatherCondition

fun weatherColors(condition: WeatherCondition, isDay: Boolean): List<Color> = when {
    !isDay -> listOf(Color(0xFF5C6BC0), Color(0xFF9575CD))
    condition == WeatherCondition.CLEAR -> listOf(Color(0xFFFFB74D), Pink)
    condition == WeatherCondition.PARTLY_CLOUDY ||
        condition == WeatherCondition.CLOUDY ||
        condition == WeatherCondition.FOG -> listOf(Color(0xFFBA68C8), Blue)
    condition.isSnowy -> listOf(Color(0xFF4FC3F7), Color(0xFFBA68C8))
    condition == WeatherCondition.STORM -> listOf(Color(0xFF5C6BC0), Color(0xFF455A64))
    condition.isWet -> listOf(Color(0xFF42A5F5), Color(0xFF7986CB))
    else -> listOf(Pink, Blue)
}

val BrandColors: List<Color> = listOf(Pink, Blue)
