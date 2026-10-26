package com.example.havadurumu.data

import com.example.havadurumu.data.remote.GeocodingApi
import com.example.havadurumu.data.remote.GeocodingResult
import com.example.havadurumu.data.remote.NominatimApi
import com.example.havadurumu.data.remote.NominatimResult
import com.example.havadurumu.data.remote.toPlace
import com.example.havadurumu.model.Place
import com.example.havadurumu.model.PlaceSuggestion
import com.example.havadurumu.model.TurkishText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

interface PlaceRepository {
    suspend fun find(query: String): Place?

    suspend fun suggest(query: String): List<PlaceSuggestion>

    companion object {
        const val MAX_SUGGESTIONS = 6
    }
}

class OnlinePlaceRepository(
    private val nominatimApi: NominatimApi,
    private val geocodingApi: GeocodingApi,
    private val districtRepository: DistrictRepository? = null,
    private val now: () -> Long = System::currentTimeMillis
) : PlaceRepository {
    private val nominatimLock = Mutex()
    private var lastNominatimCall = Long.MIN_VALUE / 2

    override suspend fun find(query: String): Place? = withContext(Dispatchers.IO) {
        val q = query.trim()
        val fromOsm = orNull { nominatim(q, limit = 1).firstOrNull()?.toPlace(fallbackName = q) }
        fromOsm ?: findWithOpenMeteo(q)
    }

    override suspend fun suggest(query: String): List<PlaceSuggestion> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isEmpty()) return@withContext emptyList()

        val normalizedQuery = TurkishText.normalize(q.substringBefore(","))

        val fromCatalog = districtRepository?.search(q).orEmpty()
            .map { PlaceSuggestion(name = it.name, region = it.province, kind = "ilçe") }

        val fromOsm = orNull { nominatim(q, limit = 5).mapNotNull { it.toSuggestion() } }.orEmpty()
            .filter { it.kind != "yer" }
            .filter { it.matches(normalizedQuery) }

        val fromOpenMeteo = orNull { geocodingApi.search(q).results }.orEmpty()
            .filter { it.countryCode == "TR" }
            .filter { it.featureCode?.startsWith("PPL") == true }
            .filter { TurkishText.normalize(it.name).startsWith(normalizedQuery) }
            .map { it.toSuggestion() }

        (fromCatalog + fromOsm + fromOpenMeteo)
            .distinctBy { it.key }
            .take(PlaceRepository.MAX_SUGGESTIONS)
    }

    private suspend fun findWithOpenMeteo(query: String): Place? {
        val name = query.substringBefore(",").trim()
        val region = query.substringAfter(",", missingDelimiterValue = "").trim()

        val results = geocodingApi.search(name).results.orEmpty()
        val inTurkey = results.filter { it.countryCode == "TR" }.ifEmpty { results }
        val exact = inTurkey.filter { TurkishText.normalize(it.name) == TurkishText.normalize(name) }
        val inRegion = if (region.isEmpty()) exact else {
            exact.filter { TurkishText.normalize(it.region.orEmpty()) == TurkishText.normalize(region) }
        }
        return (inRegion.ifEmpty { exact }.ifEmpty { inTurkey }).firstOrNull()?.toPlace()
    }

    private suspend fun nominatim(query: String, limit: Int): List<NominatimResult> =
        nominatimLock.withLock {
            val wait = NOMINATIM_INTERVAL_MS - (now() - lastNominatimCall)
            if (wait > 0) delay(wait)
            try {
                nominatimApi.search(query, limit = limit)
            } finally {
                lastNominatimCall = now()
            }
        }

    private inline fun <T> orNull(block: () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    private companion object {
        const val NOMINATIM_INTERVAL_MS = 1100L
    }
}

private fun PlaceSuggestion.matches(normalizedQuery: String): Boolean =
    TurkishText.normalize(name).split(" ", "-").any { it.startsWith(normalizedQuery) }

private fun NominatimResult.toSuggestion(): PlaceSuggestion? {
    val placeName = name?.takeIf { it.isNotBlank() } ?: return null
    val kind = when (addresstype) {
        "province", "state" -> "il"
        "city", "town", "municipality", "county", "district" -> "ilçe"
        "quarter", "suburb", "neighbourhood", "city_block" -> "semt"
        "village", "hamlet" -> "köy"
        else -> "yer"
    }

    val district = address?.town ?: address?.cityDistrict
    val detailedKind = if (kind == "semt" && district != null && district != placeName) {
        "$district semti"
    } else {
        kind
    }
    return PlaceSuggestion(
        name = placeName,
        region = address?.province ?: address?.state,
        kind = detailedKind
    )
}

private fun GeocodingResult.toSuggestion() = PlaceSuggestion(
    name = name,
    region = region,
    kind = when (featureCode) {
        "PPLA", "PPLC" -> "il merkezi"
        "PPLA2" -> "ilçe"
        "PPLX" -> "semt"
        else -> "yerleşim"
    }
)
