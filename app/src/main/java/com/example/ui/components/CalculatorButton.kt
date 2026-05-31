package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorButton(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false,
    glowColor: Color = Color.Transparent,
    isAnimationEnabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth spring physics for scale bounce on depress
    val scaleFactor by animateFloatAsState(
        targetValue = if (isPressed && isAnimationEnabled) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 1200f),
        label = "button_scale"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 2.dp else 4.dp,
            pressedElevation = 1.dp
        ),
        modifier = modifier
            .padding(4.dp)
            .scale(scaleFactor)
            .graphicsLayer {
                // Apply a glowing subtle neon drop-shadow when glow is present
                if (glowColor != Color.Transparent) {
                    shadowElevation = 8.dp.toPx()
                    spotShadowColor = glowColor
                    ambientShadowColor = glowColor
                }
            }
            .testTag("btn_$text"),
        interactionSource = interactionSource,
        onClick = onClick
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = if (text.length > 3) 16.sp else if (isLarge) 24.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = if (text.length > 2) (-0.5).sp else 0.sp
            )
        }
    }
}
