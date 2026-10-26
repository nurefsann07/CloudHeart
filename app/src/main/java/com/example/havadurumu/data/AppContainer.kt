package com.example.havadurumu.data

import android.content.Context
import com.example.havadurumu.data.local.FavoriteRepository
import com.example.havadurumu.data.local.LocalFavoriteRepository
import com.example.havadurumu.data.local.LocalPersonRepository
import com.example.havadurumu.data.local.LocalPlanRepository
import com.example.havadurumu.data.local.PersonRepository
import com.example.havadurumu.data.local.PlanRepository
import com.example.havadurumu.data.local.PreferencesStore
import com.example.havadurumu.data.remote.ApiClient

object AppContainer {
    private lateinit var store: PreferencesStore
    private lateinit var appContext: Context

    fun init(context: Context) {
        if (!::store.isInitialized) {
            appContext = context.applicationContext
            store = PreferencesStore(appContext)
        }
    }

    val districtRepository: DistrictRepository by lazy {
        val catalog = runCatching {
            val json = appContext.assets.open("districts.json").bufferedReader().use { it.readText() }
            DistrictCatalog.parse(json)
        }.getOrDefault(emptyMap())
        CatalogDistrictRepository(catalog, ApiClient.forecastApi)
    }

    val placeRepository: PlaceRepository by lazy {
        OnlinePlaceRepository(ApiClient.nominatimApi, ApiClient.geocodingApi, districtRepository)
    }

    val weatherRepository: WeatherRepository by lazy {
        OpenMeteoWeatherRepository(placeRepository, ApiClient.forecastApi)
    }

    val favoriteRepository: FavoriteRepository by lazy { LocalFavoriteRepository(store) }

    val personRepository: PersonRepository by lazy { LocalPersonRepository(store) }

    val planRepository: PlanRepository by lazy { LocalPlanRepository(store) }
}
