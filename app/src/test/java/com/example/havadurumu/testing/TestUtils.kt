package com.example.havadurumu.testing

import com.example.havadurumu.data.DistrictRepository
import com.example.havadurumu.data.PlaceNotFoundException
import com.example.havadurumu.data.local.FavoriteRepository
import com.example.havadurumu.model.DistrictWeather
import com.example.havadurumu.model.TurkishText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.havadurumu.data.PlaceRepository
import com.example.havadurumu.data.WeatherRepository
import com.example.havadurumu.model.CurrentWeather
import com.example.havadurumu.model.DailyForecast
import com.example.havadurumu.model.Forecast
import com.example.havadurumu.model.Place
import com.example.havadurumu.model.PlaceSuggestion
import com.example.havadurumu.model.WeatherCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class FakeWeatherRepository(
    private val forecasts: Map<String, Forecast> = emptyMap(),
    private val error: Exception? = null
) : WeatherRepository {
    var calls = 0
        private set

    override suspend fun getForecast(placeName: String): Forecast {
        calls++
        error?.let { throw it }
        return forecasts[placeName] ?: throw PlaceNotFoundException(placeName)
    }
}

class FakePlaceRepository(
    private val suggestions: List<PlaceSuggestion> = emptyList()
) : PlaceRepository {
    override suspend fun find(query: String): Place? = null
    override suspend fun suggest(query: String): List<PlaceSuggestion> = suggestions
}

class FakeDistrictRepository(
    private val districts: Map<String, List<DistrictWeather>> = emptyMap()
) : DistrictRepository {
    override fun hasDistricts(province: String) = districts.containsKey(province)
    override suspend fun districtWeather(province: String) = districts[province].orEmpty()
}

class FakeFavoriteRepository(initial: List<String> = emptyList()) : FavoriteRepository {
    private val _favorites = MutableStateFlow(initial)
    override val favorites: StateFlow<List<String>> = _favorites

    override fun toggle(placeName: String) {
        val key = TurkishText.normalize(placeName)
        _favorites.value = if (_favorites.value.any { TurkishText.normalize(it) == key }) {
            _favorites.value.filterNot { TurkishText.normalize(it) == key }
        } else {
            _favorites.value + placeName
        }
    }
}

fun sampleForecast(
    name: String = "Ankara",
    temperature: Int = 20,
    condition: WeatherCondition = WeatherCondition.CLEAR,
    daily: List<DailyForecast> = emptyList()
) = Forecast(
    place = Place(name, null, 39.9, 32.8),
    current = CurrentWeather(temperature, 50, 10.0, condition, isDay = true),
    hourly = emptyList(),
    daily = daily
)
