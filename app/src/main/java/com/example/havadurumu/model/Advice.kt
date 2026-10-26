package com.example.havadurumu.model

import java.util.Locale
import kotlin.math.roundToInt

private val TURKISH: Locale = Locale.forLanguageTag("tr")

object Advice {
    fun clothing(current: CurrentWeather, today: DailyForecast?, place: Place? = null): List<String> {
        val pick = picker(place, today?.dateIso)
        val t = current.temperature

        val feels = t - (if (current.windSpeed >= 20) 3 else 0) - (if (current.humidity >= 85 && t <= 10) 1 else 0)

        val main = when {
            feels <= 0 -> pick(listOf("Kalın mont, bere, atkı ve eldiven", "Şişme mont ve yün bere; eldivenini unutma"))
            feels <= 8 -> pick(listOf("Mont ve kalın bir kazak", "Kaban, altına balıkçı yaka bir kazak"))
            feels <= 15 -> pick(listOf("Hırka ya da ince bir ceket", "Kot ceket ya da trençkot"))
            feels <= 22 -> pick(listOf("Uzun kollu bir üst, yanına ince bir hırka", "Sweatshirt ve rahat bir pantolon"))
            feels <= 28 -> pick(listOf("Tişört ve rahat bir pantolon", "Keten gömlek ya da hafif bir elbise"))
            else -> pick(listOf("İnce tişört, şort ya da elbise", "Açık renkli, ince ve bol kesim giysiler"))
        }
        val range = today?.let { it.max - it.min } ?: 0
        val region = place?.let { LocalFlavor.regionOf(it) }

        return buildList {
            add(main)
            if (feels < t - 2) add("Rüzgâr yüzünden ${feels}° gibi hissettiriyor, rüzgâr geçirmeyen bir şey giy")
            val rainLater = (today?.precipitationProbability ?: 0) >= 50
            when {
                current.condition.isSnowy -> add(pick(listOf("Su geçirmez bot giy", "Kaymayan tabanlı bot seç")))
                (today?.precipitationSum ?: 0.0) >= 5.0 -> add("Bol yağış var, yağmurluk ya da su geçirmez mont giy")
                current.condition.isWet || rainLater -> add(pick(listOf("Şemsiyeni unutma", "Şemsiyeni çantana koy, ıslanma")))
            }
            if (!current.isDay && t <= 18) add("Akşam serin, üstüne bir kat daha al")
            if (current.humidity >= 80 && t >= 25) add("Hava nemli, pamuklu ve bol kesim giysiler seç")
            if (t >= 25 && current.isDay && current.condition == WeatherCondition.CLEAR) {
                add(pick(listOf("Güneş kremi ve şapka", "Güneş gözlüğü ve güneş kremi")))
            }
            if (range >= 12) add("Gün içinde sıcaklık ${range}° değişiyor, katmanlı giyin")
            val note = when (region) {
                LocalFlavor.Region.KARADENIZ -> region.note
                LocalFlavor.Region.AKDENIZ, LocalFlavor.Region.GUNEYDOGU -> region.note.takeIf { t >= 25 }
                LocalFlavor.Region.IC_ANADOLU -> region.note.takeIf { range in 8..11 }
                LocalFlavor.Region.DOGU_ANADOLU -> region.note.takeIf { (today?.min ?: t) <= 8 }
                else -> null
            }
            note?.let { add(it) }
        }.distinct().take(4)
    }

    fun activities(current: CurrentWeather, place: Place? = null, dateIso: String? = null): List<String> {
        val c = current.condition
        val t = current.temperature
        val random = java.util.Random(seedOf(place, dateIso))
        val local = place?.let { LocalFlavor.tipsFor(it) }
        val badWeather = c == WeatherCondition.STORM || c.isSnowy || c.isWet || t <= 3

        val general = when {
            c == WeatherCondition.STORM -> listOf("Battaniye ve film maratonu", "Evde kek yap", "Masa oyunu gecesi düzenle", "Yarım kalan kitabını bitir")
            c.isSnowy -> listOf("Kardan adam yap", "Sıcak salep iç", "Kar fotoğrafları çek", "Sıcak çikolata ile film izle")
            c.isWet -> listOf("Kitap ve sıcak çikolata", "Bir müze gez", "Kafede ders çalış", "Yağmur sesi eşliğinde müzik dinle")
            !current.isDay -> listOf("Yıldızlara bak", "Arkadaşlarla akşam yemeği", "Günlüğünü yaz", "Sevdiğin bir diziyi aç", "Akşam yürüyüşüne çık")
            t >= 27 -> listOf("Dondurma molası ver", "Gölgede piknik yap", "Sahile ya da bir havuza git", "Akşamüstü serinliğinde yürü")
            t >= 15 -> listOf("Parkta yürüyüş", "Bisiklet turu", "Açık havada kahve", "Arkadaşlarınla piknik", "Fotoğraf yürüyüşüne çık")
            else -> listOf("Tempolu bir yürüyüş", "Sıcak bir çorba iç", "Sinemaya git", "Bir kafede sıcak bir şeyler iç")
        }

        val picks = mutableListOf<String>()
        if (local != null) {
            val spots = when {
                badWeather -> local.indoor
                !current.isDay -> emptyList()
                else -> local.outdoor
            }
            spots.shuffled(random).firstOrNull()?.let { picks += it }
            local.taste.shuffled(random).firstOrNull()?.let { picks += it }
        }
        picks += general.shuffled(random)
        return picks.distinct().take(3)
    }

    private fun seedOf(place: Place?, dateIso: String?): Long =
        ((place?.let { LocalFlavor.provinceKey(it) + it.name } ?: "") + (dateIso ?: "")).hashCode().toLong()

    private fun picker(place: Place?, dateIso: String?): (List<String>) -> String {
        val random = java.util.Random(seedOf(place, dateIso))
        return { options -> options[random.nextInt(options.size)] }
    }

    fun careTip(current: CurrentWeather): String {
        val c = current.condition
        val t = current.temperature
        return when {
            c == WeatherCondition.STORM -> "Fırtına var, dışarı çıkmasın"
            c.isSnowy -> "Kar yağıyor, sıkı giyinsin"
            c.isWet -> "Yağmur var, şemsiyesini alsın"
            t <= 5 -> "Hava çok soğuk, sıkı giyinsin"
            t >= 32 -> "Çok sıcak, bol su içsin"
            !current.isDay -> "Orada gece, iyi uykular dile"
            c == WeatherCondition.CLEAR -> "Hava güzel, dışarı çıksın"
            else -> "Ona bir merhaba de"
        }
    }

    fun loveMessage(person: Person, forecast: Forecast): String {
        val relation = person.relationOrGuess
        val current = forecast.current
        val c = current.condition
        val t = current.temperature
        val dear = relation.addressFor(person.name)

        val greeting = when {
            !current.isDay -> "İyi geceler $dear"
            relation == Relation.OTHER -> "Merhaba $dear"
            else -> "${dear.replaceFirstChar { it.titlecase(TURKISH) }}, nasılsın?"
        }
        val care = when {
            c == WeatherCondition.STORM -> "Fırtına var, mümkünse dışarı çıkma"
            c.isSnowy -> "Kar yağıyor, sıkı giyin ve sıcak bir şeyler iç"
            c.isWet -> "Yağmur var, şemsiyeni almayı unutma"
            t <= 5 -> "Çok soğuk, sıkı giyin de üşüme"
            t <= 15 -> "Serin, dışarı çıkarsan üstüne bir hırka al"
            t >= 32 -> "Çok sıcak, bol su iç ve güneşten korun"
            !current.isDay -> "Hava güzel, rahat bir uyku olsun"
            else -> "Hava çok güzel, biraz dışarı çıkıp keyfini çıkar"
        }

        return listOf(
            greeting,
            "${TurkishText.locative(forecast.place.name)} şu an ${t}°, hava ${c.label.lowercase(TURKISH)} ${current.emoji}",
            care,
            relation.closing
        ).joinToString("\n")
    }

    fun invitation(plan: Plan, evaluation: PlanEvaluation?): String {
        val dayText = "${Dates.relativeName(plan.dateIso)} (${Dates.shortDate(plan.dateIso)})"
        val weatherText = evaluation?.let {
            "\nHava: ${it.day.condition.emoji} ${it.day.max}° / ${it.day.min}° · " +
                "Uygunluk ${it.score.score}/10"
        } ?: ""
        return "${plan.activity.emoji} ${plan.title}\n" +
            "$dayText · ${plan.city}" +
            weatherText +
            "\nGeliyor musun? 💖"
    }

    fun windText(speed: Double): String = "${speed.roundToInt()} km/sa"
}
