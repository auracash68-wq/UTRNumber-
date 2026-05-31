package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
    val bgBrush = Brush.linearGradient(
        colors = themeColors.mainBackground,
        start = Offset(0f, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = bgBrush)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (width == 0f || height == 0f) return@Canvas

            when (style) {
                BackgroundStyle.GRADIENT -> {
                    // Soft glowing ambient circles
                    drawCircle(
                        color = themeColors.accentGlow.copy(alpha = 0.15f),
                        radius = width * 0.4f,
                        center = Offset(width * 0.2f, height * 0.3f)
                    )
                    drawCircle(
                        color = themeColors.accentGlow.copy(alpha = 0.12f),
                        radius = width * 0.3f,
                        center = Offset(width * 0.8f, height * 0.7f)
                    )
                }
                BackgroundStyle.NATURE_PEAK -> {
                    // Draw stylish mountain ranges
                    val mountainPath1 = Path().apply {
                        moveTo(0f, height * 0.85f)
                        lineTo(width * 0.3f, height * 0.62f)
                        lineTo(width * 0.65f, height * 0.82f)
                        lineTo(width * 0.85f, height * 0.70f)
                        lineTo(width, height * 0.80f)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    val mountainPath2 = Path().apply {
                        moveTo(0f, height * 0.90f)
                        lineTo(width * 0.15f, height * 0.75f)
                        lineTo(width * 0.45f, height * 0.68f)
                        lineTo(width * 0.75f, height * 0.80f)
                        lineTo(width, height * 0.75f)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }

                    // Draw abstract sun / moon
                    drawCircle(
                        color = themeColors.accentGlow.copy(alpha = 0.25f),
                        radius = width * 0.12f,
                        center = Offset(width * 0.75f, height * 0.35f)
                    )

                    drawPath(
                        path = mountainPath1,
                        color = themeColors.accentGlow.copy(alpha = 0.08f)
                    )
                    drawPath(
                        path = mountainPath2,
                        color = themeColors.accentGlow.copy(alpha = 0.15f)
                    )
                }
                BackgroundStyle.RIVER_BEND -> {
                    // Curved winding river waves flowing down
                    val wavePath1 = Path()
                    val wavePath2 = Path()

                    wavePath1.moveTo(0f, height * 0.4f)
                    wavePath1.cubicTo(
                        width * 0.35f, height * 0.35f,
                        width * 0.65f, height * 0.55f,
                        width, height * 0.5f
                    )

                    wavePath2.moveTo(0f, height * 0.45f)
                    wavePath2.cubicTo(
                        width * 0.30f, height * 0.48f,
                        width * 0.70f, height * 0.38f,
                        width, height * 0.55f
                    )

                    drawPath(
                        path = wavePath1,
                        color = themeColors.accentGlow.copy(alpha = 0.12f),
                        style = Stroke(width = 8f)
                    )
                    drawPath(
                        path = wavePath2,
                        color = themeColors.accentGlow.copy(alpha = 0.18f),
                        style = Stroke(width = 5f)
                    )
                    
                    // Simple ambient water drops
                    drawCircle(color = themeColors.accentGlow.copy(alpha = 0.1f), radius = 10f, center = Offset(width * 0.15f, height * 0.25f))
                    drawCircle(color = themeColors.accentGlow.copy(alpha = 0.08f), radius = 18f, center = Offset(width * 0.35f, height * 0.18f))
                    drawCircle(color = themeColors.accentGlow.copy(alpha = 0.12f), radius = 14f, center = Offset(width * 0.80f, height * 0.28f))
                }
                BackgroundStyle.FOREST_TREES -> {
                    // Triangular stylized pine trees in forest background
                    val treePositions = listOf(
                        Offset(width * 0.2f, height * 0.85f),
                        Offset(width * 0.45f, height * 0.82f),
                        Offset(width * 0.75f, height * 0.88f),
                        Offset(width * 0.15f, height * 0.75f),
                        Offset(width * 0.85f, height * 0.72f)
                    )

                    treePositions.forEachIndexed { index, pos ->
                        val treeScale = if (index < 3) 1.2f else 0.8f
                        val alpha = if (index < 3) 0.15f else 0.07f
                        val baseW = 100f * treeScale
                        val baseH = 150f * treeScale

                        val path = Path().apply {
                            moveTo(pos.x, pos.y - baseH)
                            lineTo(pos.x - baseW / 2, pos.y)
                            lineTo(pos.x + baseW / 2, pos.y)
                            close()
                        }
                        
                        val middlePath = Path().apply {
                            moveTo(pos.x, pos.y - baseH * 1.3f)
                            lineTo(pos.x - baseW * 0.4f, pos.y - baseH * 0.4f)
                            lineTo(pos.x + baseW * 0.4f, pos.y - baseH * 0.4f)
                            close()
                        }

                        drawPath(path, themeColors.accentGlow.copy(alpha = alpha))
                        drawPath(middlePath, themeColors.accentGlow.copy(alpha = alpha * 0.8f))
                    }
                }
                BackgroundStyle.TRAVEL_ROUTE -> {
                    // Stylized grids, dashed travel railway lines, map markings
                    for (i in 0..6) {
                        val y = height * (0.2f + i * 0.12f)
                        drawLine(
                            color = themeColors.accentGlow.copy(alpha = 0.05f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 3f
                        )
                    }

                    // A dotted line tracing a journey
                    val dotPath = Path().apply {
                        moveTo(width * 0.1f, height * 0.80f)
                        quadraticTo(
                            width * 0.4f, height * 0.65f,
                            width * 0.6f, height * 0.82f
                        )
                        quadraticTo(
                            width * 0.8f, height * 0.95f,
                            width * 0.9f, height * 0.70f
                        )
                    }
                    drawPath(
                        path = dotPath,
                        color = themeColors.accentGlow.copy(alpha = 0.15f),
                        style = Stroke(
                            width = 6f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(15f, 15f), 0f
                            )
                        )
                    )

                    // Draw simple travel stops
                    drawCircle(color = themeColors.equalsBtnBg.copy(alpha = 0.6f), radius = 12f, center = Offset(width * 0.1f, height * 0.80f))
                    drawCircle(color = themeColors.equalsBtnBg.copy(alpha = 0.6f), radius = 12f, center = Offset(width * 0.9f, height * 0.70f))
                }
                BackgroundStyle.ABSTRACT_MESH -> {
                    // Fluid grid line points
                    val points = mutableListOf<Offset>()
                    val rows = 8
                    val cols = 6
                    val dy = height / rows
                    val dx = width / cols
                    
                    for (r in 0..rows) {
                        for (c in 0..cols) {
                            // Soft offset wobble
                            val wobbleX = if (r % 2 == 0) dx * 0.15f else -dx * 0.15f
                            points.add(Offset(c * dx + wobbleX, r * dy))
                        }
                    }

                    // Draw clean cross marks
                    points.forEach { pt ->
                        drawCircle(
                            color = themeColors.accentGlow.copy(alpha = 0.08f),
                            radius = 4f,
                            center = pt
                        )
                    }
                }
            }
        }
    }
}
