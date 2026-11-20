package com.example.greenbuddy.model

import androidx.compose.ui.graphics.Color
import java.util.Calendar
import java.util.UUID

enum class PlantSpecies(val displayName: String, val defaultWaterDays: Int, val defaultFertilizerDays: Int) {
    ALOE("Aloe Vera", 14, 45),
    FICUS("Ficus", 8, 30),
    PILEA("Pilea", 5, 25),
    STRELITZIA("Strelitzia", 7, 30),
    SANSEVIERIA("Sansevieria", 12, 40);

    companion object {
        fun fromName(name: String): PlantSpecies {
            val alias = when (name.lowercase()) {
                "cactus", "cactuse", "aloe" -> ALOE
                "ficus" -> FICUS
                "basilic", "basllic", "pilea" -> PILEA
                "monstera", "strelitzia" -> STRELITZIA
                "pothos", "sansevieria" -> SANSEVIERIA
                else -> null
            }
            return alias ?: entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
                ?: ALOE
        }
    }
}

enum class SunExposure(val displayName: String, val waterIntervalModifierDays: Int) {
    LOW("Faible", 2),
    MEDIUM("Moyenne", 0),
    HIGH("Lumière vive", -1),
    DIRECT("Plein soleil", -2);

    companion object {
        fun fromName(name: String): SunExposure {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
                ?: MEDIUM
        }
    }
}

enum class SoilType(val displayName: String, val waterIntervalModifierDays: Int) {
    UNIVERSAL("Terreau universel", 0),
    CACTUS_MIX("Mélange cactées", -1),
    LIGHT("Terreau léger", 0),
    DRAINING("Terreau drainant", -1);

    companion object {
        fun fromName(name: String): SoilType {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
                ?: UNIVERSAL
        }
    }
}

enum class HealthStatus(
    val label: String,
    val textColor: Color,
    val backgroundColor: Color
) {
    EN_FORME("En forme", Color(0xFF2E7D32), Color(0xFFE8F5E9)),
    ASSOIFFEE("Assoiffée", Color(0xFFE65100), Color(0xFFFFF3E0)),
    FLETRIE("Flétrie", Color(0xFFC62828), Color(0xFFFFEBEE))
}

enum class CareType(val displayName: String) {
    WATERING("Arrosage"),
    FERTILIZATION("Fertilisation")
}

data class CareLog(
    val id: String = UUID.randomUUID().toString(),
    val dateMillis: Long = System.currentTimeMillis(),
    val type: CareType = CareType.WATERING,
    val note: String = ""
)

data class GrowthRecord(
    val id: String = UUID.randomUUID().toString(),
    val dateMillis: Long = System.currentTimeMillis(),
    val heightCm: Float,
    val note: String = ""
)

data class UserAccount(
    val name: String,
    val email: String
)

data class Plant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val species: PlantSpecies,
    val location: String,
    val acquisitionDateMillis: Long = System.currentTimeMillis(),
    val sunExposure: SunExposure = SunExposure.MEDIUM,
    val soilType: SoilType = SoilType.UNIVERSAL,
    val lastWateredMillis: Long = System.currentTimeMillis(),
    val lastFertilizedMillis: Long = System.currentTimeMillis(),
    val careCount: Int = 0,
    val growthLevel: Int = 1, // 1: Jeune pousse, 2: Plante en croissance, 3: Plante adulte épanouie
    val growthHistory: List<GrowthRecord> = emptyList(),
    val careLogs: List<CareLog> = emptyList(),
    val notes: String = ""
) {
    fun getWateringIntervalDays(): Int {
        val calculated = species.defaultWaterDays + sunExposure.waterIntervalModifierDays + soilType.waterIntervalModifierDays
        return calculated.coerceAtLeast(1)
    }

    fun getFertilizerIntervalDays(): Int {
        return species.defaultFertilizerDays
    }

    fun getNextWateringDateMillis(): Long {
        return lastWateredMillis + getWateringIntervalDays() * 86_400_000L
    }

    fun getNextFertilizingDateMillis(): Long {
        return lastFertilizedMillis + getFertilizerIntervalDays() * 86_400_000L
    }

    fun getDaysOverdueWatering(nowMillis: Long = System.currentTimeMillis()): Int {
        val nextWatering = getNextWateringDateMillis()
        if (nowMillis <= nextWatering) return 0
        val diffMs = nowMillis - nextWatering
        return (diffMs / 86_400_000L).toInt()
    }

    fun getDaysOverdueFertilizing(nowMillis: Long = System.currentTimeMillis()): Int {
        val nextFertilizing = getNextFertilizingDateMillis()
        if (nowMillis <= nextFertilizing) return 0
        val diffMs = nowMillis - nextFertilizing
        return (diffMs / 86_400_000L).toInt()
    }

    fun getHealthStatus(nowMillis: Long = System.currentTimeMillis()): HealthStatus {
        val daysOverdue = getDaysOverdueWatering(nowMillis)
        return when {
            daysOverdue <= 1 -> HealthStatus.EN_FORME
            daysOverdue in 2..4 -> HealthStatus.ASSOIFFEE
            else -> HealthStatus.FLETRIE
        }
    }

    fun getGrowthStageLabel(): String {
        return when (growthLevel) {
            1 -> "Niveau 1 · Jeune pousse"
            2 -> "Niveau 2 · Plante vigoureuse"
            else -> "Niveau 3 · Plante adulte épanouie"
        }
    }
}

enum class MessageSender {
    USER, BOT
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val plantContextId: String? = null
)

data class AppSettings(
    val currentUserEmail: String? = null,
    val currentUserName: String? = null,
    val reminderTime: String = "08:00",
    val notificationsEnabled: Boolean = true,
    val darkMode: Boolean? = null, // null = System, true = Dark, false = Light
    val cloudBackup: Boolean = false,
    val onboardingCompleted: Boolean = false
) {
    val isLoggedIn: Boolean get() = !currentUserEmail.isNull_or_empty_compat()
}

private fun String?.isNull_or_empty_compat(): Boolean = this == null || this.trim().isEmpty()
