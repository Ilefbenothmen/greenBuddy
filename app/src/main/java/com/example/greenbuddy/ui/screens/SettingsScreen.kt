package com.example.greenbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.greenbuddy.model.AppSettings

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
    onResetDemoData: () -> Unit,
    onDeleteAllData: () -> Unit,
    onLogout: () -> Unit
) {
    var showTimeDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val availableTimes = listOf("07:00", "08:00", "09:00", "12:00", "18:00", "20:00")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF3F7F4)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Réglages",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF1B4332)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = settings.currentUserName ?: "Jardinier",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1B3B2B)
                        )
                        Text(
                            text = settings.currentUserEmail ?: "Compte local",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    OutlinedButton(
                        onClick = onLogout,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                    ) {
                        Text("Déconnexion", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Reminder Time
                    SettingRow(
                        icon = Icons.Default.Schedule,
                        title = "Heure des rappels",
                        subtitle = settings.reminderTime,
                        onClick = { showTimeDialog = true }
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEFF3F0))

                    // Notifications Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFF2D6A4F),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1B3B2B))
                                Text("Rappels quotidiens d'arrosage", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(notificationsEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2D6A4F)
                            )
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEFF3F0))

                    // Dark Mode Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = Color(0xFF2D6A4F),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Mode sombre", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1B3B2B))
                                Text("Apparence sombre de l'application", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Switch(
                            checked = settings.darkMode == true,
                            onCheckedChange = { onUpdateSettings(settings.copy(darkMode = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2D6A4F)
                            )
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEFF3F0))

                    // Cloud Backup Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = Color(0xFF2D6A4F),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Sauvegarde cloud", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1B3B2B))
                                Text("Synchroniser vos données", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Switch(
                            checked = settings.cloudBackup,
                            onCheckedChange = { onUpdateSettings(settings.copy(cloudBackup = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2D6A4F)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingRow(
                        icon = Icons.Default.Refresh,
                        title = "Restauration de plantes de démo",
                        subtitle = "Réinitialiser les plantes exemples",
                        iconTint = Color(0xFF2D6A4F),
                        onClick = { showResetConfirmDialog = true }
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEFF3F0))

                    SettingRow(
                        icon = Icons.Default.DeleteForever,
                        title = "Supprimer toutes les données",
                        subtitle = "Effacer définitivement tout le jardin",
                        iconTint = Color(0xFFC62828),
                        titleColor = Color(0xFFC62828),
                        onClick = { showDeleteConfirmDialog = true }
                    )
                }
            }
        }
    }

    // Time Selection Dialog
    if (showTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = { Text("Heure des rappels") },
            text = {
                Column {
                    availableTimes.forEach { time ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateSettings(settings.copy(reminderTime = time))
                                    showTimeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.reminderTime == time,
                                onClick = {
                                    onUpdateSettings(settings.copy(reminderTime = time))
                                    showTimeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(time, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimeDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Restaurer les démos ?") },
            text = { Text("Cette action va réinitialiser les plantes de votre jardin avec les plantes de démonstration.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDemoData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F))
                ) {
                    Text("Restaurer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Supprimer tout ?") },
            text = { Text("Êtes-vous sûr de vouloir supprimer définitivement toutes vos plantes et l'historique de soins ?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAllData()
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
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = Color(0xFF2D6A4F),
    titleColor: Color = Color(0xFF1B3B2B),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = titleColor)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
