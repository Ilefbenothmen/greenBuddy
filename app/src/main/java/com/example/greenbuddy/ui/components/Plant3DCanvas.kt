package com.example.greenbuddy.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.greenbuddy.model.HealthStatus
import com.example.greenbuddy.model.PlantSpecies
import kotlin.math.*

@Composable
fun Plant3DGLTFViewer(
    species: PlantSpecies,
    healthStatus: HealthStatus = HealthStatus.EN_FORME,
    modifier: Modifier = Modifier
) {
    val speciesName = species.name.lowercase()
    val statusName = healthStatus.name.lowercase()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF2F6F3))
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    @Suppress("DEPRECATION")
                    settings.allowFileAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    settings.allowUniversalAccessFromFileURLs = true
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                    webViewClient = WebViewClient()
                    loadUrl("file:///android_asset/3d_viewer.html?species=$speciesName&status=$statusName")
                }
            },
            update = { webView ->
                webView.loadUrl("file:///android_asset/3d_viewer.html?species=$speciesName&status=$statusName")
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private data class Point3D(val x: Float, val y: Float, val z: Float)

private data class Face3D(
    val v1: Point3D,
    val v2: Point3D,
    val v3: Point3D,
    val v4: Point3D? = null,
    val color: Color
)

@Composable
fun Plant3DCanvas(
    species: PlantSpecies,
    healthStatus: HealthStatus = HealthStatus.EN_FORME,
    growthLevel: Int = 1,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    showZoomControls: Boolean = false,
    overrideYaw: Float? = null,
    overridePitch: Float? = null,
    overrideZoom: Float? = null
) {
    Plant3DGLTFViewer(
        species = species,
        healthStatus = healthStatus,
        modifier = modifier
    )
}

private fun applyLighting(base: Color, factor: Float): Color {
    val f = 0.4f + 0.6f * factor
    return Color(
        red = (base.red * f).coerceIn(0f, 1f),
        green = (base.green * f).coerceIn(0f, 1f),
        blue = (base.blue * f).coerceIn(0f, 1f),
        alpha = base.alpha
    )
}

private fun generatePlantGeometry(
    species: PlantSpecies,
    healthStatus: HealthStatus,
    growthLevel: Int = 1
): List<Face3D> {
    val faces = mutableListOf<Face3D>()

    val levelScale = when (growthLevel.coerceIn(1, 3)) {
        1 -> 0.7f
        2 -> 0.95f
        else -> 1.25f
    }

    // 1. Terracotta Pot Geometry
    val potTopR = 0.38f * (0.85f + 0.15f * levelScale)
    val potBotR = 0.28f * (0.85f + 0.15f * levelScale)
    val potH = 0.42f * (0.85f + 0.15f * levelScale)
    val potY = -0.35f
    val potColor = Color(0xFFC68B59)
    val potRimColor = Color(0xFFB57A48)
    val potInsideColor = Color(0xFF5D4037)

    val segments = 12
    for (i in 0 until segments) {
        val a1 = (i * 2 * PI / segments).toFloat()
        val a2 = ((i + 1) * 2 * PI / segments).toFloat()

        val x1Top = potTopR * cos(a1)
        val z1Top = potTopR * sin(a1)
        val x2Top = potTopR * cos(a2)
        val z2Top = potTopR * sin(a2)

        val x1Bot = potBotR * cos(a1)
        val z1Bot = potBotR * sin(a1)
        val x2Bot = potBotR * cos(a2)
        val z2Bot = potBotR * sin(a2)

        // Pot side face
        faces.add(
            Face3D(
                v1 = Point3D(x1Bot, potY, z1Bot),
                v2 = Point3D(x2Bot, potY, z2Bot),
                v3 = Point3D(x2Top, potY + potH, z2Top),
                v4 = Point3D(x1Top, potY + potH, z1Top),
                color = potColor
            )
        )

        // Pot rim top
        val rimH = 0.06f
        faces.add(
            Face3D(
                v1 = Point3D(x1Top * 1.05f, potY + potH, z1Top * 1.05f),
                v2 = Point3D(x2Top * 1.05f, potY + potH, z2Top * 1.05f),
                v3 = Point3D(x2Top * 1.05f, potY + potH + rimH, z2Top * 1.05f),
                v4 = Point3D(x1Top * 1.05f, potY + potH + rimH, z1Top * 1.05f),
                color = potRimColor
            )
        )
    }

    // Soil surface inside pot
    faces.add(
        Face3D(
            v1 = Point3D(-potTopR * 0.9f, potY + potH, -potTopR * 0.9f),
            v2 = Point3D(potTopR * 0.9f, potY + potH, -potTopR * 0.9f),
            v3 = Point3D(potTopR * 0.9f, potY + potH, potTopR * 0.9f),
            v4 = Point3D(-potTopR * 0.9f, potY + potH, potTopR * 0.9f),
            color = potInsideColor
        )
    )

    // 2. Leaf and Stem Color Palette by Health Status
    val (primaryLeafColor, accentLeafColor, droopAngle) = when (healthStatus) {
        HealthStatus.EN_FORME -> Triple(Color(0xFF2E7D32), Color(0xFF4CAF50), 0f)
        HealthStatus.ASSOIFFEE -> Triple(Color(0xFF827717), Color(0xFFAFB42B), 0.35f)
        HealthStatus.FLETRIE -> Triple(Color(0xFF5D4037), Color(0xFF8D6E63), 0.75f)
    }

    val baseY = potY + potH

    // 3. Species Specific Foliage Geometry
    when (species) {
        PlantSpecies.ALOE, PlantSpecies.FICUS, PlantSpecies.PILEA, PlantSpecies.STRELITZIA, PlantSpecies.SANSEVIERIA -> {
            val leafCount = when (growthLevel) {
                1 -> 3
                2 -> 5
                else -> 7
            }
            for (i in 0 until leafCount) {
                val angle = i * (2 * PI / leafCount).toFloat() + 0.2f
                val leafRadius = 0.45f * levelScale
                val stemH = 0.35f * levelScale

                val droop = droopAngle * 0.4f
                val lx = cos(angle) * leafRadius
                val lz = sin(angle) * leafRadius
                val ly = baseY + stemH - droop

                val tipX = lx * 1.4f
                val tipZ = lz * 1.4f
                val tipY = ly + 0.15f - droop * 0.6f

                faces.add(
                    Face3D(
                        v1 = Point3D(0f, baseY, 0f),
                        v2 = Point3D(0.02f, baseY, 0.02f),
                        v3 = Point3D(lx + 0.02f, ly, lz + 0.02f),
                        v4 = Point3D(lx, ly, lz),
                        color = primaryLeafColor
                    )
                )

                val side1X = lx - sin(angle) * 0.28f
                val side1Z = lz + cos(angle) * 0.28f
                faces.add(
                    Face3D(
                        v1 = Point3D(lx, ly, lz),
                        v2 = Point3D(side1X, ly + 0.1f - droop, side1Z),
                        v3 = Point3D(tipX, tipY, tipZ),
                        color = primaryLeafColor
                    )
                )

                val side2X = lx + sin(angle) * 0.28f
                val side2Z = lz - cos(angle) * 0.28f
                faces.add(
                    Face3D(
                        v1 = Point3D(lx, ly, lz),
                        v2 = Point3D(tipX, tipY, tipZ),
                        v3 = Point3D(side2X, ly + 0.1f - droop, side2Z),
                        color = accentLeafColor
                    )
                )
            }
        }
    }

    return faces
}
