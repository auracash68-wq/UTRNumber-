package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.BackgroundStyle
import com.example.ui.theme.ThemeColors

@Composable
fun BackgroundView(
    style: BackgroundStyle,
    themeColors: ThemeColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = themeColors.mainBackground))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            when (style) {
                BackgroundStyle.GRADIENT -> {
                    // Standard visual linear gradient, no custom overlay canvas needed
                }
                BackgroundStyle.NATURE_PEAK -> {
                    // Draw clean minimalist mountains/peaks
                    val peakColors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                    val path1 = Path().apply {
                        moveTo(0f, height * 0.75f)
                        lineTo(width * 0.35f, height * 0.45f)
                        lineTo(width * 0.7f, height * 0.85f)
                        lineTo(width, height * 0.6f)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(
                        path = path1,
                        brush = Brush.verticalGradient(colors = peakColors, startY = height * 0.45f, endY = height)
                    )

                    val path2 = Path().apply {
                        moveTo(0f, height * 0.82f)
                        lineTo(width * 0.55f, height * 0.55f)
                        lineTo(width, height * 0.9f)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(
                        path = path2,
                        brush = Brush.verticalGradient(colors = peakColors, startY = height * 0.55f, endY = height)
                    )
                }
                BackgroundStyle.RIVER_BEND -> {
                    // Draw waving translucent river ribbons
                    val path = Path().apply {
                        moveTo(0f, height * 0.3f)
                        cubicTo(
                            width * 0.25f, height * 0.2f,
                            width * 0.4f, height * 0.5f,
                            width * 0.65f, height * 0.4f
                        )
                        cubicTo(
                            width * 0.85f, height * 0.3f,
                            width * 0.9f, height * 0.7f,
                            width, height * 0.6f
                        )
                    }
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.06f),
                        style = Stroke(width = 40f)
                    )
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.04f),
                        style = Stroke(width = 80f)
                    )
                }
                BackgroundStyle.FOREST_TREES -> {
                    // Draw gorgeous geometric pine trees representation
                    val baseHeight = height * 0.85f
                    val treeBrush = Brush.verticalGradient(colors = listOf(Color.White.copy(alpha = 0.06f), Color.Transparent))
                    
                    listOf(0.2f, 0.45f, 0.75f).forEach { xRatio ->
                        val centerX = width * xRatio
                        val path = Path().apply {
                            moveTo(centerX, baseHeight - 220f)
                            lineTo(centerX - 90f, baseHeight)
                            lineTo(centerX + 90f, baseHeight)
                            close()
                        }
                        drawPath(path = path, brush = treeBrush)
                        
                        val pathTop = Path().apply {
                            moveTo(centerX, baseHeight - 340f)
                            lineTo(centerX - 70f, baseHeight - 120f)
                            lineTo(centerX + 70f, baseHeight - 120f)
                            close()
                        }
                        drawPath(path = pathTop, brush = treeBrush)
                    }
                }
                BackgroundStyle.TRAVEL_ROUTE -> {
                    // Transit points & flight dashed connections
                    val path = Path().apply {
                        moveTo(width * 0.15f, height * 0.25f)
                        quadraticTo(width * 0.5f, height * 0.15f, width * 0.85f, height * 0.42f)
                        quadraticTo(width * 0.5f, height * 0.65f, width * 0.2f, height * 0.85f)
                    }
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.07f),
                        style = Stroke(
                            width = 6f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                intervals = floatArrayOf(20f, 15f),
                                phase = 0f
                            )
                        )
                    )
                    drawCircle(color = Color.White.copy(alpha = 0.15f), radius = 18f, center = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.25f))
                    drawCircle(color = Color.White.copy(alpha = 0.15f), radius = 18f, center = androidx.compose.ui.geometry.Offset(width * 0.85f, height * 0.42f))
                    drawCircle(color = Color.White.copy(alpha = 0.15f), radius = 18f, center = androidx.compose.ui.geometry.Offset(width * 0.2f, height * 0.85f))
                }
                BackgroundStyle.ABSTRACT_MESH -> {
                    // Abstract floating network points
                    val points = listOf(
                        androidx.compose.ui.geometry.Offset(width * 0.2f, height * 0.15f),
                        androidx.compose.ui.geometry.Offset(width * 0.8f, height * 0.25f),
                        androidx.compose.ui.geometry.Offset(width * 0.5f, height * 0.45f),
                        androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.65f),
                        androidx.compose.ui.geometry.Offset(width * 0.75f, height * 0.75f),
                        androidx.compose.ui.geometry.Offset(width * 0.4f, height * 0.9f)
                    )
                    val meshColor = Color.White.copy(alpha = 0.04f)
                    val nodeColor = Color.White.copy(alpha = 0.08f)
                    
                    // Draw lines between neighbors
                    for (i in points.indices) {
                        for (j in i + 1 until points.size) {
                            val distSq = (points[i].x - points[j].x)*(points[i].x - points[j].x) + (points[i].y - points[j].y)*(points[i].y - points[j].y)
                            if (distSq < width * width * 0.4f) {
                                drawLine(color = meshColor, start = points[i], end = points[j], strokeWidth = 3f)
                            }
                        }
                    }
                    
                    // Draw circles at nodes
                    points.forEach { pt ->
                        drawCircle(color = nodeColor, radius = 12f, center = pt)
                    }
                }
            }
        }
    }
}
