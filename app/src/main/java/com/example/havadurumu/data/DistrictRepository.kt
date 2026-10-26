package com.example.havadurumu.data

import com.example.havadurumu.data.remote.ForecastApi
import com.example.havadurumu.model.District
import com.example.havadurumu.model.DistrictWeather
import com.example.havadurumu.model.TurkishText
import com.example.havadurumu.model.WeatherCondition
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

interface DistrictRepository {
    fun hasDistricts(province: String): Boolean

    suspend fun districtWeather(province: String): List<DistrictWeather>

    fun search(query: String, limit: Int = 5): List<District> = emptyList()
}

class CatalogDistrictRepository(
    private val catalog: Map<String, List<District>>,
    private val forecastApi: ForecastApi,
    private val now: () -> Long = System::currentTimeMillis
) : DistrictRepository {
    private data class CacheEntry(val items: List<DistrictWeather>, val savedAt: Long)

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    private val allDistricts: List<District> by lazy { catalog.values.flatten() }

    override fun search(query: String, limit: Int): List<District> {
        val q = TurkishText.normalize(query.substringBefore(","))
        if (q.isEmpty()) return emptyList()
        return allDistricts.filter { TurkishText.normalize(it.name).startsWith(q) }.take(limit)
    }

    override fun hasDistricts(province: String): Boolean =
        catalog[TurkishText.normalize(province)].orEmpty().isNotEmpty()

    override suspend fun districtWeather(province: String): List<DistrictWeather> = withContext(Dispatchers.IO) {
        val key = TurkishText.normalize(province)
        cache[key]?.let { if (now() - it.savedAt < CACHE_DURATION_MS) return@withContext it.items }

        val districts = catalog[key].orEmpty()
        if (districts.isEmpty()) return@withContext emptyList()

        val query = if (districts.size == 1) districts + districts else districts
        val responses = forecastApi.currentMany(
            latitudes = query.joinToString(",") { it.latitude.toString() },
            longitudes = query.joinToString(",") { it.longitude.toString() }
        )

        val items = districts.mapIndexedNotNull { i, district ->
            val current = responses.getOrNull(i)?.current ?: return@mapIndexedNotNull null
            DistrictWeather(
                district = district,
                temperature = (current.temperature ?: return@mapIndexedNotNull null).roundToInt(),
                condition = WeatherCondition.fromWeatherCode(current.weatherCode ?: -1),
                isDay = current.isDay == 1
            )
        }
        cache[key] = CacheEntry(items, now())
        items
    }

    private companion object {
        const val CACHE_DURATION_MS = 10 * 60 * 1000L
    }
}

object DistrictCatalog {
    private class ProvinceJson(val province: String, val districts: List<DistrictJson>)
    private class DistrictJson(val name: String, @SerializedName("lat") val latitude: Double, @SerializedName("lon") val longitude: Double)

    fun parse(json: String): Map<String, List<District>> {
        val collator = Collator.getInstance(Locale.forLanguageTag("tr"))
        val provinces = Gson().fromJson(json, Array<ProvinceJson>::class.java).orEmpty()
        return provinces.associate { p ->
            TurkishText.normalize(p.province) to p.districts
                .map { District(it.name, p.province, it.latitude, it.longitude) }
                .sortedWith(compareBy(collator) { it.name })
        }
    }
}
