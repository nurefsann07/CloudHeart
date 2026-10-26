package com.example.havadurumu.ui.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.havadurumu.data.DistrictRepository
import com.example.havadurumu.data.PlaceNotFoundException
import com.example.havadurumu.data.WeatherRepository
import com.example.havadurumu.data.local.FavoriteRepository
import com.example.havadurumu.model.DistrictWeather
import com.example.havadurumu.model.Forecast
import com.example.havadurumu.model.TurkishText
import com.example.havadurumu.ui.LoadState
import com.example.havadurumu.ui.logError
import com.example.havadurumu.ui.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ForecastViewModel(
    val placeName: String,
    private val repository: WeatherRepository,
    private val districtRepository: DistrictRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoadState<Forecast>>(LoadState.Loading)
    val uiState: StateFlow<LoadState<Forecast>> = _uiState.asStateFlow()

    private val _districts = MutableStateFlow<LoadState<List<DistrictWeather>>?>(null)
    val districts: StateFlow<LoadState<List<DistrictWeather>>?> = _districts.asStateFlow()

    val favoriteKeys: StateFlow<Set<String>> = favoriteRepository.favorites
        .map { list -> list.map(TurkishText::normalize).toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = LoadState.Loading
            _uiState.value = try {
                LoadState.Success(repository.getForecast(placeName))
            } catch (e: PlaceNotFoundException) {
                LoadState.Empty
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logError(TAG, "Tahmin getirilemedi: $placeName", e)
                LoadState.Error(e.toUserMessage())
            }
            if (_uiState.value is LoadState.Success && districtRepository.hasDistricts(placeName)) {
                loadDistricts()
            }
        }
    }

    fun loadDistricts() {
        viewModelScope.launch {
            _districts.value = LoadState.Loading
            _districts.value = try {
                val items = districtRepository.districtWeather(placeName)
                if (items.isEmpty()) LoadState.Empty else LoadState.Success(items)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logError(TAG, "İlçeler getirilemedi: $placeName", e)
                LoadState.Error(e.toUserMessage())
            }
        }
    }

    fun toggleFavorite(name: String) {
        favoriteRepository.toggle(name)
    }

    private companion object {
        const val TAG = "ForecastViewModel"
    }
}
