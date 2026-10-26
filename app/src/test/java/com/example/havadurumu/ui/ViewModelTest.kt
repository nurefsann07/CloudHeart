package com.example.havadurumu.ui

import com.example.havadurumu.data.local.PlanRepository
import com.example.havadurumu.model.ActivityType
import com.example.havadurumu.model.DailyForecast
import com.example.havadurumu.model.District
import com.example.havadurumu.model.DistrictWeather
import com.example.havadurumu.model.Plan
import com.example.havadurumu.model.PlaceSuggestion
import com.example.havadurumu.model.TURKISH_CITIES
import com.example.havadurumu.model.WeatherCondition
import com.example.havadurumu.testing.FakeDistrictRepository
import com.example.havadurumu.testing.FakeFavoriteRepository
import com.example.havadurumu.testing.FakePlaceRepository
import com.example.havadurumu.testing.FakeWeatherRepository
import com.example.havadurumu.testing.MainDispatcherRule
import com.example.havadurumu.testing.sampleForecast
import com.example.havadurumu.ui.city.filterCities
import com.example.havadurumu.ui.forecast.ForecastViewModel
import com.example.havadurumu.ui.plans.PlansViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ForecastViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `veri gelirse Success durumuna gecer`() {
        val forecast = sampleForecast(name = "Ankara")
        val viewModel = ForecastViewModel("Ankara", FakeWeatherRepository(mapOf("Ankara" to forecast)), FakeDistrictRepository(), FakeFavoriteRepository())

        assertEquals(LoadState.Success(forecast), viewModel.uiState.value)
    }

    @Test
    fun `yer bulunamazsa Empty durumuna gecer`() {
        val viewModel = ForecastViewModel("Xyzabc", FakeWeatherRepository(), FakeDistrictRepository(), FakeFavoriteRepository())

        assertEquals(LoadState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `internet yoksa anlasilir bir hata mesaji gosterir`() {
        val viewModel = ForecastViewModel("Ankara", FakeWeatherRepository(error = IOException("no network")), FakeDistrictRepository(), FakeFavoriteRepository())

        val state = viewModel.uiState.value
        assertTrue(state is LoadState.Error)
        assertEquals("İnternet bağlantını kontrol et.", (state as LoadState.Error).message)
    }

    @Test
    fun `tekrar dene yeniden yukler`() {
        val repository = FakeWeatherRepository(error = IOException())
        val viewModel = ForecastViewModel("Ankara", repository, FakeDistrictRepository(), FakeFavoriteRepository())

        viewModel.load()

        assertEquals(2, repository.calls)
    }
}

class ForecastDistrictsTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val kadikoy = DistrictWeather(District("Kadıköy", "İstanbul", 40.98, 29.06), 16, WeatherCondition.CLOUDY, false)

    @Test
    fun `il ise once ilin havasi sonra ilceleri yuklenir`() {
        val viewModel = ForecastViewModel(
            "İstanbul",
            FakeWeatherRepository(mapOf("İstanbul" to sampleForecast(name = "İstanbul"))),
            FakeDistrictRepository(mapOf("İstanbul" to listOf(kadikoy))),
            FakeFavoriteRepository()
        )

        assertTrue(viewModel.uiState.value is LoadState.Success)
        assertEquals(LoadState.Success(listOf(kadikoy)), viewModel.districts.value)
    }

    @Test
    fun `ilce ise ilce bolumu gosterilmez`() {
        val viewModel = ForecastViewModel(
            "Kadıköy, İstanbul",
            FakeWeatherRepository(mapOf("Kadıköy, İstanbul" to sampleForecast(name = "Kadıköy"))),
            FakeDistrictRepository(mapOf("İstanbul" to listOf(kadikoy))),
            FakeFavoriteRepository()
        )

        assertEquals(null, viewModel.districts.value)
    }

    @Test
    fun `ilce de favoriye eklenebilir`() {
        val favorites = FakeFavoriteRepository()
        val viewModel = ForecastViewModel("İstanbul", FakeWeatherRepository(), FakeDistrictRepository(), favorites)

        viewModel.toggleFavorite("Kadıköy, İstanbul")

        assertEquals(setOf("kadikoy, istanbul"), viewModel.favoriteKeys.value)
    }
}

class CityFilterTest {
    @Test
    fun `arama buyuk kucuk harfe bakmaz`() {
        val result = filterCities(TURKISH_CITIES, "iz", favoritesOnly = false, favoriteKeys = emptySet())

        assertEquals(listOf("Denizli", "İzmir", "Rize"), result.map { it.name })
    }

    @Test
    fun `favoriler sekmesinde sadece favoriler gorunur`() {
        val result = filterCities(TURKISH_CITIES, "", favoritesOnly = true, favoriteKeys = setOf("ankara", "istanbul"))

        assertEquals(listOf("Ankara", "İstanbul"), result.map { it.name })
    }

    @Test
    fun `turkce karakter yazmadan da bulunur`() {
        assertEquals(listOf("İzmir"), filterCities(TURKISH_CITIES, "izmir", false, emptySet()).map { it.name })
        assertEquals(listOf("Muğla"), filterCities(TURKISH_CITIES, "MUGLA", false, emptySet()).map { it.name })
    }

    @Test
    fun `il onerileri harfle baslayanlardan secilir`() {
        assertEquals(listOf("Ankara", "Antalya"), provinceSuggestions("an").map { it.name }.take(2))
        assertEquals(listOf("Çanakkale", "Çankırı"), provinceSuggestions("can").map { it.name })
    }

    @Test
    fun `ayni yer iki kez onerilmez`() {
        val local = listOf(PlaceSuggestion("Ankara", null, "il"))
        val remote = listOf(PlaceSuggestion("Ankara", "Ankara", "il merkezi"), PlaceSuggestion("Ankara", "Kırşehir", "köy"))

        assertEquals(listOf("Ankara", "Ankara, Kırşehir"), mergeSuggestions(local, remote).map { it.displayName })
    }

    @Test
    fun `bos arama butun illeri gosterir`() {
        assertEquals(81, filterCities(TURKISH_CITIES, "  ", false, emptySet()).size)
    }
}

class PlansViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakePlanRepository(initial: List<Plan>) : PlanRepository {
        private val _plans = MutableStateFlow(initial)
        override val plans: StateFlow<List<Plan>> = _plans

        override fun add(plan: Plan) {
            _plans.value = _plans.value + plan
        }

        override fun remove(id: Long) {
            _plans.value = _plans.value.filterNot { it.id == id }
        }
    }

    @Test
    fun `plan tahmin gelince puanlanir`() {
        val day = DailyForecast("2026-10-26", 24, 15, WeatherCondition.CLEAR, 0, 10.0)
        val weather = FakeWeatherRepository(mapOf("Kadıköy" to sampleForecast(name = "Kadıköy", daily = listOf(day))))
        val plan = Plan(1, "Moda'da piknik", ActivityType.PICNIC, "Kadıköy", "2026-10-26")

        val viewModel = PlansViewModel(FakePlanRepository(listOf(plan)), weather, FakePlaceRepository(), today = { "2026-10-26" })

        val evaluation = viewModel.uiState.value.single().evaluation
        assertTrue(evaluation is LoadState.Success)
        assertEquals(10, (evaluation as LoadState.Success).data.score.score)
    }

    @Test
    fun `ayni sehirdeki planlar icin tahmin bir kez istenir`() {
        val weather = FakeWeatherRepository(mapOf("Ankara" to sampleForecast()))
        val plans = listOf(
            Plan(1, "Koşu", ActivityType.RUN, "Ankara", "2026-10-26"),
            Plan(2, "Kahve", ActivityType.COFFEE, "ankara", "2026-10-27")
        )

        PlansViewModel(FakePlanRepository(plans), weather, FakePlaceRepository(), today = { "2026-10-26" })

        assertEquals(1, weather.calls)
    }
}
