package com.example.havadurumu.data.remote

import com.example.havadurumu.model.CurrentWeather
import com.example.havadurumu.model.DailyForecast
import com.example.havadurumu.model.Forecast
import com.example.havadurumu.model.HourlyForecast
import com.example.havadurumu.model.Place
import com.example.havadurumu.model.WeatherCondition
import kotlin.math.roundToInt

const val HOURS_TO_SHOW = 24

fun GeocodingResult.toPlace(): Place = Place(
    name = name,
    region = region,
    latitude = latitude,
    longitude = longitude
)

fun NominatimResult.toPlace(fallbackName: String): Place? {
    val latitude = lat.toDoubleOrNull() ?: return null
    val longitude = lon.toDoubleOrNull() ?: return null
    return Place(
        name = name?.takeIf { it.isNotBlank() } ?: fallbackName,
        region = address?.province ?: address?.state,
        latitude = latitude,
        longitude = longitude
    )
}

fun ForecastResponse.toForecast(place: Place): Forecast {
    val now = checkNotNull(current) { "Tahmin yanıtında current bloğu yok" }
    return Forecast(
        place = place,
        current = now.toCurrentWeather(),
        hourly = hourly?.toHourly(currentTime = now.time).orEmpty(),
        daily = daily?.toDaily().orEmpty()
    )
}

private fun CurrentResponse.toCurrentWeather() = CurrentWeather(
    temperature = temperature.roundToInt(),
    humidity = humidity,
    windSpeed = windSpeed,
    condition = WeatherCondition.fromWeatherCode(weatherCode),
    isDay = isDay == 1
)

internal fun HourlyResponse.toHourly(currentTime: String): List<HourlyForecast> {
    val nowHour = currentTime.take(13)
    val start = time.indexOfFirst { it.take(13) == nowHour }.coerceAtLeast(0)
    val end = minOf(start + HOURS_TO_SHOW, time.size, temperatures.size, weatherCodes.size)
    return (start until end).map { i ->
        HourlyForecast(
            label = if (i == start) "Şimdi" else time[i].substring(11, 16),
            temperature = (temperatures[i] ?: 0.0).roundToInt(),
            condition = WeatherCondition.fromWeatherCode(weatherCodes[i] ?: -1)
        )
    }
}

internal fun DailyResponse.toDaily(): List<DailyForecast> {
    val count = minOf(time.size, maxTemperatures.size, minTemperatures.size, weatherCodes.size)
    return List(count) { i ->
        DailyForecast(
            dateIso = time[i],
            max = (maxTemperatures[i] ?: 0.0).roundToInt(),
            min = (minTemperatures[i] ?: 0.0).roundToInt(),
            condition = WeatherCondition.fromWeatherCode(weatherCodes[i] ?: -1),
            precipitationProbability = precipitationProbabilities?.getOrNull(i) ?: 0,
            windMax = maxWindSpeeds?.getOrNull(i) ?: 0.0,
            precipitationSum = precipitationSums?.getOrNull(i) ?: 0.0
        )
    }
}
