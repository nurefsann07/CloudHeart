package com.example.havadurumu.ui.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.havadurumu.data.PlaceNotFoundException
import com.example.havadurumu.data.PlaceRepository
import com.example.havadurumu.data.WeatherRepository
import com.example.havadurumu.data.local.PlanRepository
import com.example.havadurumu.model.ActivityType
import com.example.havadurumu.model.Dates
import com.example.havadurumu.model.Forecast
import com.example.havadurumu.model.Plan
import com.example.havadurumu.model.PlanAdvisor
import com.example.havadurumu.model.PlanEvaluation
import com.example.havadurumu.model.PlaceSuggestion
import com.example.havadurumu.ui.PlaceSuggestionSearch
import com.example.havadurumu.ui.LoadState
import com.example.havadurumu.ui.logError
import com.example.havadurumu.ui.people.cityKey
import com.example.havadurumu.ui.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class PlanCardState(
    val plan: Plan,
    val evaluation: LoadState<PlanEvaluation>
)

class PlansViewModel(
    private val planRepository: PlanRepository,
    private val weatherRepository: WeatherRepository,
    placeRepository: PlaceRepository,
    private val today: () -> String = Dates::todayIso
) : ViewModel() {
    private val citySearch = PlaceSuggestionSearch(viewModelScope, placeRepository, includeProvinces = true)
    val citySuggestions: StateFlow<List<PlaceSuggestion>> = citySearch.suggestions

    fun onCityQueryChange(query: String) {
        citySearch.onQueryChange(query)
    }

    private val forecastByCity = MutableStateFlow<Map<String, LoadState<Forecast>>>(emptyMap())

    val uiState: StateFlow<List<PlanCardState>> = combine(
        planRepository.plans,
        forecastByCity
    ) { plans, forecasts ->
        plans.map { plan ->
            PlanCardState(plan, evaluate(plan, forecasts[cityKey(plan.city)] ?: LoadState.Loading))
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            planRepository.plans.collect { plans ->
                plans.map { it.city }
                    .distinctBy(::cityKey)
                    .filter { cityKey(it) !in forecastByCity.value }
                    .forEach(::loadCity)
            }
        }
    }

    fun addPlan(title: String, activity: ActivityType, city: String, dateIso: String) {
        if (city.isBlank()) return
        planRepository.add(
            Plan(
                id = System.currentTimeMillis(),
                title = title.trim().ifBlank { activity.label },
                activity = activity,
                city = city.trim(),
                dateIso = dateIso
            )
        )
        citySearch.onQueryChange("")
    }

    fun removePlan(id: Long) {
        planRepository.remove(id)
    }

    fun retry(city: String) {
        loadCity(city)
    }

    private fun evaluate(plan: Plan, forecast: LoadState<Forecast>): LoadState<PlanEvaluation> =
        when (forecast) {
            is LoadState.Loading -> LoadState.Loading
            is LoadState.Empty -> LoadState.Empty
            is LoadState.Error -> LoadState.Error(forecast.message)
            is LoadState.Success -> PlanAdvisor.evaluate(plan, forecast.data, today())
                ?.let { LoadState.Success(it) }
                ?: LoadState.Empty
        }

    private fun loadCity(city: String) {
        val key = cityKey(city)
        forecastByCity.update { it + (key to LoadState.Loading) }
        viewModelScope.launch {
            val result: LoadState<Forecast> = try {
                LoadState.Success(weatherRepository.getForecast(city))
            } catch (e: PlaceNotFoundException) {
                LoadState.Empty
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logError(TAG, "Tahmin getirilemedi: $city", e)
                LoadState.Error(e.toUserMessage())
            }
            forecastByCity.update { it + (key to result) }
        }
    }

    private companion object {
        const val TAG = "PlansViewModel"
    }
}
