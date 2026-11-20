package com.example.greenbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.WaterDrop
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
import com.example.greenbuddy.model.CareType
import com.example.greenbuddy.model.HealthStatus
import com.example.greenbuddy.model.Plant
import com.example.greenbuddy.ui.components.HealthBadge
import com.example.greenbuddy.ui.components.LineChartCanvas
import com.example.greenbuddy.ui.components.Plant3DCanvas
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DetailTab(val title: String) {
    SUIVI("Suivi"),
    VUE_3D("Vue 3D"),
    JOURNAL("Journal"),
    CONSEILS("Conseils")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plant: Plant,
    onBack: () -> Unit,
    onWater: () -> Unit,
    onFertilize: () -> Unit,
    onEdit: () -> Unit,
    onOpenChatbot: () -> Unit,
    onAddGrowthRecord: (Float, String) -> Unit,
    onDeleteGrowthRecord: (String) -> Unit,
    onDeletePlant: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(DetailTab.SUIVI) }
    var selected3DStateOverride by remember { mutableStateOf<HealthStatus?>(null) }
    var showAddGrowthDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val currentHealth = plant.getHealthStatus()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF3F7F4)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = plant.name,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenChatbot) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Assistant IA")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF3F7F4))
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Plant Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFFEFF5F0))
                            ) {
                                Plant3DCanvas(
                                    species = plant.species,
                                    healthStatus = selected3DStateOverride ?: currentHealth,
                                    growthLevel = plant.growthLevel,
                                    interactive = false,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    HealthBadge(status = currentHealth)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFFD8F3DC),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "Niv. ${plant.growthLevel}",
                                            color = Color(0xFF1B4332),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = plant.species.displayName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = Color(0xFF1B3B2B)
                                )
                                Text(
                                    text = "📍 ${plant.location}",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "☀️ ${plant.sunExposure.displayName}",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Tab Selector Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(Color.White)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailTab.entries.forEach { tab ->
                            val isSelected = tab == selectedTab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) Color(0xFF2D6A4F) else Color.Transparent)
                                    .clickable { selectedTab = tab }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = tab.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color.Gray
                                )
                            }
                        }
                    }
                }

                // Tab Content
                when (selectedTab) {
                    DetailTab.SUIVI -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Échéances & Soins",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B4332)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    val daysOverdueWater = plant.getDaysOverdueWatering()
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Arrosage", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(
                                                text = if (daysOverdueWater > 0) "En retard de $daysOverdueWater jours" else "Prochain arrosage: ${dateFormat.format(Date(plant.getNextWateringDateMillis()))}",
                                                fontSize = 12.sp,
                                                color = if (daysOverdueWater > 0) Color(0xFFC62828) else Color.Gray
                                            )
                                        }

                                        Button(
                                            onClick = onWater,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Arrosée", fontSize = 12.sp)
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEFF3F0))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Fertilisation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(
                                                text = "Prochaine: ${dateFormat.format(Date(plant.getNextFertilizingDateMillis()))}",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        Button(
                                            onClick = onFertilize,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Icon(Icons.Default.LocalFlorist, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Fertilisée", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Care History
                        item {
                            Text(
                                text = "Historique des soins",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B4332)
                            )
                        }

                        if (plant.careLogs.isEmpty()) {
                            item {
                                Text("Aucun historique pour le moment.", fontSize = 13.sp, color = Color.Gray)
                            }
                        } else {
                            items(plant.careLogs) { log ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (log.type == CareType.WATERING) Icons.Default.WaterDrop else Icons.Default.LocalFlorist,
                                                contentDescription = null,
                                                tint = if (log.type == CareType.WATERING) Color(0xFF1976D2) else Color(0xFF2E7D32),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(log.type.displayName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        }
                                        Text(dateFormat.format(Date(log.dateMillis)), fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    DetailTab.VUE_3D -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp)
                                    ) {
                                        Plant3DCanvas(
                                            species = plant.species,
                                            healthStatus = selected3DStateOverride ?: currentHealth,
                                            growthLevel = plant.growthLevel,
                                            interactive = true,
                                            showZoomControls = true,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "Glissez pour faire tourner · boutons ou molette pour zoomer",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Aperçu des états visuels",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B4332)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        HealthStatus.entries.forEach { status ->
                                            val isSelected = (selected3DStateOverride ?: currentHealth) == status
                                            OutlinedButton(
                                                onClick = { selected3DStateOverride = status },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (isSelected) status.backgroundColor else Color.Transparent,
                                                    contentColor = status.textColor
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(status.label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    if (currentHealth == status) {
                                                        Text("(actuel)", fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    DetailTab.JOURNAL -> {
                        item {
                            LineChartCanvas(growthRecords = plant.growthHistory)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mesures enregistrées",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B4332)
                                )

                                Button(
                                    onClick = { showAddGrowthDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F)),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text("+ Mesure", fontSize = 12.sp)
                                }
                            }
                        }

                        if (plant.growthHistory.isEmpty()) {
                            item {
                                Text("Aucune mesure ajoutée", fontSize = 13.sp, color = Color.Gray)
                            }
                        } else {
                            items(plant.growthHistory) { record ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("${record.heightCm} cm", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            if (record.note.isNotBlank()) {
                                                Text(record.note, fontSize = 12.sp, color = Color.Gray)
                                            }
                                            Text(dateFormat.format(Date(record.dateMillis)), fontSize = 11.sp, color = Color.Gray)
                                        }

                                        IconButton(onClick = { onDeleteGrowthRecord(record.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    DetailTab.CONSEILS -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Conseils d'entretien personnalisés",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B4332)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    TipItem(
                                        title = "Arrosage & Hydratation",
                                        desc = "Pour une exposition « ${plant.sunExposure.displayName} » et un sol « ${plant.soilType.displayName} », un arrosage tous les ${plant.getWateringIntervalDays()} jours est parfait."
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    TipItem(
                                        title = "Lumière & Emplacement",
                                        desc = "Emplacement idéal : ${plant.location}. Évitez les courants d'air froid et le soleil direct brûlant derrière une vitre."
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    TipItem(
                                        title = "Fertilisation de saison",
                                        desc = "Nourrissez avec un engrais équilibré tous les ${plant.getFertilizerIntervalDays()} jours durant la saison de croissance."
                                    )
                                }
                            }
                        }
                    }
                }

                // Delete Plant Button
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "🗑 Supprimer cette plante",
                            color = Color(0xFFC62828),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Add Growth Dialog
    if (showAddGrowthDialog) {
        var heightText by remember { mutableStateOf("") }
        var noteText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddGrowthDialog = false },
            title = { Text("Nouvelle mesure de hauteur") },
            text = {
                Column {
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Hauteur (cm)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Note / Remarque") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val h = heightText.toFloatOrNull() ?: 0f
                        if (h > 0f) {
                            onAddGrowthRecord(h, noteText.trim())
                            showAddGrowthDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F))
                ) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGrowthDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Supprimer ${plant.name} ?") },
            text = { Text("Cette action est irréversible. Voulez-vous vraiment supprimer cette plante ?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePlant()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun TipItem(title: String, desc: String) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2D6A4F))
        Text(desc, fontSize = 13.sp, color = Color.DarkGray, lineHeight = 18.sp)
    }
}
