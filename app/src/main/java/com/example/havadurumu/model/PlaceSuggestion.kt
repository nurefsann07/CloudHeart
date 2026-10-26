package com.example.havadurumu.model

data class PlaceSuggestion(
    val name: String,
    val region: String?,
    val kind: String
) {
    val displayName: String
        get() = listOfNotNull(name, region).distinctBy(TurkishText::normalize).joinToString(", ")

    val key: String get() = TurkishText.normalize(displayName)
}
