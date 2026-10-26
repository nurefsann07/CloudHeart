package com.example.havadurumu.model

data class Plan(
    val id: Long,
    val title: String,
    val activity: ActivityType,
    val city: String,
    val dateIso: String
)

enum class ActivityType(
    val emoji: String,
    val label: String,
    val idealMin: Int,
    val idealMax: Int,
    val rainSensitivity: Double,
    val windSensitivity: Double,
    val outdoor: Boolean,
    val wantsSun: Boolean = false
) {
    PICNIC("🧺", "Piknik", idealMin = 18, idealMax = 28, rainSensitivity = 7.0, windSensitivity = 2.0, outdoor = true, wantsSun = true),
    RUN("🏃‍♀️", "Koşu", idealMin = 8, idealMax = 20, rainSensitivity = 5.0, windSensitivity = 1.5, outdoor = true),
    BEACH("🏖️", "Sahil", idealMin = 25, idealMax = 35, rainSensitivity = 8.0, windSensitivity = 2.0, outdoor = true, wantsSun = true),
    CITY_WALK("🚶‍♀️", "Şehir gezisi", idealMin = 12, idealMax = 26, rainSensitivity = 5.0, windSensitivity = 1.0, outdoor = true),
    COFFEE("☕", "Kahve buluşması", idealMin = -10, idealMax = 40, rainSensitivity = 2.0, windSensitivity = 0.0, outdoor = false)
}
