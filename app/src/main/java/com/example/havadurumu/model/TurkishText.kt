package com.example.havadurumu.model

import java.util.Locale

object TurkishText {
    private val TURKISH: Locale = Locale.forLanguageTag("tr")

    fun normalize(text: String): String = text.trim()
        .lowercase(TURKISH)
        .replace('ı', 'i')
        .replace('ö', 'o')
        .replace('ü', 'u')
        .replace('ş', 's')
        .replace('ç', 'c')
        .replace('ğ', 'g')
        .replace('â', 'a')
        .replace('î', 'i')
        .replace('û', 'u')

    fun locative(word: String): String {
        val lower = word.trim().lowercase(TURKISH)
        val lastVowel = lower.lastOrNull { it in "aıoueiöü" }
        val vowel = if (lastVowel != null && lastVowel in "eiöü") 'e' else 'a'
        val consonant = if (lower.lastOrNull()?.let { it in "fstkçşhp" } == true) 't' else 'd'
        return "${word.trim()}'$consonant$vowel"
    }
}
