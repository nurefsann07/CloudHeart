package com.example.havadurumu.ui

import com.example.havadurumu.data.PlaceRepository
import com.example.havadurumu.model.City
import com.example.havadurumu.model.PlaceSuggestion
import com.example.havadurumu.model.TURKISH_CITIES
import com.example.havadurumu.model.TurkishText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class PlaceSuggestionSearch(
    scope: CoroutineScope,
    private val repository: PlaceRepository,
    private val includeProvinces: Boolean,
    private val debounceMs: Long = 500
) {
    private val query = MutableStateFlow("")

    private val _suggestions = MutableStateFlow<List<PlaceSuggestion>>(emptyList())
    val suggestions: StateFlow<List<PlaceSuggestion>> = _suggestions.asStateFlow()

    init {
        scope.launch {
            query.collectLatest { q ->
                val local = if (includeProvinces) provinceSuggestions(q) else emptyList()
                _suggestions.value = local
                if (TurkishText.normalize(q).length < MIN_REMOTE_LENGTH) return@collectLatest

                delay(debounceMs)
                val remote = try {
                    repository.suggest(q)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logError("PlaceSuggestionSearch", "Öneriler alınamadı: $q", e)
                    emptyList()
                }
                _suggestions.value = mergeSuggestions(local, remote)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    private companion object {
        const val MIN_REMOTE_LENGTH = 3
    }
}

internal fun provinceSuggestions(query: String, cities: List<City> = TURKISH_CITIES): List<PlaceSuggestion> {
    val q = TurkishText.normalize(query)
    if (q.isEmpty()) return emptyList()
    return cities
        .filter { TurkishText.normalize(it.name).startsWith(q) }
        .take(3)
        .map { PlaceSuggestion(name = it.name, region = null, kind = "il") }
}

internal fun mergeSuggestions(local: List<PlaceSuggestion>, remote: List<PlaceSuggestion>): List<PlaceSuggestion> =
    (local + remote).distinctBy { it.key }.take(PlaceRepository.MAX_SUGGESTIONS)

internal fun PlaceSuggestion.isProvince(cities: List<City> = TURKISH_CITIES): Boolean =
    cities.any { TurkishText.normalize(it.name) == key }
