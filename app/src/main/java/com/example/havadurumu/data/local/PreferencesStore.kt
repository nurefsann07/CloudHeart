package com.example.havadurumu.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesStore(context: Context) {
    private val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun <T> readList(key: String, token: TypeToken<List<T>>): List<T> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return try {
            gson.fromJson<List<T>>(json, token.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun <T> writeList(key: String, items: List<T>) {
        prefs.edit().putString(key, gson.toJson(items)).apply()
    }

    private companion object {
        const val FILE_NAME = "cloudheart"
    }
}
