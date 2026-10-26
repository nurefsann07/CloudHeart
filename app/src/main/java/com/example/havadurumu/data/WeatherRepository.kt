package com.example.havadurumu.data

import com.example.havadurumu.data.remote.ForecastApi
import com.example.havadurumu.data.remote.toForecast
import com.example.havadurumu.model.Forecast
import com.example.havadurumu.model.TurkishText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

interface WeatherRepository {
    suspend fun getForecast(placeName: String): Forecast
}

class PlaceNotFoundException(placeName: String) : Exception("Yer bulunamadı: $placeName")

class OpenMeteoWeatherRepository(
    private val placeRepository: PlaceRepository,
    private val forecastApi: ForecastApi,
    private val now: () -> Long = System::currentTimeMillis
) : WeatherRepository {
    private data class CacheEntry(val forecast: Forecast, val savedAt: Long)

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    override suspend fun getForecast(placeName: String): Forecast = withContext(Dispatchers.IO) {
        val key = TurkishText.normalize(placeName)

        cache[key]?.let { entry ->
            if (now() - entry.savedAt < CACHE_DURATION_MS) return@withContext entry.forecast
        }

        val place = placeRepository.find(placeName) ?: throw PlaceNotFoundException(placeName)

        val forecast = forecastApi
            .forecast(latitude = place.latitude, longitude = place.longitude)
            .toForecast(place)

        cache[key] = CacheEntry(forecast, now())
        forecast
    }

    private companion object {
        const val CACHE_DURATION_MS = 10 * 60 * 1000L
    }
}
