package com.example.havadurumu.model

data class Person(
    val id: Long,
    val name: String,
    val emoji: String,
    val city: String,
    val relation: Relation? = null
) {
    val relationOrGuess: Relation get() = relation ?: Relation.guessFrom(name)
}

enum class Relation(
    val emoji: String,
    val label: String,
    val address: String?,
    val closing: String
) {
    MOTHER("👩", "Annem", "annecim", "Seni çok seviyorum 💕"),
    FATHER("👨", "Babam", "babacım", "Seni çok seviyorum 💙"),
    SIBLING("🧒", "Kardeşim", "kardeşim", "Kendine iyi bak 💖"),
    GRANDMOTHER("👵", "Anneannem", "nineciğim", "Ellerinden öperim, seni çok özledim 💞"),
    GRANDFATHER("👴", "Dedem", "dedeciğim", "Ellerinden öperim, seni çok özledim 💞"),
    PARTNER("💑", "Sevgilim", "aşkım", "Seni seviyorum 💘"),
    FRIEND("👯‍♀️", "Arkadaşım", null, "Öpüldün 😘"),
    OTHER("💖", "Diğer", null, "Sevgiler 💖");

    fun addressFor(name: String): String = address ?: name.trim()

    companion object {
        fun guessFrom(name: String): Relation {
            val n = TurkishText.normalize(name)
            return when {
                "anneanne" in n || "babaanne" in n || "nine" in n || "babanne" in n -> GRANDMOTHER
                "dede" in n -> GRANDFATHER
                "anne" in n || "anam" in n -> MOTHER
                "baba" in n -> FATHER
                "kardes" in n || "abla" in n || "abi" in n -> SIBLING
                "sevgil" in n || "ask" in n -> PARTNER
                else -> FRIEND
            }
        }
    }
}
