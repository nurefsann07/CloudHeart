package com.example.havadurumu.data

import com.example.havadurumu.data.remote.CurrentResponse
import com.example.havadurumu.data.remote.DailyResponse
import com.example.havadurumu.data.remote.ForecastApi
import com.example.havadurumu.data.remote.ForecastResponse
import com.example.havadurumu.data.remote.GeocodingApi
import com.example.havadurumu.data.remote.GeocodingResponse
import com.example.havadurumu.data.remote.GeocodingResult
import com.example.havadurumu.data.remote.HourlyResponse
import com.example.havadurumu.data.remote.MultiCurrentResponse
import com.example.havadurumu.data.remote.SimpleCurrentResponse
import com.example.havadurumu.data.remote.NominatimAddress
import com.example.havadurumu.data.remote.NominatimApi
import com.example.havadurumu.data.remote.NominatimResult
import com.example.havadurumu.data.remote.toDaily
import com.example.havadurumu.data.remote.toHourly
import com.example.havadurumu.model.Place
import com.example.havadurumu.model.PlaceSuggestion
import com.example.havadurumu.model.WeatherCondition
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class ForecastMapperTest {
    @Test
    fun `saatlik tahmin su anki saatten baslar`() {
        val hourly = HourlyResponse(
            time = listOf("2026-10-25T08:00", "2026-10-25T09:00", "2026-10-25T10:00"),
            temperatures = listOf(10.0, 11.4, 12.6),
            weatherCodes = listOf(0, 61, 3)
        )

        val rows = hourly.toHourly(currentTime = "2026-10-25T09:45")

        assertEquals(2, rows.size)
        assertEquals("Şimdi", rows[0].label)
        assertEquals(11, rows[0].temperature)
        assertEquals(WeatherCondition.RAIN, rows[0].condition)
        assertEquals("10:00", rows[1].label)
        assertEquals(13, rows[1].temperature)
    }

    @Test
    fun `gunluk sutunlar ayni gune ait satirlara donusur`() {
        val daily = DailyResponse(
            time = listOf("2026-10-25", "2026-10-26"),
            weatherCodes = listOf(0, 95),
            maxTemperatures = listOf(20.4, 15.0),
            minTemperatures = listOf(10.0, 8.6),
            precipitationProbabilities = listOf(10, null),
            maxWindSpeeds = null
        )

        val rows = daily.toDaily()

        assertEquals(2, rows.size)
        assertEquals("2026-10-26", rows[1].dateIso)
        assertEquals(15, rows[1].max)
        assertEquals(9, rows[1].min)
        assertEquals(WeatherCondition.STORM, rows[1].condition)
        assertEquals(10, rows[0].precipitationProbability)
        assertEquals(0, rows[1].precipitationProbability)
        assertEquals(0.0, rows[0].windMax, 0.001)
    }
}

class PlaceRepositoryTest {
    private class FakeGeocodingApi(private val results: List<GeocodingResult>?) : GeocodingApi {
        override suspend fun search(name: String, count: Int, language: String) = GeocodingResponse(results)
    }

    private class FakeNominatimApi(private val results: List<NominatimResult>? = emptyList()) : NominatimApi {
        override suspend fun search(
            query: String,
            format: String,
            countryCodes: String,
            limit: Int,
            language: String,
            addressDetails: Int
        ): List<NominatimResult> = results ?: throw IOException("Nominatim kapalı")
    }

    private val kadikoyIstanbul = NominatimResult(
        name = "Kadıköy",
        lat = "40.99",
        lon = "29.02",
        addresstype = "town",
        address = NominatimAddress(province = "İstanbul", state = null, town = "Kadıköy", cityDistrict = null)
    )

    private val babadag = GeocodingResult("Babadağ", 37.8, 28.8, "TR", "Denizli", "PPLA2")
    private val kadikoyYalova = GeocodingResult("Kadıköy", 40.62, 29.22, "TR", "Yalova", "PPL")
    private val kadikoyAydin = GeocodingResult("Kadıköy", 37.82, 27.81, "TR", "Aydın", "PPL")
    private val airport = GeocodingResult("Kadıköy Havalimanı", 1.0, 1.0, "TR", "İstanbul", "AIRP")

    @Test
    fun `once OpenStreetMap sonucu kullanilir ve il adi eklenir`() = runTest {
        val repository = OnlinePlaceRepository(FakeNominatimApi(listOf(kadikoyIstanbul)), FakeGeocodingApi(null))

        val place = repository.find("Kadıköy")

        assertEquals(40.99, place!!.latitude, 0.001)
        assertEquals("Kadıköy, İstanbul", place.displayName)
    }

    @Test
    fun `OpenStreetMap cevap vermezse yedek aramada adi birebir eslesen yer secilir`() = runTest {
        val repository = OnlinePlaceRepository(
            FakeNominatimApi(results = null),
            FakeGeocodingApi(listOf(babadag, kadikoyYalova))
        )

        val place = repository.find("Kadıköy")

        assertEquals("Kadıköy", place!!.name)
    }

    @Test
    fun `yedek aramada il yazildiysa o ildeki yer secilir`() = runTest {
        val repository = OnlinePlaceRepository(
            FakeNominatimApi(results = null),
            FakeGeocodingApi(listOf(babadag, kadikoyYalova, kadikoyAydin))
        )

        val place = repository.find("Kadıköy, Aydın")

        assertEquals("Aydın", place!!.region)
    }

    @Test
    fun `turkce karaktersiz yazim da eslesir`() = runTest {
        val repository = OnlinePlaceRepository(FakeNominatimApi(results = null), FakeGeocodingApi(listOf(kadikoyYalova)))

        assertEquals("Kadıköy", repository.find("kadikoy")!!.name)
    }

    @Test
    fun `yer bulunamazsa null doner`() = runTest {
        val repository = OnlinePlaceRepository(FakeNominatimApi(), FakeGeocodingApi(null))

        assertNull(repository.find("Xyzabc"))
    }

    @Test
    fun `oneriler alakasiz yerleri elerek iki servisi birlestirir`() = runTest {
        val repository = OnlinePlaceRepository(
            FakeNominatimApi(listOf(kadikoyIstanbul)),
            FakeGeocodingApi(listOf(babadag, kadikoyYalova, kadikoyAydin, airport))
        )

        val names = repository.suggest("Kadık").map { it.displayName }

        assertEquals(listOf("Kadıköy, İstanbul", "Kadıköy, Yalova", "Kadıköy, Aydın"), names)
    }

    @Test
    fun `mala yazinca alakasiz yerler degil ilce listesindeki Malazgirt cikar`() = runTest {
        val catalog = DistrictCatalog.parse(
            """[{"province":"Muş","districts":[{"name":"Malazgirt","lat":39.14,"lon":42.53}]},
               {"province":"Ankara","districts":[{"name":"Mamak","lat":39.92,"lon":32.92}]}]"""
        )
        val asiaMinor = NominatimResult("Asia Minor", "39.0", "33.0", "region", NominatimAddress("Ankara", null, null, null))
        val cevizlik = NominatimResult("Cevizlik", "40.9", "39.7", "village", NominatimAddress("Trabzon", null, null, null))
        val repository = OnlinePlaceRepository(
            FakeNominatimApi(listOf(asiaMinor, cevizlik)),
            FakeGeocodingApi(null),
            CatalogDistrictRepository(catalog, WeatherRepositoryTest.FakeForecastApi())
        )

        val names = repository.suggest("mala").map { it.displayName }

        assertEquals(listOf("Malazgirt, Muş"), names)
    }

    @Test
    fun `OpenStreetMap ilce turunu Turkce yazar`() = runTest {
        val repository = OnlinePlaceRepository(FakeNominatimApi(listOf(kadikoyIstanbul)), FakeGeocodingApi(null))

        assertEquals("ilçe", repository.suggest("Kadıköy").single().kind)
    }
}

class WeatherRepositoryTest {
    private val ankara = Place("Ankara", "Ankara", 39.9, 32.8)

    private class FakePlaceRepository(private val place: Place?) : PlaceRepository {
        var findCalls = 0
        override suspend fun find(query: String): Place? {
            findCalls++
            return place
        }
        override suspend fun suggest(query: String): List<PlaceSuggestion> = emptyList()
    }

    class FakeForecastApi : ForecastApi {
        var calls = 0
        var lastLatitude = 0.0

        override suspend fun forecast(
            latitude: Double,
            longitude: Double,
            current: String,
            hourly: String,
            daily: String,
            days: Int,
            timezone: String
        ): ForecastResponse {
            calls++
            lastLatitude = latitude
            return ForecastResponse(
                current = CurrentResponse("2026-10-25T09:00", 18.4, 60, 12.0, 0, 1),
                hourly = null,
                daily = null
            )
        }

        var multiCalls = 0
        var lastLatitudes = ""

        override suspend fun currentMany(
            latitudes: String,
            longitudes: String,
            current: String,
            timezone: String
        ): List<MultiCurrentResponse> {
            multiCalls++
            lastLatitudes = latitudes
            return latitudes.split(",").mapIndexed { i, _ ->
                MultiCurrentResponse(SimpleCurrentResponse(temperature = 10.0 + i, weatherCode = 3, isDay = 1))
            }
        }
    }

    @Test
    fun `bulunan yerin tahmini getirilir`() = runTest {
        val forecastApi = FakeForecastApi()
        val repository = OpenMeteoWeatherRepository(FakePlaceRepository(ankara), forecastApi)

        val forecast = repository.getForecast("Ankara")

        assertEquals(39.9, forecastApi.lastLatitude, 0.001)
        assertEquals(18, forecast.current.temperature)
        assertEquals("Ankara", forecast.place.displayName)
    }

    @Test
    fun `yer bulunamazsa PlaceNotFoundException firlatilir`() = runTest {
        val repository = OpenMeteoWeatherRepository(FakePlaceRepository(null), FakeForecastApi())

        try {
            repository.getForecast("Xyzabc")
            fail("Hata bekleniyordu")
        } catch (e: PlaceNotFoundException) {
        }
    }

    @Test
    fun `10 dakika icinde ayni yer tekrar istenirse internete gidilmez`() = runTest {
        var now = 0L
        val places = FakePlaceRepository(ankara)
        val forecastApi = FakeForecastApi()
        val repository = OpenMeteoWeatherRepository(places, forecastApi) { now }

        repository.getForecast("Ankara")
        now += 5 * 60 * 1000
        repository.getForecast("  ANKARA ")
        assertEquals(1, forecastApi.calls)
        assertEquals(1, places.findCalls)

        now += 6 * 60 * 1000
        repository.getForecast("Ankara")
        assertEquals(2, forecastApi.calls)
    }
}

class DistrictRepositoryTest {
    private val json = """
        [{"province":"İstanbul","districts":[
            {"name":"Üsküdar","lat":41.02,"lon":29.01},
            {"name":"Beşiktaş","lat":41.04,"lon":29.0},
            {"name":"Kadıköy","lat":40.98,"lon":29.06},
            {"name":"Çatalca","lat":41.14,"lon":28.46}
        ]}]
    """.trimIndent()

    @Test
    fun `ilceler Turk alfabesine gore siralanir`() {
        val catalog = DistrictCatalog.parse(json)

        assertEquals(listOf("Beşiktaş", "Çatalca", "Kadıköy", "Üsküdar"), catalog["istanbul"]!!.map { it.name })
    }

    @Test
    fun `butun ilcelerin havasi tek istekte ve dogru sirayla gelir`() = runTest {
        val api = WeatherRepositoryTest.FakeForecastApi()
        val repository = CatalogDistrictRepository(DistrictCatalog.parse(json), api)

        val items = repository.districtWeather("istanbul")

        assertEquals(1, api.multiCalls)
        assertEquals("41.04,41.14,40.98,41.02", api.lastLatitudes)
        assertEquals("Beşiktaş", items[0].district.name)
        assertEquals(10, items[0].temperature)
        assertEquals(13, items[3].temperature)
        assertEquals("Kadıköy, İstanbul", items[2].district.displayName)
    }

    @Test
    fun `il olmayan yerin ilcesi yoktur`() {
        val repository = CatalogDistrictRepository(DistrictCatalog.parse(json), WeatherRepositoryTest.FakeForecastApi())

        assertTrue(repository.hasDistricts("İstanbul"))
        assertFalse(repository.hasDistricts("Kadıköy, İstanbul"))
    }
}
