package com.example.greenbuddy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.greenbuddy.model.GrowthRecord
import com.example.greenbuddy.model.HealthStatus
import com.example.greenbuddy.model.Plant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HealthBadge(
    status: HealthStatus,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = status.backgroundColor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = status.label,
            color = status.textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PlantCard(
    plant: Plant,
    onClick: () -> Unit,
    onWaterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val healthStatus = plant.getHealthStatus()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // 3D Canvas Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFEFF5F0))
            ) {
                Plant3DCanvas(
                    species = plant.species,
                    healthStatus = healthStatus,
                    growthLevel = plant.growthLevel,
                    interactive = false,
                    modifier = Modifier.fillMaxSize()
                )

                // Level pill badge
                Surface(
                    color = Color(0xFF1B4332).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "Niv. ${plant.growthLevel}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(modifier = Modifier.padding(horizontal = 6.dp)) {
                Text(
                    text = plant.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF1B3B2B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${plant.species.displayName} · ${plant.location}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                HealthBadge(status = healthStatus)

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onWaterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE3F2FD),
                        contentColor = Color(0xFF1976D2)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Arrosée",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

enum class NavigationTab(val title: String) {
    JARDIN("Jardin"),
    RAPPELS("Rappels"),
    CHATBOT("Chatbot"),
    REGLAGES("Réglages")
}

@Composable
fun BottomNavBar(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    overdueTaskCount: Int = 0
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Jardin
            NavItem(
                icon = Icons.Default.LocalFlorist,
                label = NavigationTab.JARDIN.title,
                isSelected = selectedTab == NavigationTab.JARDIN,
                onClick = { onTabSelected(NavigationTab.JARDIN) }
            )

            // Rappels (with badge)
            NavItem(
                icon = Icons.Default.NotificationsNone,
                label = NavigationTab.RAPPELS.title,
                isSelected = selectedTab == NavigationTab.RAPPELS,
                badgeCount = overdueTaskCount,
                onClick = { onTabSelected(NavigationTab.RAPPELS) }
            )

            // Chatbot
            NavItem(
                icon = Icons.Default.ChatBubbleOutline,
                label = NavigationTab.CHATBOT.title,
                isSelected = selectedTab == NavigationTab.CHATBOT,
                onClick = { onTabSelected(NavigationTab.CHATBOT) }
            )

            // Réglages
            NavItem(
                icon = Icons.Default.Settings,
                label = NavigationTab.REGLAGES.title,
                isSelected = selectedTab == NavigationTab.REGLAGES,
                onClick = { onTabSelected(NavigationTab.REGLAGES) }
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Color(0xFFD8F3DC) else Color.Transparent)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) Color(0xFF1B4332) else Color.Gray,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-2).dp)
                        .size(18.dp)
                        .background(Color(0xFFC62828), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF1B4332) else Color.Gray
        )
    }
}

@Composable
fun LineChartCanvas(
    growthRecords: List<GrowthRecord>,
    modifier: Modifier = Modifier
) {
    if (growthRecords.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFFF7FAF7), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Aucune mesure de croissance enregistrée", color = Color.Gray, fontSize = 13.sp)
        }
        return
    }

    val sortedRecords = growthRecords.sortedBy { it.dateMillis }
    val minHeight = (sortedRecords.minOfOrNull { it.heightCm } ?: 0f) * 0.8f
    val maxHeight = (sortedRecords.maxOfOrNull { it.heightCm } ?: 10f) * 1.2f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF7FAF7), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Courbe d'évolution (cm)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val w = size.width
            val h = size.height
            val paddingLeft = 30.dp.toPx()
            val paddingBottom = 24.dp.toPx()
            val graphW = w - paddingLeft - 10.dp.toPx()
            val graphH = h - paddingBottom - 10.dp.toPx()

            // Draw Y Grid lines
            val gridSteps = 3
            for (i in 0..gridSteps) {
                val ratio = i / gridSteps.toFloat()
                val y = graphH * (1f - ratio) + 10.dp.toPx()
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(paddingLeft, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }

            val points = sortedRecords.mapIndexed { index, record ->
                val xRatio = if (sortedRecords.size > 1) index / (sortedRecords.size - 1).toFloat() else 0.5f
                val yRatio = (record.heightCm - minHeight) / (maxHeight - minHeight).coerceAtLeast(1f)

                val x = paddingLeft + xRatio * graphW
                val y = 10.dp.toPx() + graphH * (1f - yRatio)
                Pair(x, y)
            }

            if (points.size > 1) {
                val path = Path().apply {
                    moveTo(points[0].first, points[0].second)
                    for (i in 1 until points.size) {
                        val p1 = points[i - 1]
                        val p2 = points[i]
                        val cx = (p1.first + p2.first) / 2f
                        cubicTo(cx, p1.second, cx, p2.second, p2.first, p2.second)
                    }
                }

                drawPath(
                    path = path,
                    color = Color(0xFF2E7D32),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Draw Points
            points.forEachIndexed { index, pt ->
                drawCircle(
                    color = Color(0xFF1B4332),
                    radius = 5.dp.toPx(),
                    center = Offset(pt.first, pt.second)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = Offset(pt.first, pt.second)
                )
            }
        }
    }
}
