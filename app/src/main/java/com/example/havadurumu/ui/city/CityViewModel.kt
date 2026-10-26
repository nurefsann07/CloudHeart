package com.example.havadurumu.ui.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.havadurumu.data.PlaceRepository
import com.example.havadurumu.data.local.FavoriteRepository
import com.example.havadurumu.model.City
import com.example.havadurumu.model.PlaceSuggestion
import com.example.havadurumu.model.TURKISH_CITIES
import com.example.havadurumu.model.TurkishText
import com.example.havadurumu.ui.PlaceSuggestionSearch
import com.example.havadurumu.ui.isProvince
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CityUiState(
    val query: String = "",
    val favoritesOnly: Boolean = false,
    val favoriteKeys: Set<String> = emptySet(),
    val favoriteCount: Int = 0,
    val cities: List<City> = TURKISH_CITIES,
    val favoritePlaces: List<String> = emptyList(),
    val districts: List<PlaceSuggestion> = emptyList(),
    val lastSelected: String? = null
) {
    fun isFavorite(placeName: String): Boolean = TurkishText.normalize(placeName) in favoriteKeys
}

class CityViewModel(
    private val favoriteRepository: FavoriteRepository,
    placeRepository: PlaceRepository,
    private val allCities: List<City> = TURKISH_CITIES
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val favoritesOnly = MutableStateFlow(false)
    private val lastSelected = MutableStateFlow<String?>(null)

    private val districtSearch = PlaceSuggestionSearch(viewModelScope, placeRepository, includeProvinces = false)

    val uiState: StateFlow<CityUiState> = combine(
        query,
        favoritesOnly,
        favoriteRepository.favorites,
        lastSelected,
        districtSearch.suggestions
    ) { q, onlyFavorites, favorites, selected, suggestions ->
        val keys = favorites.map(TurkishText::normalize).toSet()
        val normalizedQuery = TurkishText.normalize(q)
        CityUiState(
            query = q,
            favoritesOnly = onlyFavorites,
            favoriteKeys = keys,
            favoriteCount = favorites.size,
            cities = filterCities(allCities, q, onlyFavorites, keys),

            favoritePlaces = if (!onlyFavorites) emptyList() else favorites
                .filterNot { name -> allCities.any { TurkishText.normalize(it.name) == TurkishText.normalize(name) } }
                .filter { TurkishText.normalize(it).contains(normalizedQuery) },

            districts = if (onlyFavorites || q.isBlank()) emptyList() else suggestions.filterNot { it.isProvince(allCities) },
            lastSelected = selected
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CityUiState(cities = allCities)
    )

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
        districtSearch.onQueryChange(newQuery)
    }

    fun onFavoritesOnlyChange(value: Boolean) {
        favoritesOnly.value = value
    }

    fun onToggleFavorite(placeName: String) {
        favoriteRepository.toggle(placeName)
    }

    fun onPlaceSelected(name: String) {
        lastSelected.value = name
    }
}

internal fun filterCities(
    cities: List<City>,
    query: String,
    favoritesOnly: Boolean,
    favoriteKeys: Set<String>
): List<City> {
    val q = TurkishText.normalize(query)
    return cities
        .filter { !favoritesOnly || TurkishText.normalize(it.name) in favoriteKeys }
        .filter { q.isEmpty() || TurkishText.normalize(it.name).contains(q) }
}
