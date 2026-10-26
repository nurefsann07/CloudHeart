package com.example.havadurumu.ui

import android.util.Log
import retrofit2.HttpException
import java.io.IOException

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>

    data class Success<T>(val data: T) : LoadState<T>

    data object Empty : LoadState<Nothing>

    data class Error(val message: String) : LoadState<Nothing>
}

fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "İnternet bağlantını kontrol et."
    is HttpException -> "Sunucu şu an cevap vermiyor, birazdan tekrar dene."
    else -> "Bir şeyler ters gitti, tekrar dene."
}

fun logError(tag: String, message: String, error: Throwable) {
    runCatching { Log.e(tag, message, error) }
}
