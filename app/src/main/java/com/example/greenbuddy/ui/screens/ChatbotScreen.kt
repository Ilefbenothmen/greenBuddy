package com.example.greenbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.greenbuddy.model.ChatMessage
import com.example.greenbuddy.model.MessageSender
import com.example.greenbuddy.model.Plant
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ChatbotScreen(
    messages: List<ChatMessage>,
    selectedPlantContext: Plant?,
    onSendMessage: (String, String?) -> Unit,
    onClearPlantContext: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val suggestedQuestions = listOf(
        "Pourquoi les feuilles jaunissent ?",
        "Fréquence d'arrosage idéale ?",
        "Quel engrais utiliser ?",
        "Taches sur les feuilles ?",
        "Exposition au soleil ?"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF3F7F4)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFlorist,
                        contentDescription = null,
                        tint = Color(0xFF2D6A4F),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Assistant GreenBuddy",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF1B4332)
                    )
                }

                // Plant Context Banner if active
                if (selectedPlantContext != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFD8F3DC),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Conseils personnalisés pour ${selectedPlantContext.name} (${selectedPlantContext.species.displayName})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B4332)
                            )
                            IconButton(
                                onClick = onClearPlantContext,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fermer le contexte",
                                    tint = Color(0xFF1B4332)
                                )
                            }
                        }
                    }
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isUser = msg.sender == MessageSender.USER
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = if (isUser) 18.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 18.dp
                            ),
                            color = if (isUser) Color(0xFF2D6A4F) else Color.White,
                            shadowElevation = 1.dp,
                            modifier = Modifier.widthIn(max = 290.dp)
                        ) {
                            Text(
                                text = msg.text,
                                fontSize = 14.sp,
                                color = if (isUser) Color.White else Color(0xFF1B3B2B),
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // Suggested Question Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestedQuestions) { question ->
                    Surface(
                        modifier = Modifier.clickable {
                            onSendMessage(question, selectedPlantContext?.id)
                        },
                        shape = RoundedCornerShape(50),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB7E4C7))
                    ) {
                        Text(
                            text = question,
                            fontSize = 12.sp,
                            color = Color(0xFF2D6A4F),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Text Input Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Posez votre question...", fontSize = 14.sp, color = Color.Gray) },
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF4F7F4),
                            unfocusedContainerColor = Color(0xFFF4F7F4),
                            focusedBorderColor = Color(0xFF52B788),
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText.trim(), selectedPlantContext?.id)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color(0xFF2D6A4F), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Envoyer",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// Local smart response simulator
fun generateBotResponse(prompt: String, plant: Plant?): String {
    val text = prompt.lowercase()
    val plantInfo = if (plant != null) "pour votre ${plant.name} (${plant.species.displayName})" else "pour vos plantes"

    return when {
        text.contains("jaune") || text.contains("jaunissent") -> {
            "Les feuilles jaunes $plantInfo sont généralement le signe d'un excès d'arrosage ! Assurez-vous que le terreau sèche en surface entre deux arrosages et que le pot soit bien drainé."
        }
        text.contains("arrosage") || text.contains("eau") || text.contains("frequence") -> {
            if (plant != null) {
                "L'intervalle idéal d'arrosage recommandé pour votre ${plant.species.displayName} en exposition « ${plant.sunExposure.displayName} » est d'environ ${plant.getWateringIntervalDays()} jours."
            } else {
                "En général, arrosez lorsque le premier tiers du terreau est sec. Le Basilic a besoin d'eau tous les 2-3 jours, tandis que le Cactus peut attendre 2 semaines."
            }
        }
        text.contains("engrais") || text.contains("nourrir") || text.contains("fertil") -> {
            "Appliquez un engrais liquide adapté durant la période de croissance (printemps et été). En hiver, réduisez ou stoppez la fertilisation."
        }
        text.contains("tache") || text.contains("marron") || text.contains("noire") -> {
            "Des taches brunes sur les feuilles peuvent indiquer un manque d'humidité ambiante ou une maladie cryptogamique. Pensez à vaporiser les feuilles avec de l'eau non calcaire."
        }
        text.contains("soleil") || text.contains("lumiere") || text.contains("exposition") -> {
            if (plant != null) {
                "Votre ${plant.name} a un besoin en lumière « ${plant.sunExposure.displayName} ». Évitez le soleil direct brûlant derrière une vitre sans voilage."
            } else {
                "La plupart des plantes tropicales (Monstera, Pothos, Ficus) adorent une lumière vive mais indirecte."
            }
        }
        else -> {
            "Pour maintenir $plantInfo en parfaite santé, veillez à respecter son rythme d'arrosage, vérifiez le drainage du pot et dépoussiérez régulièrement ses feuilles !"
        }
    }
}
