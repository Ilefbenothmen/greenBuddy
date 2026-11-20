package com.example.greenbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.greenbuddy.model.CareType
import com.example.greenbuddy.model.Plant
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TaskItem(
    val plant: Plant,
    val type: CareType,
    val isOverdue: Boolean,
    val daysOverdue: Int,
    val dueDateMillis: Long
)

@Composable
fun RemindersScreen(
    plants: List<Plant>,
    reminderTime: String,
    onMarkDone: (Plant, CareType) -> Unit,
    onPostpone: (Plant) -> Unit
) {
    val now = System.currentTimeMillis()

    // Calculate due / overdue tasks
    val activeTasks = mutableListOf<TaskItem>()
    val upcomingTasks = mutableListOf<TaskItem>()

    plants.forEach { plant ->
        val daysOverdueWater = plant.getDaysOverdueWatering(now)
        val nextWaterDate = plant.getNextWateringDateMillis()

        if (daysOverdueWater > 0 || nextWaterDate <= now + 86_400_000L) {
            activeTasks.add(
                TaskItem(
                    plant = plant,
                    type = CareType.WATERING,
                    isOverdue = daysOverdueWater > 0,
                    daysOverdue = daysOverdueWater,
                    dueDateMillis = nextWaterDate
                )
            )
        } else {
            upcomingTasks.add(
                TaskItem(
                    plant = plant,
                    type = CareType.WATERING,
                    isOverdue = false,
                    daysOverdue = 0,
                    dueDateMillis = nextWaterDate
                )
            )
        }

        val daysOverdueFert = plant.getDaysOverdueFertilizing(now)
        val nextFertDate = plant.getNextFertilizingDateMillis()
        if (daysOverdueFert > 0 || nextFertDate <= now + 86_400_000L) {
            activeTasks.add(
                TaskItem(
                    plant = plant,
                    type = CareType.FERTILIZATION,
                    isOverdue = daysOverdueFert > 0,
                    daysOverdue = daysOverdueFert,
                    dueDateMillis = nextFertDate
                )
            )
        }
    }

    val dateFormat = SimpleDateFormat("d MMM", Locale.FRENCH)

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
                text = "Rappels du jour",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF1B4332)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Notification quotidienne à $reminderTime",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (activeTasks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Aucun rappel en souffrance !",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1B4332)
                                    )
                                    Text(
                                        text = "Toutes vos plantes sont heureuses et bien hydratées.",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(activeTasks) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (task.type == CareType.WATERING) Color(0xFFE3F2FD) else Color(0xFFE8F5E9)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (task.type == CareType.WATERING) Icons.Default.WaterDrop else Icons.Default.LocalFlorist,
                                            contentDescription = null,
                                            tint = if (task.type == CareType.WATERING) Color(0xFF1976D2) else Color(0xFF2E7D32),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = "${if (task.type == CareType.WATERING) "Arroser" else "Fertiliser"} ${task.plant.name}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1B3B2B)
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = if (task.isOverdue) "En retard de ${task.daysOverdue} j · ${task.plant.location}" else "Aujourd'hui · ${task.plant.location}",
                                            fontSize = 13.sp,
                                            color = if (task.isOverdue) Color(0xFFC62828) else Color(0xFF2E7D32),
                                            fontWeight = if (task.isOverdue) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { onMarkDone(task.plant, task.type) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2D6A4F),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Fait", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { onPostpone(task.plant) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFF1B4332)
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Reporter", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section "Cette semaine"
                if (upcomingTasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Cette semaine",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFF1B4332)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(upcomingTasks.take(5)) { upcoming ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        tint = Color(0xFF1976D2),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Arroser ${upcoming.plant.name}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1B3B2B)
                                    )
                                }

                                Text(
                                    text = dateFormat.format(Date(upcoming.dueDateMillis)),
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
