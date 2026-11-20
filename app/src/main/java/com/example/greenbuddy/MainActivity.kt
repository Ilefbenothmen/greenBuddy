package com.example.greenbuddy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.greenbuddy.data.GreenBuddyRepository
import com.example.greenbuddy.model.AppSettings
import com.example.greenbuddy.model.CareType
import com.example.greenbuddy.model.ChatMessage
import com.example.greenbuddy.model.MessageSender
import com.example.greenbuddy.model.Plant
import com.example.greenbuddy.ui.components.BottomNavBar
import com.example.greenbuddy.ui.components.NavigationTab
import com.example.greenbuddy.ui.screens.*
import com.example.greenbuddy.ui.theme.GreenBuddyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val repository = remember { GreenBuddyRepository(context) }

            var settings by remember { mutableStateOf(repository.getSettings()) }
            var plants by remember { mutableStateOf(repository.getPlants()) }
            var chatMessages by remember { mutableStateOf(repository.getChatMessages()) }

            LaunchedEffect(settings.currentUserEmail) {
                repository.listenToPlants { updatedPlants ->
                    plants = updatedPlants
                }
            }

            val useDarkTheme = when (settings.darkMode) {
                true -> true
                false -> false
                null -> isSystemInDarkTheme()
            }

            GreenBuddyTheme(darkTheme = useDarkTheme) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (useDarkTheme) Color(0xFF0F1713) else Color(0xFFE2ECE5)),
                    contentAlignment = Alignment.Center
                ) {
                    // Frame container for phone mockup (390x844 aspect ratio centered on large screens)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 440.dp)
                            .heightIn(max = 920.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        GreenBuddyAppContent(
                            settings = settings,
                            plants = plants,
                            chatMessages = chatMessages,
                            onLoginSuccess = { email, name, password, isSignUp, onResult ->
                                repository.loginUser(email, name, password, isSignUp) { success, err ->
                                    if (success) {
                                        settings = repository.getSettings()
                                        plants = repository.getPlants()
                                        chatMessages = repository.getChatMessages()
                                        onResult(true, null)
                                    } else {
                                        onResult(false, err)
                                    }
                                }
                            },
                            onGoogleSignInSuccess = { idToken, email, name, onResult ->
                                repository.loginWithGoogle(idToken, email, name) { success, err ->
                                    if (success) {
                                        settings = repository.getSettings()
                                        plants = repository.getPlants()
                                        chatMessages = repository.getChatMessages()
                                        onResult(true, null)
                                    } else {
                                        onResult(false, err)
                                    }
                                }
                            },
                            onLogout = {
                                repository.logoutUser()
                                settings = repository.getSettings()
                                plants = repository.getPlants()
                                chatMessages = repository.getChatMessages()
                            },
                            onUpdateSettings = { newSettings ->
                                settings = newSettings
                                repository.saveSettings(newSettings)
                            },
                            onWaterPlant = { plant ->
                                val updated = repository.waterPlant(plant.id)
                                plants = repository.getPlants()
                                if (updated != null) {
                                    Toast.makeText(context, "💧 ${plant.name} a été arrosée !", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onFertilizePlant = { plant ->
                                val updated = repository.fertilizePlant(plant.id)
                                plants = repository.getPlants()
                                if (updated != null) {
                                    Toast.makeText(context, "🌱 ${plant.name} a été fertilisée !", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onPostponeWatering = { plant ->
                                repository.postponeWatering(plant.id)
                                plants = repository.getPlants()
                                Toast.makeText(context, "🕒 Arrosage de ${plant.name} reporté à demain", Toast.LENGTH_SHORT).show()
                            },
                            onSavePlant = { plantToSave ->
                                repository.saveOrUpdatePlant(plantToSave)
                                plants = repository.getPlants()
                            },
                            onDeletePlant = { plantId ->
                                repository.deletePlant(plantId)
                                plants = repository.getPlants()
                            },
                            onAddGrowthRecord = { plantId, height, note ->
                                repository.addGrowthRecord(plantId, height, note)
                                plants = repository.getPlants()
                            },
                            onDeleteGrowthRecord = { plantId, recordId ->
                                repository.deleteGrowthRecord(plantId, recordId)
                                plants = repository.getPlants()
                            },
                            onSendMessage = { prompt, plantContextId ->
                                val userMsg = ChatMessage(sender = MessageSender.USER, text = prompt, plantContextId = plantContextId)
                                val updatedMessages = chatMessages.toMutableList().apply { add(userMsg) }

                                val contextPlant = plantContextId?.let { id -> plants.firstOrNull { it.id == id } }
                                val botText = generateBotResponse(prompt, contextPlant)
                                val botMsg = ChatMessage(sender = MessageSender.BOT, text = botText, plantContextId = plantContextId)
                                updatedMessages.add(botMsg)

                                chatMessages = updatedMessages
                                repository.saveChatMessages(updatedMessages)
                            },
                            onResetDemoData = {
                                repository.resetDemoPlants()
                                plants = repository.getPlants()
                                Toast.makeText(context, "Plantes de démonstration restaurées !", Toast.LENGTH_SHORT).show()
                            },
                            onDeleteAllData = {
                                repository.clearAllData()
                                plants = repository.getPlants()
                                chatMessages = repository.getChatMessages()
                                settings = repository.getSettings()
                                Toast.makeText(context, "Toutes les données ont été effacées", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GreenBuddyAppContent(
    settings: AppSettings,
    plants: List<Plant>,
    chatMessages: List<ChatMessage>,
    onLoginSuccess: (email: String, name: String, password: String, isSignUp: Boolean, onResult: (Boolean, String?) -> Unit) -> Unit,
    onGoogleSignInSuccess: (idToken: String?, email: String?, name: String?, onResult: (Boolean, String?) -> Unit) -> Unit,
    onLogout: () -> Unit,
    onUpdateSettings: (AppSettings) -> Unit,
    onWaterPlant: (Plant) -> Unit,
    onFertilizePlant: (Plant) -> Unit,
    onPostponeWatering: (Plant) -> Unit,
    onSavePlant: (Plant) -> Unit,
    onDeletePlant: (String) -> Unit,
    onAddGrowthRecord: (String, Float, String) -> Unit,
    onDeleteGrowthRecord: (String, String) -> Unit,
    onSendMessage: (String, String?) -> Unit,
    onResetDemoData: () -> Unit,
    onDeleteAllData: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.JARDIN) }
    var selectedPlantForDetail by remember { mutableStateOf<Plant?>(null) }
    var isAddingPlant by remember { mutableStateOf(false) }
    var plantToEdit by remember { mutableStateOf<Plant?>(null) }
    var chatbotPlantContext by remember { mutableStateOf<Plant?>(null) }

    // 1. Auth Flow: User must log in first
    if (!settings.isLoggedIn) {
        AuthScreen(
            onLoginSuccess = onLoginSuccess,
            onGoogleSignInSuccess = onGoogleSignInSuccess
        )
        return
    }

    // 2. Onboarding Flow
    if (!settings.onboardingCompleted) {
        OnboardingScreen(
            onEnableNotificationsAndStart = {
                onUpdateSettings(settings.copy(onboardingCompleted = true, notificationsEnabled = true))
            },
            onSkipLater = {
                onUpdateSettings(settings.copy(onboardingCompleted = true))
            }
        )
        return
    }

    // Keep active detailed plant updated if plants list changes
    val activeDetailPlant = remember(plants, selectedPlantForDetail) {
        selectedPlantForDetail?.let { current -> plants.firstOrNull { it.id == current.id } }
    }

    // Overdue task counter for badge
    val overdueCount = remember(plants) {
        plants.count { it.getDaysOverdueWatering() > 0 || it.getDaysOverdueFertilizing() > 0 }
    }

    Scaffold(
        bottomBar = {
            if (activeDetailPlant == null && !isAddingPlant && plantToEdit == null) {
                BottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    overdueTaskCount = overdueCount
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // Sub-Screen: Plant Detail
                activeDetailPlant != null -> {
                    PlantDetailScreen(
                        plant = activeDetailPlant,
                        onBack = { selectedPlantForDetail = null },
                        onWater = { onWaterPlant(activeDetailPlant) },
                        onFertilize = { onFertilizePlant(activeDetailPlant) },
                        onEdit = {
                            plantToEdit = activeDetailPlant
                            selectedPlantForDetail = null
                        },
                        onOpenChatbot = {
                            chatbotPlantContext = activeDetailPlant
                            selectedPlantForDetail = null
                            selectedTab = NavigationTab.CHATBOT
                        },
                        onAddGrowthRecord = { height, note ->
                            onAddGrowthRecord(activeDetailPlant.id, height, note)
                        },
                        onDeleteGrowthRecord = { recordId ->
                            onDeleteGrowthRecord(activeDetailPlant.id, recordId)
                        },
                        onDeletePlant = {
                            onDeletePlant(activeDetailPlant.id)
                            selectedPlantForDetail = null
                        }
                    )
                }

                // Sub-Screen: Add or Edit Plant Form
                isAddingPlant || plantToEdit != null -> {
                    AddEditPlantScreen(
                        existingPlant = plantToEdit,
                        onBack = {
                            isAddingPlant = false
                            plantToEdit = null
                        },
                        onSave = { name, species, location, sun, soil ->
                            val newPlant = plantToEdit?.copy(
                                name = name,
                                species = species,
                                location = location,
                                sunExposure = sun,
                                soilType = soil
                            ) ?: Plant(
                                name = name,
                                species = species,
                                location = location,
                                sunExposure = sun,
                                soilType = soil
                            )
                            onSavePlant(newPlant)
                            isAddingPlant = false
                            plantToEdit = null
                        }
                    )
                }

                // Primary Bottom Bar Tabs
                else -> {
                    when (selectedTab) {
                        NavigationTab.JARDIN -> {
                            GardenScreen(
                                plants = plants,
                                onPlantClick = { selectedPlantForDetail = it },
                                onWaterClick = { onWaterPlant(it) },
                                onAddPlantClick = { isAddingPlant = true }
                            )
                        }

                        NavigationTab.RAPPELS -> {
                            RemindersScreen(
                                plants = plants,
                                reminderTime = settings.reminderTime,
                                onMarkDone = { plant, careType ->
                                    if (careType == CareType.WATERING) {
                                        onWaterPlant(plant)
                                    } else {
                                        onFertilizePlant(plant)
                                    }
                                },
                                onPostpone = { onPostponeWatering(it) }
                            )
                        }

                        NavigationTab.CHATBOT -> {
                            ChatbotScreen(
                                messages = chatMessages,
                                selectedPlantContext = chatbotPlantContext,
                                onSendMessage = { prompt, contextId ->
                                    onSendMessage(prompt, contextId)
                                },
                                onClearPlantContext = { chatbotPlantContext = null }
                            )
                        }

                        NavigationTab.REGLAGES -> {
                            SettingsScreen(
                                settings = settings,
                                onUpdateSettings = onUpdateSettings,
                                onResetDemoData = onResetDemoData,
                                onDeleteAllData = onDeleteAllData,
                                onLogout = onLogout
                            )
                        }
                    }
                }
            }
        }
    }
}
