package com.example.havadurumu.model

import com.example.havadurumu.testing.sampleForecast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanAdvisorTest {
    private val sunnyDay = DailyForecast("2026-10-26", max = 24, min = 15, condition = WeatherCondition.CLEAR, precipitationProbability = 0, windMax = 10.0)
    private val rainyColdDay = DailyForecast("2026-10-27", max = 14, min = 8, condition = WeatherCondition.RAIN, precipitationProbability = 80, windMax = 20.0, precipitationSum = 6.0)

    @Test
    fun `gunesli ve ilik gun piknik icin 10 puan alir`() {
        val result = PlanAdvisor.score(ActivityType.PICNIC, sunnyDay)

        assertEquals(10, result.score)
        assertEquals(listOf("Harika bir gün"), result.reasons)
    }

    @Test
    fun `yagmurlu ve serin gun piknik icin dusuk puan alir ve nedenini soyler`() {
        val result = PlanAdvisor.score(ActivityType.PICNIC, rainyColdDay)

        assertEquals(1, result.score)
        assertTrue(result.reasons.any { it.contains("%80") })
        assertTrue(result.reasons.any { it.contains("serin") })
    }

    @Test
    fun `kahve bulusmasi yagmurdan az etkilenir`() {
        val result = PlanAdvisor.score(ActivityType.COFFEE, rainyColdDay)

        assertEquals(8, result.score)
    }

    @Test
    fun `servis yagmurlu deyip ihtimali dusuk gosterse bile puan yuksek cikmaz`() {
        val lightRain = DailyForecast("2026-10-26", max = 19, min = 14, condition = WeatherCondition.RAIN, precipitationProbability = 18, windMax = 14.8, precipitationSum = 0.1)

        val result = PlanAdvisor.score(ActivityType.PICNIC, lightRain)

        assertEquals(6, result.score)
        assertTrue(result.reasons.any { it.contains("Hafif yağmur") })
        assertTrue(result.reasons.none { it.contains("Harika") })
    }

    @Test
    fun `kapali hava piknikten biraz puan goturur`() {
        val cloudy = sunnyDay.copy(condition = WeatherCondition.CLOUDY)

        val result = PlanAdvisor.score(ActivityType.PICNIC, cloudy)

        assertEquals(9, result.score)
        assertTrue(result.reasons.any { it.contains("kapalı") })
    }

    @Test
    fun `firtina acik hava etkinliginden ekstra puan goturur`() {
        val stormy = sunnyDay.copy(condition = WeatherCondition.STORM)

        val result = PlanAdvisor.score(ActivityType.RUN, stormy)

        assertTrue(result.score < 10)
        assertTrue(result.reasons.any { it.contains("Fırtına") })
    }

    @Test
    fun `daha iyi bir gun varsa onerilir`() {
        val forecast = sampleForecast(daily = listOf(rainyColdDay, sunnyDay.copy(dateIso = "2026-10-28")))
        val plan = Plan(1, "Piknik", ActivityType.PICNIC, "Ankara", dateIso = "2026-10-27")

        val evaluation = PlanAdvisor.evaluate(plan, forecast, todayIso = "2026-10-26")

        assertNotNull(evaluation)
        assertEquals(1, evaluation!!.score.score)
        assertEquals("2026-10-28", evaluation.betterDay?.dateIso)
        assertEquals(10, evaluation.betterScore?.score)
    }

    @Test
    fun `gecmis gunler daha iyi gun olarak onerilmez`() {
        val forecast = sampleForecast(daily = listOf(sunnyDay, rainyColdDay))
        val plan = Plan(1, "Piknik", ActivityType.PICNIC, "Ankara", dateIso = "2026-10-27")

        val evaluation = PlanAdvisor.evaluate(plan, forecast, todayIso = "2026-10-27")

        assertNull(evaluation!!.betterDay)
    }

    @Test
    fun `planin gunu tahminde yoksa null doner`() {
        val forecast = sampleForecast(daily = listOf(sunnyDay))
        val plan = Plan(1, "Koşu", ActivityType.RUN, "Ankara", dateIso = "2026-12-31")

        assertNull(PlanAdvisor.evaluate(plan, forecast, todayIso = "2026-10-26"))
    }
}
