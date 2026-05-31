package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sound.ClickType
import com.example.sound.SoundManager
import com.example.ui.CalculatorViewModel
import androidx.compose.ui.graphics.Brush
import com.example.ui.components.CalculatorButton
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeColors

@Composable
fun MainCalculatorScreen(
    viewModel: CalculatorViewModel,
    colors: ThemeColors,
    soundManager: SoundManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val inputExpr by viewModel.inputExpression.collectAsState()
    val resultText by viewModel.resultText.collectAsState()
    val realTimePreview by viewModel.realTimePreview.collectAsState()
    val isDegreeMode by viewModel.isDegreeMode.collectAsState()
    val isScientificExpanded by viewModel.isScientificExpanded.collectAsState()
    val memoryVal by viewModel.memoryValue.collectAsState()
    val isAnimEnabled by viewModel.isAnimationEnabled.collectAsState()

    val copyToClipboard = { textToCopy: String ->
        if (textToCopy.isNotEmpty()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Calculator Result", textToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied result to clipboard!", Toast.LENGTH_SHORT).show()
        }
    }

    val pasteFromClipboard = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val item = clipboard.primaryClip?.getItemAt(0)
        val text = item?.text?.toString() ?: ""
        if (text.isNotEmpty()) {
            viewModel.pasteExpression(text)
            Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
        }
    }

    val currentTheme by viewModel.currentTheme.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 12.dp, bottom = 0.dp)
    ) {
        // --- 1. GLASSMORPHIC DISPLAY CARD ---
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = if (currentTheme == AppTheme.SLEEK_INTERFACE) Color.Transparent else colors.displayCardBg,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .padding(horizontal = 12.dp)
                .then(
                    if (currentTheme == AppTheme.SLEEK_INTERFACE) {
                        Modifier
                    } else {
                        Modifier.border(1.dp, colors.displayCardBorder, RoundedCornerShape(28.dp))
                    }
                )
                .padding(16.dp)
                .testTag("calculator_display_screen")
        ) {
            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxSize()
            ) {
                // Clipboard utilities & Mode label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Degrees/Radians status badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.functionBtnBg.copy(alpha = 0.3f))
                            .border(1.dp, colors.displayCardBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                soundManager.playClick(ClickType.FUNCTION)
                                viewModel.toggleDegreeMode()
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isDegreeMode) "DEG" else "RAD",
                            color = colors.numberBtnText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row {
                        IconButton(onClick = {
                            soundManager.playClick(ClickType.FUNCTION)
                            pasteFromClipboard()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste formula",
                                tint = colors.numberBtnText.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(onClick = {
                            if (resultText.isNotEmpty()) {
                                soundManager.playClick(ClickType.FUNCTION)
                                copyToClipboard(resultText)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy result",
                                tint = colors.numberBtnText.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Formula Input text field
                Text(
                    text = inputExpr.ifEmpty { "0" },
                    color = colors.numberBtnText,
                    fontSize = if (inputExpr.length > 15) 26.sp else 36.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.End,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .testTag("expression_field")
                )

                // Real-time computation preview
                if (realTimePreview.isNotEmpty()) {
                    Text(
                        text = realTimePreview,
                        color = colors.numberBtnText.copy(alpha = 0.5f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }

                // Final Evaluation Output text
                if (resultText.isNotEmpty()) {
                    Text(
                        text = "= $resultText",
                        color = if (currentTheme == AppTheme.SLEEK_INTERFACE) Color(0xFFC084FC) else colors.equalsBtnBg,
                        fontSize = if (resultText.length > 12) 28.sp else 38.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .testTag("result_field")
                    )
                }

                if (currentTheme == AppTheme.SLEEK_INTERFACE) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. MEMORY AND PANEL STATE CONTROL ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Memory operations Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("MC", "MR", "M+", "M-").forEach { op ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.numberBtnBg.copy(alpha = 0.4f))
                            .border(1.dp, colors.displayCardBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable {
                                soundManager.playClick(ClickType.FUNCTION)
                                when (op) {
                                    "MC" -> viewModel.memoryClear()
                                    "MR" -> viewModel.memoryRecall()
                                    "M+" -> viewModel.memoryAdd()
                                    "M-" -> viewModel.memorySubtract()
                                }
                                Toast.makeText(context, "$op Triggered (M=${memoryVal})", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = op,
                            color = colors.numberBtnText.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Scientific expand switch button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isScientificExpanded) colors.equalsBtnBg else colors.functionBtnBg)
                    .clickable {
                        soundManager.playClick(ClickType.FUNCTION)
                        viewModel.toggleScientificPanel()
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isScientificExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand Scientific options",
                        tint = if (isScientificExpanded) colors.equalsBtnText else colors.functionBtnText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "SCI",
                        color = if (isScientificExpanded) colors.equalsBtnText else colors.functionBtnText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- 3. EXPANDABLE SCIENTIFIC KEYBOARD PANEL ---
        AnimatedVisibility(
            visible = isScientificExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.displayCardBg.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .border(1.dp, colors.displayCardBorder.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(6.dp)
            ) {
                val sciKeys = listOf(
                    listOf("sin", "cos", "tan", "log"),
                    listOf("asin", "acos", "atan", "ln"),
                    listOf("^", "sqrt", "cbrt", "abs"),
                    listOf("pi", "e", "mod", "!")
                )

                sciKeys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.forEach { op ->
                            val readableOp = when (op) {
                                "pi" -> "π"
                                else -> op
                            }
                            CalculatorButton(
                                text = readableOp,
                                textColor = colors.functionBtnText,
                                backgroundColor = colors.functionBtnBg,
                                onClick = {
                                    soundManager.playClick(ClickType.FUNCTION)
                                    when (op) {
                                        "pi" -> viewModel.appendToken("π")
                                        "e" -> viewModel.appendToken("e")
                                        "mod" -> viewModel.appendToken("mod")
                                        "!" -> viewModel.appendToken("!")
                                        "^" -> viewModel.appendToken("^")
                                        "sqrt" -> viewModel.appendToken("sqrt(")
                                        "cbrt" -> viewModel.appendToken("cbrt(")
                                        "abs" -> viewModel.appendToken("abs(")
                                        else -> viewModel.appendToken("$op(") // trigonometric methods
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                isAnimationEnabled = isAnimEnabled
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- 4. STANDARD BASIC KEYBOARD BOARD ---
        val basicKeys = listOf(
            listOf("AC", "(", ")", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("+/-", "0", ".", "=")
        )

        Box(
            modifier = if (currentTheme == AppTheme.SLEEK_INTERFACE) {
                Modifier
                    .fillMaxWidth()
                    .weight(3.5f)
                    .background(Color(0xFF14151C), RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .padding(horizontal = 12.dp)
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                basicKeys.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { op ->
                        val isOp = op == "÷" || op == "×" || op == "−" || op == "+" || op == "="
                        val isAc = op == "AC" || op == "(" || op == ")" || op == "+/-"
                        
                        val textColor = when {
                            op == "AC" && currentTheme == AppTheme.SLEEK_INTERFACE -> Color(0xFFFB7185) // rose-400 AC
                            op == "=" -> colors.equalsBtnText
                            isOp -> colors.operatorBtnText
                            isAc -> colors.functionBtnText
                            else -> colors.numberBtnText
                        }

                        val bgColor = when {
                            op == "AC" && currentTheme == AppTheme.SLEEK_INTERFACE -> Color(0x0DF43F5E) // rose-500/5
                            op == "=" -> if (currentTheme == AppTheme.SLEEK_INTERFACE) Color(0xFF8B5CF6) else colors.equalsBtnBg
                            isOp -> colors.operatorBtnBg
                            isAc -> colors.functionBtnBg
                            else -> colors.numberBtnBg
                        }

                        val glow = if (op == "=") colors.accentGlow else Color.Transparent

                        CalculatorButton(
                            text = op,
                            textColor = textColor,
                            backgroundColor = bgColor,
                            onClick = {
                                when (op) {
                                    "=" -> {
                                        soundManager.playClick(ClickType.EQUALS)
                                        viewModel.evaluateFinal()
                                    }
                                    "AC" -> {
                                        soundManager.playClick(ClickType.OPERATOR)
                                        viewModel.handleClear()
                                    }
                                    "+/-" -> {
                                        soundManager.playClick(ClickType.STANDARD)
                                        viewModel.appendToken("-")
                                    }
                                    else -> {
                                        // Standard operators vs number tap sounds
                                        if (isOp) {
                                            soundManager.playClick(ClickType.OPERATOR)
                                        } else {
                                            soundManager.playClick(ClickType.STANDARD)
                                        }
                                        viewModel.appendToken(op)
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            glowColor = glow,
                            isAnimationEnabled = isAnimEnabled
                        )
                    }
                }
            }
        }
    }
}
}
