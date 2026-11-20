package com.example.greenbuddy.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.example.greenbuddy.model.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class GreenBuddyRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("greenbuddy_prefs", Context.MODE_PRIVATE)

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val analytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(context) }

    private var plantsListenerRegistration: ListenerRegistration? = null

    companion object {
        private const val KEY_SETTINGS = "key_settings_json"
        private const val KEY_CHAT_PREFIX = "key_chat_json_"
        private const val KEY_PLANTS_PREFIX = "key_plants_json_"
    }

    fun loginUser(
        email: String,
        name: String,
        passwordInput: String = "defaultPass123",
        isSignUp: Boolean = false,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val emailTrimmed = email.trim()
        val nameTrimmed = name.trim().ifBlank { emailTrimmed.substringBefore("@") }

        if (isSignUp) {
            auth.createUserWithEmailAndPassword(emailTrimmed, passwordInput)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    val current = getSettings().copy(
                        currentUserEmail = user?.email ?: emailTrimmed,
                        currentUserName = nameTrimmed
                    )
                    saveSettings(current)
                    syncUserProfileToFirebase(current)
                    try {
                        analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, Bundle().apply {
                            putString(FirebaseAnalytics.Param.METHOD, "email_password")
                        })
                    } catch (_: Exception) {}
                    onResult(true, null)
                }
                .addOnFailureListener { e ->
                    // Fallback to local login if offline / demo
                    val current = getSettings().copy(
                        currentUserEmail = emailTrimmed,
                        currentUserName = nameTrimmed
                    )
                    saveSettings(current)
                    onResult(true, "Mode hors-ligne : ${e.localizedMessage}")
                }
        } else {
            auth.signInWithEmailAndPassword(emailTrimmed, passwordInput)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    val current = getSettings().copy(
                        currentUserEmail = user?.email ?: emailTrimmed,
                        currentUserName = nameTrimmed
                    )
                    saveSettings(current)
                    syncUserProfileToFirebase(current)
                    try {
                        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
                            putString(FirebaseAnalytics.Param.METHOD, "email_password")
                        })
                    } catch (_: Exception) {}
                    onResult(true, null)
                }
                .addOnFailureListener { e ->
                    // Try creating account if sign-in failed due to non-existent account or network offline
                    auth.createUserWithEmailAndPassword(emailTrimmed, passwordInput)
                        .addOnSuccessListener { authResult ->
                            val user = authResult.user
                            val current = getSettings().copy(
                                currentUserEmail = user?.email ?: emailTrimmed,
                                currentUserName = nameTrimmed
                            )
                            saveSettings(current)
                            syncUserProfileToFirebase(current)
                            try {
                                analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, Bundle().apply {
                                    putString(FirebaseAnalytics.Param.METHOD, "email_password")
                                })
                            } catch (_: Exception) {}
                            onResult(true, null)
                        }
                        .addOnFailureListener {
                            // Offline fallback
                            val current = getSettings().copy(
                                currentUserEmail = emailTrimmed,
                                currentUserName = nameTrimmed
                            )
                            saveSettings(current)
                            onResult(true, null)
                        }
                }
        }
    }

    fun loginWithGoogle(
        idToken: String?,
        email: String?,
        name: String?,
        onResult: (Boolean, String?) -> Unit
    ) {
        val userEmail = email?.ifBlank { null } ?: auth.currentUser?.email ?: "jardinier.google@gmail.com"
        val userName = name?.ifBlank { null } ?: userEmail.substringBefore("@")

        if (!idToken.isNullOrEmpty()) {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        val finalEmail = user?.email ?: userEmail
                        val finalName = user?.displayName ?: userName

                        val current = getSettings().copy(
                            currentUserEmail = finalEmail,
                            currentUserName = finalName
                        )
                        saveSettings(current)
                        syncUserProfileToFirebase(current)

                        try {
                            analytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
                                putString(FirebaseAnalytics.Param.METHOD, "google.com")
                            })
                        } catch (_: Exception) {}

                        onResult(true, null)
                    }
                    .addOnFailureListener {
                        val current = getSettings().copy(
                            currentUserEmail = userEmail,
                            currentUserName = userName
                        )
                        saveSettings(current)
                        syncUserProfileToFirebase(current)
                        onResult(true, null)
                    }
                return
            } catch (_: Exception) {}
        }

        val current = getSettings().copy(
            currentUserEmail = userEmail,
            currentUserName = userName
        )
        saveSettings(current)
        syncUserProfileToFirebase(current)
        onResult(true, null)
    }

    fun logoutUser() {
        plantsListenerRegistration?.remove()
        plantsListenerRegistration = null

        val current = getSettings()
        val updated = current.copy(
            currentUserEmail = null,
            currentUserName = null
        )
        saveSettings(updated)
        try {
            auth.signOut()
        } catch (e: Exception) {
            // Firebase Auth offline fallback
        }
    }

    private fun getUserId(): String {
        return auth.currentUser?.uid ?: getSettings().currentUserEmail?.replace(".", "_") ?: "guest"
    }

    private fun getPlantsKey(): String {
        return "$KEY_PLANTS_PREFIX${getUserId()}"
    }

    private fun getChatKey(): String {
        return "$KEY_CHAT_PREFIX${getUserId()}"
    }

    fun getSettings(): AppSettings {
        val jsonStr = prefs.getString(KEY_SETTINGS, null) ?: return AppSettings()
        return try {
            val json = JSONObject(jsonStr)
            AppSettings(
                currentUserEmail = if (json.has("currentUserEmail") && !json.isNull("currentUserEmail")) json.getString("currentUserEmail") else null,
                currentUserName = if (json.has("currentUserName") && !json.isNull("currentUserName")) json.getString("currentUserName") else null,
                reminderTime = json.optString("reminderTime", "08:00"),
                notificationsEnabled = json.optBoolean("notificationsEnabled", true),
                darkMode = if (json.has("darkMode")) {
                    if (json.isNull("darkMode")) null else json.getBoolean("darkMode")
                } else null,
                cloudBackup = json.optBoolean("cloudBackup", false),
                onboardingCompleted = json.optBoolean("onboardingCompleted", false)
            )
        } catch (e: Exception) {
            AppSettings()
        }
    }

    fun saveSettings(settings: AppSettings) {
        val json = JSONObject().apply {
            put("currentUserEmail", settings.currentUserEmail ?: JSONObject.NULL)
            put("currentUserName", settings.currentUserName ?: JSONObject.NULL)
            put("reminderTime", settings.reminderTime)
            put("notificationsEnabled", settings.notificationsEnabled)
            if (settings.darkMode != null) {
                put("darkMode", settings.darkMode)
            } else {
                put("darkMode", JSONObject.NULL)
            }
            put("cloudBackup", settings.cloudBackup)
            put("onboardingCompleted", settings.onboardingCompleted)
        }
        prefs.edit().putString(KEY_SETTINGS, json.toString()).apply()
        syncUserProfileToFirebase(settings)
    }

    fun syncUserProfileToFirebase(settings: AppSettings) {
        try {
            val userId = getUserId()
            if (userId == "guest") return
            val userDoc = mapOf(
                "email" to (settings.currentUserEmail ?: ""),
                "name" to (settings.currentUserName ?: ""),
                "reminderTime" to settings.reminderTime,
                "notificationsEnabled" to settings.notificationsEnabled,
                "cloudBackup" to settings.cloudBackup,
                "onboardingCompleted" to settings.onboardingCompleted,
                "lastUpdatedMillis" to System.currentTimeMillis()
            )
            firestore.collection("users").document(userId).set(userDoc)
        } catch (e: Exception) {
            // Firestore fallback
        }
    }

    fun listenToPlants(onPlantsUpdated: (List<Plant>) -> Unit) {
        plantsListenerRegistration?.remove()
        val userId = getUserId()
        if (userId == "guest") {
            onPlantsUpdated(getPlants())
            return
        }

        try {
            plantsListenerRegistration = firestore.collection("users")
                .document(userId)
                .collection("plants")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        onPlantsUpdated(getPlants())
                        return@addSnapshotListener
                    }
                    val remotePlants = snapshot.documents.mapNotNull { doc -> documentToPlant(doc) }
                    if (remotePlants.isNotEmpty()) {
                        savePlantsLocally(remotePlants)
                        onPlantsUpdated(remotePlants)
                    } else if (!snapshot.metadata.hasPendingWrites()) {
                        val local = getPlants()
                        if (local.isNotEmpty()) {
                            syncPlantsToFirebase(local)
                        }
                        onPlantsUpdated(local)
                    }
                }
        } catch (e: Exception) {
            onPlantsUpdated(getPlants())
        }
    }

    private fun savePlantsLocally(plants: List<Plant>) {
        val key = getPlantsKey()
        val array = JSONArray()
        plants.forEach { plant ->
            array.put(serializePlant(plant))
        }
        prefs.edit().putString(key, array.toString()).apply()
    }

    fun getPlants(): List<Plant> {
        val key = getPlantsKey()
        val jsonStr = prefs.getString(key, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<Plant>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(parsePlant(obj))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun savePlants(plants: List<Plant>) {
        savePlantsLocally(plants)
        syncPlantsToFirebase(plants)
    }

    private fun syncPlantsToFirebase(plants: List<Plant>) {
        try {
            val userId = getUserId()
            if (userId == "guest") return

            val batch = firestore.batch()
            plants.forEach { plant ->
                val docRef = firestore.collection("users")
                    .document(userId)
                    .collection("plants")
                    .document(plant.id)

                val map = mapOf(
                    "id" to plant.id,
                    "name" to plant.name,
                    "species" to plant.species.name,
                    "location" to plant.location,
                    "acquisitionDateMillis" to plant.acquisitionDateMillis,
                    "sunExposure" to plant.sunExposure.name,
                    "soilType" to plant.soilType.name,
                    "lastWateredMillis" to plant.lastWateredMillis,
                    "lastFertilizedMillis" to plant.lastFertilizedMillis,
                    "careCount" to plant.careCount,
                    "growthLevel" to plant.growthLevel,
                    "notes" to plant.notes,
                    "growthHistory" to plant.growthHistory.map { g ->
                        mapOf(
                            "id" to g.id,
                            "dateMillis" to g.dateMillis,
                            "heightCm" to g.heightCm,
                            "note" to g.note
                        )
                    },
                    "careLogs" to plant.careLogs.map { c ->
                        mapOf(
                            "id" to c.id,
                            "dateMillis" to c.dateMillis,
                            "type" to c.type.name,
                            "note" to c.note
                        )
                    }
                )
                batch.set(docRef, map)
            }
            batch.commit()
        } catch (e: Exception) {
            // Firestore offline or not configured yet
        }
    }

    fun getPlantById(id: String): Plant? {
        return getPlants().firstOrNull { it.id == id }
    }

    fun saveOrUpdatePlant(plant: Plant) {
        val plants = getPlants().toMutableList()
        val index = plants.indexOfFirst { it.id == plant.id }
        val isNew = index < 0
        if (index >= 0) {
            plants[index] = plant
        } else {
            plants.add(plant)
        }
        savePlants(plants)

        if (isNew) {
            try {
                analytics.logEvent("add_plant", Bundle().apply {
                    putString("plant_id", plant.id)
                    putString("plant_name", plant.name)
                    putString("species", plant.species.name)
                })
            } catch (_: Exception) {}
        }
    }

    fun deletePlant(plantId: String) {
        val plants = getPlants().filter { it.id != plantId }
        savePlants(plants)

        try {
            val userId = getUserId()
            if (userId != "guest") {
                firestore.collection("users")
                    .document(userId)
                    .collection("plants")
                    .document(plantId)
                    .delete()
            }
        } catch (e: Exception) {
            // Firestore fallback
        }
    }

    fun waterPlant(plantId: String): Plant? {
        val plant = getPlantById(plantId) ?: return null
        val now = System.currentTimeMillis()
        val newCareCount = plant.careCount + 1

        val newGrowthLevel = when {
            newCareCount >= 6 -> 3
            newCareCount >= 3 -> 2
            else -> 1
        }

        val noteText = if (newGrowthLevel > plant.growthLevel) {
            "Arrosage effectué · 🌟 Bravo ! Votre ${plant.name} a grandit (Niveau $newGrowthLevel) !"
        } else {
            "Arrosage effectué"
        }

        val updatedLogs = plant.careLogs.toMutableList().apply {
            add(0, CareLog(dateMillis = now, type = CareType.WATERING, note = noteText))
        }

        val updated = plant.copy(
            lastWateredMillis = now,
            careCount = newCareCount,
            growthLevel = newGrowthLevel,
            careLogs = updatedLogs
        )
        saveOrUpdatePlant(updated)

        try {
            analytics.logEvent("water_plant", Bundle().apply {
                putString("plant_id", plant.id)
                putString("plant_name", plant.name)
                putString("species", plant.species.name)
            })
        } catch (_: Exception) {}

        return updated
    }

    fun fertilizePlant(plantId: String): Plant? {
        val plant = getPlantById(plantId) ?: return null
        val now = System.currentTimeMillis()
        val newCareCount = plant.careCount + 1

        val newGrowthLevel = when {
            newCareCount >= 6 -> 3
            newCareCount >= 3 -> 2
            else -> 1
        }

        val noteText = if (newGrowthLevel > plant.growthLevel) {
            "Fertilisation effectuée · 🌟 Votre ${plant.name} a atteint le Niveau $newGrowthLevel !"
        } else {
            "Fertilisation effectuée"
        }

        val updatedLogs = plant.careLogs.toMutableList().apply {
            add(0, CareLog(dateMillis = now, type = CareType.FERTILIZATION, note = noteText))
        }

        val updated = plant.copy(
            lastFertilizedMillis = now,
            careCount = newCareCount,
            growthLevel = newGrowthLevel,
            careLogs = updatedLogs
        )
        saveOrUpdatePlant(updated)

        try {
            analytics.logEvent("fertilize_plant", Bundle().apply {
                putString("plant_id", plant.id)
                putString("plant_name", plant.name)
                putString("species", plant.species.name)
            })
        } catch (_: Exception) {}

        return updated
    }

    fun postponeWatering(plantId: String): Plant? {
        val plant = getPlantById(plantId) ?: return null
        val updated = plant.copy(
            lastWateredMillis = plant.lastWateredMillis + 86_400_000L
        )
        saveOrUpdatePlant(updated)
        return updated
    }

    fun addGrowthRecord(plantId: String, heightCm: Float, note: String): Plant? {
        val plant = getPlantById(plantId) ?: return null
        val newRecord = GrowthRecord(heightCm = heightCm, note = note)
        val updatedRecords = plant.growthHistory.toMutableList().apply {
            add(newRecord)
            sortBy { it.dateMillis }
        }
        val updated = plant.copy(growthHistory = updatedRecords)
        saveOrUpdatePlant(updated)
        return updated
    }

    fun deleteGrowthRecord(plantId: String, recordId: String): Plant? {
        val plant = getPlantById(plantId) ?: return null
        val updatedRecords = plant.growthHistory.filter { it.id != recordId }
        val updated = plant.copy(growthHistory = updatedRecords)
        saveOrUpdatePlant(updated)
        return updated
    }

    fun resetDemoPlants() {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        val demoPlants = listOf(
            Plant(
                id = "demo_strelitzia",
                name = "Stella",
                species = PlantSpecies.STRELITZIA,
                location = "Salon",
                sunExposure = SunExposure.HIGH,
                soilType = SoilType.UNIVERSAL,
                lastWateredMillis = now - (6 * dayMs),
                lastFertilizedMillis = now - (15 * dayMs),
                careCount = 4,
                growthLevel = 2,
                growthHistory = listOf(
                    GrowthRecord(dateMillis = now - (40 * dayMs), heightCm = 25f, note = "Achat"),
                    GrowthRecord(dateMillis = now - (20 * dayMs), heightCm = 29f, note = "Nouvelle feuille"),
                    GrowthRecord(dateMillis = now - (5 * dayMs), heightCm = 34f, note = "Pousse vigoureuse")
                ),
                careLogs = listOf(
                    CareLog(dateMillis = now - (6 * dayMs), type = CareType.WATERING, note = "Arrosage copieux"),
                    CareLog(dateMillis = now - (15 * dayMs), type = CareType.FERTILIZATION, note = "Engrais liquide vert")
                )
            ),
            Plant(
                id = "demo_aloe",
                name = "Aloe Vera",
                species = PlantSpecies.ALOE,
                location = "Balcon",
                sunExposure = SunExposure.DIRECT,
                soilType = SoilType.CACTUS_MIX,
                lastWateredMillis = now - (17 * dayMs),
                lastFertilizedMillis = now - (35 * dayMs),
                careCount = 1,
                growthLevel = 1,
                growthHistory = listOf(
                    GrowthRecord(dateMillis = now - (60 * dayMs), heightCm = 12f, note = "Rempotage")
                ),
                careLogs = listOf(
                    CareLog(dateMillis = now - (17 * dayMs), type = CareType.WATERING)
                )
            ),
            Plant(
                id = "demo_pilea",
                name = "Pilea",
                species = PlantSpecies.PILEA,
                location = "Cuisine",
                sunExposure = SunExposure.HIGH,
                soilType = SoilType.LIGHT,
                lastWateredMillis = now - (8 * dayMs),
                lastFertilizedMillis = now - (10 * dayMs),
                careCount = 2,
                growthLevel = 1,
                growthHistory = listOf(
                    GrowthRecord(dateMillis = now - (30 * dayMs), heightCm = 15f),
                    GrowthRecord(dateMillis = now - (10 * dayMs), heightCm = 18f, note = "Taille des apex")
                ),
                careLogs = listOf(
                    CareLog(dateMillis = now - (8 * dayMs), type = CareType.WATERING)
                )
            ),
            Plant(
                id = "demo_fifi",
                name = "Fifi",
                species = PlantSpecies.FICUS,
                location = "Chambre",
                sunExposure = SunExposure.MEDIUM,
                soilType = SoilType.UNIVERSAL,
                lastWateredMillis = now - (4 * dayMs),
                lastFertilizedMillis = now - (20 * dayMs),
                careCount = 7,
                growthLevel = 3,
                growthHistory = listOf(
                    GrowthRecord(dateMillis = now - (50 * dayMs), heightCm = 45f)
                ),
                careLogs = listOf(
                    CareLog(dateMillis = now - (4 * dayMs), type = CareType.WATERING)
                )
            )
        )
        savePlants(demoPlants)
    }

    fun clearAllData() {
        val key = getPlantsKey()
        prefs.edit().remove(key).remove(getChatKey()).apply()
    }

    // Chatbot Storage
    fun getChatMessages(): List<ChatMessage> {
        val key = getChatKey()
        val jsonStr = prefs.getString(key, null) ?: return defaultWelcomeChat()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<ChatMessage>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ChatMessage(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        sender = MessageSender.valueOf(obj.getString("sender")),
                        text = obj.getString("text"),
                        timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis()),
                        plantContextId = if (obj.isNull("plantContextId")) null else obj.optString("plantContextId")
                    )
                )
            }
            if (list.isEmpty()) defaultWelcomeChat() else list
        } catch (e: Exception) {
            defaultWelcomeChat()
        }
    }

    fun saveChatMessages(messages: List<ChatMessage>) {
        val key = getChatKey()
        val array = JSONArray()
        messages.forEach { msg ->
            val obj = JSONObject().apply {
                put("id", msg.id)
                put("sender", msg.sender.name)
                put("text", msg.text)
                put("timestampMillis", msg.timestampMillis)
                put("plantContextId", msg.plantContextId ?: JSONObject.NULL)
            }
            array.put(obj)
        }
        prefs.edit().putString(key, array.toString()).apply()
    }

    private fun defaultWelcomeChat(): List<ChatMessage> {
        val userName = getSettings().currentUserName ?: "Jardinier"
        return listOf(
            ChatMessage(
                sender = MessageSender.BOT,
                text = "Bonjour $userName ! Je suis votre assistant GreenBuddy 🌿. Posez-moi vos questions sur vos plantes, l'arrosage, l'exposition ou la taille !"
            )
        )
    }

    private fun parsePlant(obj: JSONObject): Plant {
        val growthArr = obj.optJSONArray("growthHistory") ?: JSONArray()
        val growthList = mutableListOf<GrowthRecord>()
        for (i in 0 until growthArr.length()) {
            val g = growthArr.getJSONObject(i)
            growthList.add(
                GrowthRecord(
                    id = g.optString("id", UUID.randomUUID().toString()),
                    dateMillis = g.optLong("dateMillis", System.currentTimeMillis()),
                    heightCm = g.getDouble("heightCm").toFloat(),
                    note = g.optString("note", "")
                )
            )
        }

        val careArr = obj.optJSONArray("careLogs") ?: JSONArray()
        val careList = mutableListOf<CareLog>()
        for (i in 0 until careArr.length()) {
            val c = careArr.getJSONObject(i)
            careList.add(
                CareLog(
                    id = c.optString("id", UUID.randomUUID().toString()),
                    dateMillis = c.optLong("dateMillis", System.currentTimeMillis()),
                    type = CareType.valueOf(c.optString("type", CareType.WATERING.name)),
                    note = c.optString("note", "")
                )
            )
        }

        return Plant(
            id = obj.getString("id"),
            name = obj.getString("name"),
            species = PlantSpecies.fromName(obj.getString("species")),
            location = obj.optString("location", "Intérieur"),
            acquisitionDateMillis = obj.optLong("acquisitionDateMillis", System.currentTimeMillis()),
            sunExposure = SunExposure.fromName(obj.optString("sunExposure", SunExposure.MEDIUM.name)),
            soilType = SoilType.fromName(obj.optString("soilType", SoilType.UNIVERSAL.name)),
            lastWateredMillis = obj.optLong("lastWateredMillis", System.currentTimeMillis()),
            lastFertilizedMillis = obj.optLong("lastFertilizedMillis", System.currentTimeMillis()),
            careCount = obj.optInt("careCount", 0),
            growthLevel = obj.optInt("growthLevel", 1),
            growthHistory = growthList,
            careLogs = careList,
            notes = obj.optString("notes", "")
        )
    }

    private fun serializePlant(plant: Plant): JSONObject {
        val obj = JSONObject()
        obj.put("id", plant.id)
        obj.put("name", plant.name)
        obj.put("species", plant.species.name)
        obj.put("location", plant.location)
        obj.put("acquisitionDateMillis", plant.acquisitionDateMillis)
        obj.put("sunExposure", plant.sunExposure.name)
        obj.put("soilType", plant.soilType.name)
        obj.put("lastWateredMillis", plant.lastWateredMillis)
        obj.put("lastFertilizedMillis", plant.lastFertilizedMillis)
        obj.put("careCount", plant.careCount)
        obj.put("growthLevel", plant.growthLevel)
        obj.put("notes", plant.notes)

        val growthArr = JSONArray()
        plant.growthHistory.forEach { g ->
            val gObj = JSONObject().apply {
                put("id", g.id)
                put("dateMillis", g.dateMillis)
                put("heightCm", g.heightCm)
                put("note", g.note)
            }
            growthArr.put(gObj)
        }
        obj.put("growthHistory", growthArr)

        val careArr = JSONArray()
        plant.careLogs.forEach { c ->
            val cObj = JSONObject().apply {
                put("id", c.id)
                put("dateMillis", c.dateMillis)
                put("type", c.type.name)
                put("note", c.note)
            }
            careArr.put(cObj)
        }
        obj.put("careLogs", careArr)

        return obj
    }

    @Suppress("UNCHECKED_CAST")
    private fun documentToPlant(doc: DocumentSnapshot): Plant? {
        return try {
            val id = doc.getString("id") ?: doc.id
            val name = doc.getString("name") ?: return null
            val speciesStr = doc.getString("species") ?: PlantSpecies.ALOE.name
            val location = doc.getString("location") ?: "Intérieur"
            val acquisitionDateMillis = doc.getLong("acquisitionDateMillis") ?: System.currentTimeMillis()
            val sunExposureStr = doc.getString("sunExposure") ?: SunExposure.MEDIUM.name
            val soilTypeStr = doc.getString("soilType") ?: SoilType.UNIVERSAL.name
            val lastWateredMillis = doc.getLong("lastWateredMillis") ?: System.currentTimeMillis()
            val lastFertilizedMillis = doc.getLong("lastFertilizedMillis") ?: System.currentTimeMillis()
            val careCount = doc.getLong("careCount")?.toInt() ?: 0
            val growthLevel = doc.getLong("growthLevel")?.toInt() ?: 1
            val notes = doc.getString("notes") ?: ""

            val growthHistoryRaw = doc.get("growthHistory") as? List<Map<String, Any>> ?: emptyList()
            val growthHistory = growthHistoryRaw.mapNotNull { g ->
                val gId = g["id"] as? String ?: UUID.randomUUID().toString()
                val gDate = (g["dateMillis"] as? Number)?.toLong() ?: System.currentTimeMillis()
                val gHeight = (g["heightCm"] as? Number)?.toFloat() ?: 0f
                val gNote = g["note"] as? String ?: ""
                GrowthRecord(id = gId, dateMillis = gDate, heightCm = gHeight, note = gNote)
            }

            val careLogsRaw = doc.get("careLogs") as? List<Map<String, Any>> ?: emptyList()
            val careLogs = careLogsRaw.mapNotNull { c ->
                val cId = c["id"] as? String ?: UUID.randomUUID().toString()
                val cDate = (c["dateMillis"] as? Number)?.toLong() ?: System.currentTimeMillis()
                val cTypeStr = c["type"] as? String ?: CareType.WATERING.name
                val cType = try { CareType.valueOf(cTypeStr) } catch (e: Exception) { CareType.WATERING }
                val cNote = c["note"] as? String ?: ""
                CareLog(id = cId, dateMillis = cDate, type = cType, note = cNote)
            }

            Plant(
                id = id,
                name = name,
                species = PlantSpecies.fromName(speciesStr),
                location = location,
                acquisitionDateMillis = acquisitionDateMillis,
                sunExposure = SunExposure.fromName(sunExposureStr),
                soilType = SoilType.fromName(soilTypeStr),
                lastWateredMillis = lastWateredMillis,
                lastFertilizedMillis = lastFertilizedMillis,
                careCount = careCount,
                growthLevel = growthLevel,
                growthHistory = growthHistory,
                careLogs = careLogs,
                notes = notes
            )
        } catch (e: Exception) {
            null
        }
    }
}
