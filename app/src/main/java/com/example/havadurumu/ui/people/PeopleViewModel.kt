package com.example.havadurumu.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.havadurumu.data.PlaceNotFoundException
import com.example.havadurumu.data.PlaceRepository
import com.example.havadurumu.data.WeatherRepository
import com.example.havadurumu.data.local.PersonRepository
import com.example.havadurumu.model.Forecast
import com.example.havadurumu.model.Person
import com.example.havadurumu.model.PlaceSuggestion
import com.example.havadurumu.model.Relation
import com.example.havadurumu.model.TurkishText
import com.example.havadurumu.ui.PlaceSuggestionSearch
import com.example.havadurumu.ui.LoadState
import com.example.havadurumu.ui.logError
import com.example.havadurumu.ui.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class PersonCardState(
    val person: Person,
    val weather: LoadState<Forecast>
)

class PeopleViewModel(
    private val personRepository: PersonRepository,
    private val weatherRepository: WeatherRepository,
    placeRepository: PlaceRepository
) : ViewModel() {
    private val citySearch = PlaceSuggestionSearch(viewModelScope, placeRepository, includeProvinces = true)
    val citySuggestions: StateFlow<List<PlaceSuggestion>> = citySearch.suggestions

    fun onCityQueryChange(query: String) {
        citySearch.onQueryChange(query)
    }

    private val weatherByCity = MutableStateFlow<Map<String, LoadState<Forecast>>>(emptyMap())

    val uiState: StateFlow<List<PersonCardState>> = combine(
        personRepository.people,
        weatherByCity
    ) { people, weather ->
        people.map { person ->
            PersonCardState(person, weather[cityKey(person.city)] ?: LoadState.Loading)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            personRepository.people.collect { people ->
                people.map { it.city }
                    .distinctBy(::cityKey)
                    .filter { cityKey(it) !in weatherByCity.value }
                    .forEach(::loadCity)
            }
        }
    }

    fun addPerson(name: String, relation: Relation, city: String) {
        if (name.isBlank() || city.isBlank()) return
        personRepository.add(name, relation, city)
        citySearch.onQueryChange("")
    }

    fun removePerson(id: Long) {
        personRepository.remove(id)
    }

    fun retry(city: String) {
        loadCity(city)
    }

    private fun loadCity(city: String) {
        val key = cityKey(city)
        weatherByCity.update { it + (key to LoadState.Loading) }
        viewModelScope.launch {
            val result: LoadState<Forecast> = try {
                LoadState.Success(weatherRepository.getForecast(city))
            } catch (e: PlaceNotFoundException) {
                LoadState.Empty
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logError(TAG, "Hava getirilemedi: $city", e)
                LoadState.Error(e.toUserMessage())
            }
            weatherByCity.update { it + (key to result) }
        }
    }

    private companion object {
        const val TAG = "PeopleViewModel"
    }
}

internal fun cityKey(city: String): String = TurkishText.normalize(city)
