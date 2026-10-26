package com.example.havadurumu.data.local

import com.example.havadurumu.model.Person
import com.example.havadurumu.model.Plan
import com.example.havadurumu.model.Relation
import com.example.havadurumu.model.TURKISH_CITIES
import com.example.havadurumu.model.TurkishText
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface FavoriteRepository {
    val favorites: StateFlow<List<String>>
    fun toggle(placeName: String)
}

class LocalFavoriteRepository(private val store: PreferencesStore) : FavoriteRepository {
    private val _favorites = MutableStateFlow(load())
    override val favorites: StateFlow<List<String>> = _favorites.asStateFlow()

    override fun toggle(placeName: String) {
        val key = TurkishText.normalize(placeName)
        _favorites.update { current ->
            if (current.any { TurkishText.normalize(it) == key }) {
                current.filterNot { TurkishText.normalize(it) == key }
            } else {
                current + placeName.trim()
            }
        }
        store.writeList(KEY, _favorites.value)
        store.writeList(OLD_KEY, emptyList<Int>())
    }

    private fun load(): List<String> {
        val saved = store.readList(KEY, object : TypeToken<List<String>>() {})
        if (saved.isNotEmpty()) return saved
        return store.readList(OLD_KEY, object : TypeToken<List<Int>>() {})
            .mapNotNull { plate -> TURKISH_CITIES.firstOrNull { it.plate == plate }?.name }
    }

    private companion object {
        const val KEY = "favorite_places"
        const val OLD_KEY = "favorites"
    }
}

interface PersonRepository {
    val people: StateFlow<List<Person>>
    fun add(name: String, relation: Relation, city: String)
    fun remove(id: Long)
}

class LocalPersonRepository(private val store: PreferencesStore) : PersonRepository {
    private val _people = MutableStateFlow(
        store.readList(KEY, object : TypeToken<List<Person>>() {})
    )
    override val people: StateFlow<List<Person>> = _people.asStateFlow()

    override fun add(name: String, relation: Relation, city: String) {
        val person = Person(
            id = System.currentTimeMillis(),
            name = name.trim(),
            emoji = relation.emoji,
            city = city.trim(),
            relation = relation
        )
        _people.update { it + person }
        store.writeList(KEY, _people.value)
    }

    override fun remove(id: Long) {
        _people.update { list -> list.filterNot { it.id == id } }
        store.writeList(KEY, _people.value)
    }

    private companion object {
        const val KEY = "people"
    }
}

interface PlanRepository {
    val plans: StateFlow<List<Plan>>
    fun add(plan: Plan)
    fun remove(id: Long)
}

class LocalPlanRepository(private val store: PreferencesStore) : PlanRepository {
    private val _plans = MutableStateFlow(
        store.readList(KEY, object : TypeToken<List<Plan>>() {})
    )
    override val plans: StateFlow<List<Plan>> = _plans.asStateFlow()

    override fun add(plan: Plan) {
        _plans.update { (it + plan).sortedBy { p -> p.dateIso } }
        store.writeList(KEY, _plans.value)
    }

    override fun remove(id: Long) {
        _plans.update { list -> list.filterNot { it.id == id } }
        store.writeList(KEY, _plans.value)
    }

    private companion object {
        const val KEY = "plans"
    }
}
