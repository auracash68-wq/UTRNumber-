package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sound.ClickType
import com.example.sound.SoundManager
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BackgroundStyle
import com.example.ui.theme.ThemeColors
import com.example.ui.theme.ThemeConfig

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: CalculatorViewModel,
    colors: ThemeColors,
    soundManager: SoundManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTheme by viewModel.currentTheme.collectAsState()
    val currentBgStyle by viewModel.currentBgStyle.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val soundVolume by viewModel.soundVolume.collectAsState()
    val isAnimEnabled by viewModel.isAnimationEnabled.collectAsState()

    var showResetDbConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // --- 1. THEME SELECTION PANEL ---
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.displayCardBg.copy(alpha = 0.2f),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.displayCardBorder.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Palette, contentDescription = "Themes", tint = colors.equalsBtnBg)
                    Text(
                        text = "Visual Themes",
                        color = colors.numberBtnText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                // Create a beautiful FlowRow grid showing all themes
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 2
                ) {
                    AppTheme.values().forEach { theme ->
                        val isSelected = theme == currentTheme
                        val themeColorProfile = ThemeConfig.getColorsForTheme(theme)
                        
                        // Theme Preview Box Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.linearGradient(colors = themeColorProfile.mainBackground)
                                )
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) colors.equalsBtnBg else colors.displayCardBorder.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    soundManager.playClick(ClickType.FUNCTION)
                                    viewModel.updateTheme(theme)
                                }
                                .padding(10.dp)
                                .testTag("theme_card_${theme.name}")
                        ) {
                            Column {
                                Text(
                                    text = theme.displayName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                // Miniature button display representation
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(themeColorProfile.numberBtnBg, RoundedCornerShape(4.dp))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(themeColorProfile.operatorBtnBg, RoundedCornerShape(4.dp))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(themeColorProfile.equalsBtnBg, RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 2. THEME BACKGROUND STYLE SELECTION ---
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.displayCardBg.copy(alpha = 0.2f),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.displayCardBorder.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Landscape, contentDescription = "Background Settings", tint = colors.equalsBtnBg)
                    Text(
                        text = "Canvas Background Style",
                        color = colors.numberBtnText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable or wrapping background selection chip buttons
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BackgroundStyle.values().forEach { style ->
                        val active = style == currentBgStyle
                        Box(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (active) colors.equalsBtnBg else colors.numberBtnBg.copy(alpha = 0.2f)
                                )
                                .clickable {
                                    soundManager.playClick(ClickType.STANDARD)
                                    viewModel.updateBackground(style)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = style.displayName,
                                color = if (active) colors.equalsBtnText else colors.numberBtnText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // --- 3. SOUND EFFECTS CONFIG PANEL ---
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.displayCardBg.copy(alpha = 0.2f),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.displayCardBorder.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Acoustics", tint = colors.equalsBtnBg)
                    Text(
                        text = "Tactile Audio Settings",
                        color = colors.numberBtnText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Playback Sound Enabled switch row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Responsive Click Sounds", color = colors.numberBtnText, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Synthesize tones for buttons", color = colors.numberBtnText.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                    Switch(
                        checked = isSoundEnabled,
                        onCheckedChange = {
                            soundManager.setEnabled(it)
                            viewModel.toggleSound(it)
                            soundManager.playClick(ClickType.EQUALS)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.equalsBtnBg)
                    )
                }

                // Volume slider row (Visible if enabled)
                if (isSoundEnabled) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Volume Intensity", color = colors.numberBtnText, fontSize = 13.sp)
                            Text("${(soundVolume * 100).toInt()}%", color = colors.numberBtnText.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = soundVolume,
                            onValueChange = {
                                viewModel.updateVolume(it)
                                soundManager.setVolume(it)
                            },
                            onValueChangeFinished = {
                                soundManager.playClick(ClickType.FUNCTION)
                            },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = colors.equalsBtnBg,
                                activeTrackColor = colors.equalsBtnBg
                            )
                        )
                    }
                }
            }
        }

        // --- 4. ANIMATION CONFIG PANEL ---
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.displayCardBg.copy(alpha = 0.2f),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.displayCardBorder.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Animation, contentDescription = "Animations Settings", tint = colors.equalsBtnBg)
                    Text(
                        text = "Motion Settings",
                        color = colors.numberBtnText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Scale Contraction", color = colors.numberBtnText, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Button scales down upon depress", color = colors.numberBtnText.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                    Switch(
                        checked = isAnimEnabled,
                        onCheckedChange = {
                            soundManager.playClick(ClickType.FUNCTION)
                            viewModel.toggleAnimations(it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.equalsBtnBg)
                    )
                }
            }
        }

        // --- 5. SYSTEM DATA PURGE ACTIONS CARD ---
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.displayCardBg.copy(alpha = 0.2f),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.displayCardBorder.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Maintenance", tint = colors.equalsBtnBg)
                    Text(
                        text = "Actions & Maintenance",
                        color = colors.numberBtnText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Button(
                    onClick = {
                        soundManager.playClick(ClickType.OPERATOR)
                        showResetDbConfirm = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear")
                        Text("Wipe Calculations Ledger Database", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 6. ABOUT APP / PRIVACY FOOTER CARD ---
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.displayCardBg.copy(alpha = 0.15f),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.displayCardBorder.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Application Information",
                    color = colors.numberBtnText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Aura Calculator v1.0.0\nFully client-side premium offline compilation.\nNo user analytics or data mining networks embedded.",
                    color = colors.numberBtnText.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                
                Divider(color = colors.displayCardBorder.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Offline Privacy Policy",
                    color = colors.numberBtnText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Aura Calculator operates completely self-contained. All calculation logs, bookmarks, and style states are saved locally in private sandbox SQLite/Room nodes. Your exports are written using MediaStore directly on-device with zero server synchronization.",
                    color = colors.numberBtnText.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // --- 7. CLEAR DATABASE ACTION DIALOG ---
        if (showResetDbConfirm) {
            AlertDialog(
                onDismissRequest = { showResetDbConfirm = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = colors.displayCardBg.copy(alpha = 0.95f),
                title = { Text("Wipe Database", color = colors.numberBtnText, fontWeight = FontWeight.Bold) },
                text = { Text("Are you absolutely sure you want to delete all historical logs? This cannot be undone.", color = colors.numberBtnText.copy(alpha = 0.8f)) },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            soundManager.playClick(ClickType.OPERATOR)
                            viewModel.deleteAllHistory()
                            showResetDbConfirm = false
                            Toast.makeText(context, "All history wiped!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Reset All Now", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDbConfirm = false }) {
                        Text("Cancel", color = colors.numberBtnText)
                    }
                },
                modifier = Modifier.border(1.dp, colors.displayCardBorder, RoundedCornerShape(24.dp))
            )
        }
    }
}
