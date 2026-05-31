package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.admob.AdMobManager
import com.example.sound.ClickType
import com.example.sound.SoundManager
import com.example.ui.CalculatorViewModel
import com.example.ui.components.BackgroundView
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MainCalculatorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ThemeConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

enum class ActiveTab(val label: String, val icon: ImageVector) {
    CALCULATOR("Calculator", Icons.Default.Calculate),
    HISTORY("Ledger", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure fluid edge-to-edge system layouts
        enableEdgeToEdge()

        // Initialize AdMob and download initial Interstitial unit configuration
        AdMobManager.initialize(this)

        // Initialize tactile clicking manager
        soundManager = SoundManager(this).apply {
            setEnabled(viewModel.isSoundEnabled.value)
            setVolume(viewModel.soundVolume.value)
        }

        setContent {
            val currentTheme by viewModel.currentTheme.collectAsState()
            val currentBgStyle by viewModel.currentBgStyle.collectAsState()
            val colors = ThemeConfig.getColorsForTheme(currentTheme)
            
            var activeTab by remember { mutableStateOf(ActiveTab.CALCULATOR) }

            // Sync ViewModel and Sound settings dynamically
            val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
            val soundVolume by viewModel.soundVolume.collectAsState()
            
            LaunchedEffect(isSoundEnabled, soundVolume) {
                soundManager.setEnabled(isSoundEnabled)
                soundManager.setVolume(soundVolume)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // --- A. DYNAMIC CANVAS VECTOR BACKGROUNDS ---
                BackgroundView(style = currentBgStyle, themeColors = colors)

                // --- B. CENTRAL CONTENT SCRIPTS AND DISPLAYS ---
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    topBar = {
                        Column {
                            // System status bar spacer to avoid camera cutout overlaps
                            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                            
                            // Google Test Banner Ad in safe empty area
                            AdBannerContainer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    },
                    bottomBar = {
                        // Premium Glassmorphic Bottom Navigation
                        Surface(
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            color = colors.displayCardBg.copy(alpha = 0.85f),
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = colors.displayCardBorder.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                                )
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonerElevation = 0.dp,
                                modifier = Modifier.navigationBarsPadding(),
                                windowInsets = WindowInsets.navigationBars
                            ) {
                                ActiveTab.values().forEach { tab ->
                                    val isSelected = activeTab == tab
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            soundManager.playClick(ClickType.FUNCTION)
                                            activeTab = tab
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = tab.icon,
                                                contentDescription = tab.label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = colors.equalsBtnBg,
                                            selectedTextColor = colors.equalsBtnBg,
                                            unselectedIconColor = colors.numberBtnText.copy(alpha = 0.5f),
                                            unselectedTextColor = colors.numberBtnText.copy(alpha = 0.5f),
                                            indicatorColor = colors.functionBtnBg.copy(alpha = 0.25f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    // Dynamic fade transitions on tab switches
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = activeTab,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(180))
                            },
                            label = "screen_switch"
                        ) { targetTab ->
                            when (targetTab) {
                                ActiveTab.CALCULATOR -> {
                                    MainCalculatorScreen(
                                        viewModel = viewModel,
                                        colors = colors,
                                        soundManager = soundManager
                                    )
                                }
                                ActiveTab.HISTORY -> {
                                    HistoryScreen(
                                        viewModel = viewModel,
                                        colors = colors,
                                        soundManager = soundManager
                                    )
                                }
                                ActiveTab.SETTINGS -> {
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        colors = colors,
                                        soundManager = soundManager
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdBannerContainer(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(50.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdMobManager.BANNER_AD_UNIT_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

// Extension to override material NavigationBar elevation warning if custom types used
@Composable
private fun NavigationBar(
    containerColor: Color,
    tonerElevation: androidx.compose.ui.unit.Dp,
    modifier: Modifier,
    windowInsets: WindowInsets,
    content: @Composable RowScope.() -> Unit
) {
    NavigationBar(
        containerColor = containerColor,
        tonalElevation = tonerElevation,
        modifier = modifier,
        windowInsets = windowInsets,
        content = content
    )
}
