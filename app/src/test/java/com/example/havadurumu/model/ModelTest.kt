package com.example.havadurumu.model

import com.example.havadurumu.testing.sampleForecast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherConditionTest {
    @Test
    fun `WMO kodlari dogru gorunume cevrilir`() {
        assertEquals(WeatherCondition.CLEAR, WeatherCondition.fromWeatherCode(0))
        assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherCondition.fromWeatherCode(2))
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWeatherCode(63))
        assertEquals(WeatherCondition.SNOW, WeatherCondition.fromWeatherCode(75))
        assertEquals(WeatherCondition.STORM, WeatherCondition.fromWeatherCode(95))
        assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromWeatherCode(-1))
    }

    @Test
    fun `gece acik havada ay gosterilir`() {
        assertEquals("🌙", WeatherCondition.CLEAR.emojiFor(isDay = false))
        assertEquals("☀️", WeatherCondition.CLEAR.emojiFor(isDay = true))
    }
}

class DatesTest {
    @Test
    fun `gun adlari dogru bulunur`() {
        assertEquals("Pazar", Dates.dayName("2026-10-25"))
        assertEquals("Pazartesi", Dates.dayName("2026-10-26"))
        assertEquals("Cuma", Dates.dayName("2027-01-01"))
    }

    @Test
    fun `bugun ve yarin ozel yazilir`() {
        assertEquals("Bugün", Dates.relativeName("2026-10-25", todayIso = "2026-10-25"))
        assertEquals("Yarın", Dates.relativeName("2026-10-26", todayIso = "2026-10-25"))
        assertEquals("Salı", Dates.relativeName("2026-10-27", todayIso = "2026-10-25"))
    }

    @Test
    fun `yil donumunde gunler dogru ilerler`() {
        assertEquals(
            listOf("2026-12-30", "2026-12-31", "2027-01-01"),
            Dates.upcomingDays(3, todayIso = "2026-12-30")
        )
    }

    @Test
    fun `kisa tarih gun ve ay gosterir`() {
        assertEquals("26.10", Dates.shortDate("2026-10-26"))
    }
}

class AdviceTest {
    @Test
    fun `yagmurda semsiye onerilir`() {
        val rainy = sampleForecast(temperature = 12, condition = WeatherCondition.RAIN)

        val lines = Advice.clothing(rainy.current, today = null)

        assertTrue(lines.any { it.contains("Şemsiye") })
    }

    @Test
    fun `gunesli ilik havada semsiye onerilmez`() {
        val sunny = sampleForecast(temperature = 20, condition = WeatherCondition.CLEAR)

        val lines = Advice.clothing(sunny.current, today = null)

        assertFalse(lines.any { it.contains("Şemsiye") })
    }

    @Test
    fun `istanbulda ve kadikoyde istanbula ozel oneriler cikar`() {
        val sunny = sampleForecast(temperature = 20, condition = WeatherCondition.CLEAR)
        val kadikoy = Place("Kadıköy", "İstanbul", 40.99, 29.02)
        val istanbulTips = LocalFlavor.tipsFor(kadikoy)!!

        val ideas = Advice.activities(sunny.current, kadikoy, "2026-10-26")

        assertEquals(3, ideas.size)
        assertTrue(ideas[0] in istanbulTips.outdoor)
        assertTrue(ideas[1] in istanbulTips.taste)
    }

    @Test
    fun `yagmurda sehrin kapali mekanlari onerilir`() {
        val rainy = sampleForecast(temperature = 12, condition = WeatherCondition.RAIN)
        val antep = Place("Gaziantep", null, 37.06, 37.38)

        val ideas = Advice.activities(rainy.current, antep, "2026-10-26")

        assertTrue(ideas[0] in LocalFlavor.tipsFor(antep)!!.indoor)
    }

    @Test
    fun `oneriler gunden gune degisir ama ayni gun sabit kalir`() {
        val sunny = sampleForecast(temperature = 20, condition = WeatherCondition.CLEAR)
        val izmir = Place("İzmir", null, 38.4, 27.1)

        val monday = Advice.activities(sunny.current, izmir, "2026-10-26")
        val week = (1..7).map { Advice.activities(sunny.current, izmir, "2026-11-0$it") }

        assertEquals(monday, Advice.activities(sunny.current, izmir, "2026-10-26"))
        assertTrue(week.any { it != monday })
    }

    @Test
    fun `karadenizde yagmurluk notu ve sert ruzgarda hissedilen sicaklik yazilir`() {
        val windy = CurrentWeather(temperature = 12, humidity = 70, windSpeed = 30.0, condition = WeatherCondition.CLOUDY, isDay = true)
        val rize = Place("Rize", null, 41.0, 40.5)

        val lines = Advice.clothing(windy, today = null, place = rize)

        assertTrue(lines.any { it.contains("9° gibi") })
        assertTrue(lines.any { it.contains("Karadeniz") })
    }

    @Test
    fun `anneye gece mesaji samimi ve hitap sadece bir kez gecer`() {
        val forecast = sampleForecast(name = "Ankara", temperature = 10, condition = WeatherCondition.PARTLY_CLOUDY)
            .let { it.copy(current = it.current.copy(isDay = false)) }
        val person = Person(1, "Annem", "👩", "Ankara", Relation.MOTHER)

        val message = Advice.loveMessage(person, forecast)
        val lines = message.lines()

        assertEquals("İyi geceler annecim", lines[0])
        assertTrue(lines[1].startsWith("Ankara'da şu an 10°"))
        assertEquals("Seni çok seviyorum 💕", lines.last())
        assertEquals(1, Regex("annecim").findAll(message).count())
    }

    @Test
    fun `arkadasa gunduz adiyla nasilsin diye sorulur`() {
        val forecast = sampleForecast(name = "İzmir", temperature = 24, condition = WeatherCondition.CLEAR)
        val person = Person(2, "Zeynep", "👯‍♀️", "İzmir", Relation.FRIEND)

        val message = Advice.loveMessage(person, forecast)

        assertTrue(message.startsWith("Zeynep, nasılsın?"))
        assertTrue(message.contains("İzmir'de"))
        assertTrue(message.endsWith("Öpüldün 😘"))
    }

    @Test
    fun `eski kayitlarda yakinlik addan tahmin edilir`() {
        assertEquals(Relation.MOTHER, Relation.guessFrom("Annem"))
        assertEquals(Relation.GRANDMOTHER, Relation.guessFrom("Babaannem"))
        assertEquals(Relation.FATHER, Relation.guessFrom("babam"))
        assertEquals(Relation.FRIEND, Relation.guessFrom("Zeynep"))
        assertEquals(Relation.MOTHER, Person(1, "Annem", "👩", "Ankara", relation = null).relationOrGuess)
    }
}

class TurkishTextTest {
    @Test
    fun `bulunma eki unlu uyumu ve sert unsuze gore secilir`() {
        assertEquals("Ankara'da", TurkishText.locative("Ankara"))
        assertEquals("İzmir'de", TurkishText.locative("İzmir"))
        assertEquals("Kadıköy'de", TurkishText.locative("Kadıköy"))
        assertEquals("Sinop'ta", TurkishText.locative("Sinop"))
        assertEquals("Bitlis'te", TurkishText.locative("Bitlis"))
        assertEquals("Kars'ta", TurkishText.locative("Kars"))
        assertEquals("İstanbul'da", TurkishText.locative("İstanbul"))
    }

    @Test
    fun `turkce karakterler aramada yok sayilir`() {
        assertEquals("kadikoy", TurkishText.normalize("Kadıköy"))
        assertEquals("istanbul", TurkishText.normalize(" İSTANBUL "))
        assertEquals("sanliurfa", TurkishText.normalize("Şanlıurfa"))
    }
}
