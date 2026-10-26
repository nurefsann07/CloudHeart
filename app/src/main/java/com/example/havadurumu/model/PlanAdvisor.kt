package com.example.havadurumu.model

import kotlin.math.min
import kotlin.math.roundToInt

data class PlanScore(
    val score: Int,
    val reasons: List<String>
)

data class PlanEvaluation(
    val placeName: String,
    val day: DailyForecast,
    val score: PlanScore,
    val betterDay: DailyForecast?,
    val betterScore: PlanScore?
)

object PlanAdvisor {
    private const val BETTER_DAY_MARGIN = 2

    fun score(activity: ActivityType, day: DailyForecast): PlanScore {
        var points = 10.0
        val reasons = mutableListOf<String>()

        val rain = rainRisk(day)
        points -= rain / 100.0 * activity.rainSensitivity
        when {
            day.condition.isWet && day.precipitationSum < 1.0 -> reasons += "Hafif yağmur ihtimali"
            day.condition.isWet -> reasons += "Yağmur bekleniyor (%$rain)"
            rain >= 40 -> reasons += "Yağış ihtimali %$rain"
        }
        if (activity.outdoor && day.condition.isWet) points -= 1

        val tooCold = activity.idealMin - day.max
        val tooHot = day.max - activity.idealMax
        if (tooCold > 0) {
            points -= min(tooCold * 0.5, 5.0)
            if (tooCold >= 3) reasons += "${activity.label} için serin (en yüksek ${day.max}°)"
        }
        if (tooHot > 0) {
            points -= min(tooHot * 0.5, 5.0)
            if (tooHot >= 3) reasons += "Çok sıcak olabilir (${day.max}°)"
        }

        if (day.windMax >= 30) {
            points -= activity.windSensitivity
            if (activity.windSensitivity > 0) {
                reasons += "Rüzgâr sert (${day.windMax.roundToInt()} km/sa)"
            }
        }

        if (activity.wantsSun && (day.condition == WeatherCondition.CLOUDY || day.condition == WeatherCondition.FOG)) {
            points -= 1
            reasons += "Hava kapalı, güneş az"
        }

        if (activity.outdoor && day.condition == WeatherCondition.STORM) {
            points -= 3
            reasons += "Fırtına bekleniyor"
        }
        if (activity.outdoor && day.condition.isSnowy) {
            points -= 2
            reasons += "Kar bekleniyor"
        }

        val score = points.roundToInt().coerceIn(0, 10)
        if (reasons.isEmpty()) {
            reasons += if (score >= 8) "Harika bir gün" else "Fena değil"
        }
        return PlanScore(score, reasons)
    }

    fun rainRisk(day: DailyForecast): Int {
        val fromCondition = when {
            day.condition == WeatherCondition.STORM -> 90
            day.condition.isWet || day.condition.isSnowy -> if (day.precipitationSum >= 1.0) 70 else 40
            day.precipitationSum >= 1.0 -> 50
            else -> 0
        }
        return maxOf(day.precipitationProbability, fromCondition)
    }

    fun evaluate(plan: Plan, forecast: Forecast, todayIso: String): PlanEvaluation? {
        val day = forecast.daily.firstOrNull { it.dateIso == plan.dateIso } ?: return null
        val score = score(plan.activity, day)

        val best = forecast.daily
            .filter { it.dateIso >= todayIso && it.dateIso != plan.dateIso }
            .map { it to score(plan.activity, it) }
            .maxByOrNull { (_, s) -> s.score }
            ?.takeIf { (_, s) -> s.score >= score.score + BETTER_DAY_MARGIN }

        return PlanEvaluation(
            placeName = forecast.place.displayName,
            day = day,
            score = score,
            betterDay = best?.first,
            betterScore = best?.second
        )
    }
}
