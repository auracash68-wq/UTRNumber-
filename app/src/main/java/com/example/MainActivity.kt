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
import androidx.compose.foundation.background
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

            var showSplash by remember { mutableStateOf(true) }
            var splashProgress by remember { mutableStateOf(0f) }

            LaunchedEffect(Unit) {
                val duration = 1800L
                val steps = 60
                val delayTime = duration / steps
                for (i in 1..steps) {
                    kotlinx.coroutines.delay(delayTime)
                    splashProgress = i.toFloat() / steps
                }
                showSplash = false
            }

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

                // --- C. PROFESSIONAL SPLASH SCREEN WITH SMOOTH PROGRESS ---
                if (showSplash) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F1016)), // Deep dark slate background 
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Rounded calculator block container
                            Card(
                                modifier = Modifier
                                    .size(96.dp)
                                    .padding(8.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF161824)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Calculate,
                                        contentDescription = "nano calculate logo",
                                        tint = Color(0xFFC084FC),
                                        modifier = Modifier.size(56.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "nano calculate",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "The Professional Matrix Solver",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.5f),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(48.dp))
                            
                            // Professional progress bar loader
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .width(180.dp)
                                        .height(6.dp)
                                        .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(3.dp))
                                        .background(Color(0xFF14151D), RoundedCornerShape(3.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(splashProgress)
                                            .background(
                                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFF8B5CF6), Color(0xFFC084FC))
                                                ),
                                                RoundedCornerShape(3.dp)
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Initializing System... ${(splashProgress * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC084FC).copy(alpha = 0.85f),
                                    letterSpacing = 1.sp
                                )
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
