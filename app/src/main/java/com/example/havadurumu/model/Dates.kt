package com.example.havadurumu.model

import java.util.Calendar
import java.util.Locale

object Dates {
    private val DAY_NAMES = listOf(
        "Pazar", "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi"
    )

    fun todayIso(): String = isoOf(Calendar.getInstance())

    fun upcomingDays(count: Int, todayIso: String = todayIso()): List<String> {
        val calendar = calendarOf(todayIso)
        return List(count) { index ->
            if (index > 0) calendar.add(Calendar.DAY_OF_MONTH, 1)
            isoOf(calendar)
        }
    }

    fun dayName(iso: String): String =
        DAY_NAMES[calendarOf(iso).get(Calendar.DAY_OF_WEEK) - 1]

    fun shortDate(iso: String): String {
        val parts = iso.split("-")
        return "${parts[2]}.${parts[1]}"
    }

    fun relativeName(iso: String, todayIso: String = todayIso()): String = when (iso) {
        todayIso -> "Bugün"
        upcomingDays(2, todayIso).last() -> "Yarın"
        else -> dayName(iso)
    }

    private fun calendarOf(iso: String): Calendar {
        val (year, month, day) = iso.split("-").map { it.toInt() }
        return Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day)
        }
    }

    private fun isoOf(calendar: Calendar): String = String.format(
        Locale.ROOT,
        "%04d-%02d-%02d",
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}
