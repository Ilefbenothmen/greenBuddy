package com.example.greenbuddy

import com.example.greenbuddy.model.*
import com.example.greenbuddy.ui.screens.generateBotResponse
import org.junit.Assert.*
import org.junit.Test

class GreenBuddyLogicTest {

    @Test
    fun testPlantWateringIntervalCalculation() {
        val strelitzia = Plant(
            name = "Stella",
            species = PlantSpecies.STRELITZIA,
            location = "Salon",
            sunExposure = SunExposure.HIGH, // -1 day
            soilType = SoilType.UNIVERSAL // 0 day
        )
        // Strelitzia base water days = 7, -1 = 6 days
        assertEquals(6, strelitzia.getWateringIntervalDays())

        val aloe = Plant(
            name = "Pépito",
            species = PlantSpecies.ALOE,
            location = "Balcon",
            sunExposure = SunExposure.DIRECT, // -2 days
            soilType = SoilType.CACTUS_MIX // -1 day
        )
        // Aloe base water days = 14, -2 - 1 = 11 days
        assertEquals(11, aloe.getWateringIntervalDays())
    }

    @Test
    fun testHealthStatusCalculation() {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        // Watered 6 days ago for Strelitzia with 6 days interval -> 0 days overdue -> EN_FORME
        val healthyPlant = Plant(
            name = "Stella",
            species = PlantSpecies.STRELITZIA,
            location = "Salon",
            sunExposure = SunExposure.HIGH,
            soilType = SoilType.UNIVERSAL,
            lastWateredMillis = now - (6 * dayMs)
        )
        assertEquals(0, healthyPlant.getDaysOverdueWatering(now))
        assertEquals(HealthStatus.EN_FORME, healthyPlant.getHealthStatus(now))

        val thirstyPlant = Plant(
            name = "Pépito",
            species = PlantSpecies.ALOE,
            location = "Balcon",
            sunExposure = SunExposure.DIRECT,
            soilType = SoilType.CACTUS_MIX,
            lastWateredMillis = now - (14 * dayMs) // Interval = 11 days -> overdue by 3 days
        )
        assertEquals(3, thirstyPlant.getDaysOverdueWatering(now))
        assertEquals(HealthStatus.ASSOIFFEE, thirstyPlant.getHealthStatus(now))

        val witheredPlant = Plant(
            name = "Pilea",
            species = PlantSpecies.PILEA,
            location = "Cuisine",
            sunExposure = SunExposure.HIGH,
            soilType = SoilType.LIGHT,
            lastWateredMillis = now - (11 * dayMs) // Interval = 4 days -> overdue by 7 days
        )
        assertEquals(7, witheredPlant.getDaysOverdueWatering(now))
        assertEquals(HealthStatus.FLETRIE, witheredPlant.getHealthStatus(now))
    }

    @Test
    fun testChatbotResponseGeneration() {
        val strelitzia = Plant(
            name = "Stella",
            species = PlantSpecies.STRELITZIA,
            location = "Salon",
            sunExposure = SunExposure.HIGH,
            soilType = SoilType.UNIVERSAL
        )

        val yellowLeafResponse = generateBotResponse("Pourquoi les feuilles jaunissent ?", strelitzia)
        assertTrue(yellowLeafResponse.contains("excès d'arrosage"))

        val wateringResponse = generateBotResponse("Fréquence d'arrosage ?", strelitzia)
        assertTrue(wateringResponse.contains("arrosage"))

        val sunResponse = generateBotResponse("Besoin en soleil ?", strelitzia)
        assertTrue(sunResponse.contains("Stella"))
    }
}
