package com.example.greenbuddy.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.example.greenbuddy.model.HealthStatus
import com.example.greenbuddy.model.Plant
import com.example.greenbuddy.model.PlantSpecies
import com.example.greenbuddy.model.SoilType
import com.example.greenbuddy.model.SunExposure
import com.example.greenbuddy.ui.components.Plant3DCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlantScreen(
    existingPlant: Plant? = null,
    onBack: () -> Unit,
    onSave: (name: String, species: PlantSpecies, location: String, sun: SunExposure, soil: SoilType) -> Unit
) {
    var name by remember { mutableStateOf(existingPlant?.name ?: "") }
    var species by remember { mutableStateOf(existingPlant?.species ?: PlantSpecies.ALOE) }
    var location by remember { mutableStateOf(existingPlant?.location ?: "Salon") }
    var sunExposure by remember { mutableStateOf(existingPlant?.sunExposure ?: SunExposure.HIGH) }
    var soilType by remember { mutableStateOf(existingPlant?.soilType ?: SoilType.UNIVERSAL) }

    var sunExpanded by remember { mutableStateOf(false) }
    var soilExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF3F7F4)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingPlant != null) "Modifier la plante" else "Ajouter une plante",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF3F7F4))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live 3D Plant Main Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Plant3DCanvas(
                            species = species,
                            healthStatus = HealthStatus.EN_FORME,
                            growthLevel = 2,
                            interactive = true,
                            showZoomControls = false,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Selected species title overlay badge
                        Surface(
                            color = Color(0xFF1B4332).copy(alpha = 0.85f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Modèle 3D : ${species.displayName}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // 3D Model Picker Carousel List
                Column {
                    Text(
                        text = "Choisissez le modèle 3D de la plante",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B4332)
                    )
                    Text(
                        text = "Glissez et touchez un modèle pour le sélectionner",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(PlantSpecies.entries) { itemSpecies ->
                            val isSelected = itemSpecies == species

                            Card(
                                modifier = Modifier
                                    .width(115.dp)
                                    .clickable { species = itemSpecies },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFD8F3DC) else Color.White
                                ),
                                border = if (isSelected) BorderStroke(2.dp, Color(0xFF2D6A4F)) else BorderStroke(1.dp, Color(0xFFE0E8E2))
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(90.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFFEFF5F0))
                                    ) {
                                        Plant3DCanvas(
                                            species = itemSpecies,
                                            healthStatus = HealthStatus.EN_FORME,
                                            growthLevel = 2,
                                            interactive = false,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(20.dp)
                                                    .background(Color(0xFF2D6A4F), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Sélectionné",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = itemSpecies.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF1B4332) else Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }

                // Name TextField
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de la plante (ex: Monty)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // Location TextField
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Emplacement (ex: Salon, Balcon, Cuisine)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // Sun Exposure Dropdown
                ExposedDropdownMenuBox(
                    expanded = sunExpanded,
                    onExpandedChange = { sunExpanded = !sunExpanded }
                ) {
                    OutlinedTextField(
                        value = sunExposure.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Exposition au soleil") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sunExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = sunExpanded,
                        onDismissRequest = { sunExpanded = false }
                    ) {
                        SunExposure.entries.forEach { exp ->
                            DropdownMenuItem(
                                text = { Text(exp.displayName) },
                                onClick = {
                                    sunExposure = exp
                                    sunExpanded = false
                                }
                            )
                        }
                    }
                }

                // Soil Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = soilExpanded,
                    onExpandedChange = { soilExpanded = !soilExpanded }
                ) {
                    OutlinedTextField(
                        value = soilType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type de sol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = soilExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = soilExpanded,
                        onDismissRequest = { soilExpanded = false }
                    ) {
                        SoilType.entries.forEach { soil ->
                            DropdownMenuItem(
                                text = { Text(soil.displayName) },
                                onClick = {
                                    soilType = soil
                                    soilExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Save Button
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(name.trim(), species, location.trim(), sunExposure, soilType)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F)),
                    shape = RoundedCornerShape(28.dp),
                    enabled = name.isNotBlank()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enregistrer la plante",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
