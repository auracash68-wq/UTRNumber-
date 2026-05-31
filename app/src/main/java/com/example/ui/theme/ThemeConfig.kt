package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppTheme(val displayName: String) {
    OCEAN("Ocean Theme"),
    FOREST("Forest Theme"),
    SUNSET("Sunset Theme"),
    NIGHT("Night Theme"),
    TRAVEL("Travel Theme"),
    PURPLE_NEON("Purple Neon"),
    SLEEK_INTERFACE("Sleek Interface")
}

enum class BackgroundStyle(val displayName: String) {
    GRADIENT("Dynamic Gradient"),
    NATURE_PEAK("Nature Peaks"),
    RIVER_BEND("River Flow"),
    FOREST_TREES("Abundant Forest"),
    TRAVEL_ROUTE("Transit Patterns"),
    ABSTRACT_MESH("Abstract Mesh")
}

data class ThemeColors(
    val mainBackground: List<Color>,
    val displayCardBg: Color,
    val displayCardBorder: Color,
    val numberBtnBg: Color,
    val numberBtnText: Color,
    val operatorBtnBg: Color,
    val operatorBtnText: Color,
    val functionBtnBg: Color,
    val functionBtnText: Color,
    val equalsBtnBg: Color,
    val equalsBtnText: Color,
    val accentGlow: Color,
    val isDark: Boolean
)

object ThemeConfig {
    fun getColorsForTheme(theme: AppTheme): ThemeColors {
        return when (theme) {
            AppTheme.OCEAN -> ThemeColors(
                mainBackground = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)), // Blue Ocean
                displayCardBg = Color(0x2600E5FF), // Teal Translucent
                displayCardBorder = Color(0x6600E5FF),
                numberBtnBg = Color(0x1F2C5364),
                numberBtnText = Color(0xFFE0F7FA),
                operatorBtnBg = Color(0xFF00bcd4),
                operatorBtnText = Color(0xFFFFFFFF),
                functionBtnBg = Color(0x3300E5FF),
                functionBtnText = Color(0xFFE0F7FA),
                equalsBtnBg = Color(0xFF00E5FF),
                equalsBtnText = Color(0xFF0F2027),
                accentGlow = Color(0x9900E5FF),
                isDark = true
            )
            AppTheme.FOREST -> ThemeColors(
                mainBackground = listOf(Color(0xFF11998e), Color(0xFF38ef7d)), // Herbal Mint
                displayCardBg = Color(0x26FFFFFF),
                displayCardBorder = Color(0x55E0F2F1),
                numberBtnBg = Color(0x2611998E),
                numberBtnText = Color(0xFFE8F5E9),
                operatorBtnBg = Color(0xFF1B5E20),
                operatorBtnText = Color(0xFFFFFFFF),
                functionBtnBg = Color(0x33A5D6A7),
                functionBtnText = Color(0xFFC8E6C9),
                equalsBtnBg = Color(0xFF2E7D32),
                equalsBtnText = Color(0xFFFFFFFF),
                accentGlow = Color(0x8838EF7D),
                isDark = false
            )
            AppTheme.SUNSET -> ThemeColors(
                mainBackground = listOf(Color(0xFFF12711), Color(0xFFF5AF19)), // Orange Sunset
                displayCardBg = Color(0x33FFFFFF),
                displayCardBorder = Color(0x66FFEB3B),
                numberBtnBg = Color(0x1AF5AF19),
                numberBtnText = Color(0xFFFFFDE7),
                operatorBtnBg = Color(0xFFE65100),
                operatorBtnText = Color(0xFFFFFFFF),
                functionBtnBg = Color(0x44FF9800),
                functionBtnText = Color(0xFFFFF3E0),
                equalsBtnBg = Color(0xFFFF6D00),
                equalsBtnText = Color(0xFFFFFFFF),
                accentGlow = Color(0x99FFEB3B),
                isDark = false
            )
            AppTheme.NIGHT -> ThemeColors(
                mainBackground = listOf(Color(0xFF0D0D0C), Color(0xFF15161E), Color(0xFF1E1F29)), // Carbon Slate
                displayCardBg = Color(0x1900FFFF), // Cyan Translucent
                displayCardBorder = Color(0x4400FFFF),
                numberBtnBg = Color(0xFF23252F),
                numberBtnText = Color(0xFFE4E4E6),
                operatorBtnBg = Color(0xFF4A4B56),
                operatorBtnText = Color(0xFFFFFFFF),
                functionBtnBg = Color(0xFF2E303D),
                functionBtnText = Color(0xFF00FFCC),
                equalsBtnBg = Color(0xFF00E5FF),
                equalsBtnText = Color(0xFF000000),
                accentGlow = Color(0x8800FFFF),
                isDark = true
            )
            AppTheme.TRAVEL -> ThemeColors(
                mainBackground = listOf(Color(0xFF654ea3), Color(0xFFeaafc8)), // Pastel route (Purple Lavender)
                displayCardBg = Color(0x26FFFFFF),
                displayCardBorder = Color(0x44FFFFFF),
                numberBtnBg = Color(0x22FFFFFF),
                numberBtnText = Color(0xFFFFFFFF),
                operatorBtnBg = Color(0xFF9C27B0),
                operatorBtnText = Color(0xFFFFFFFF),
                functionBtnBg = Color(0x33F06292),
                functionBtnText = Color(0xFFFFFFFF),
                equalsBtnBg = Color(0xFFFF4081),
                equalsBtnText = Color(0xFFFFFFFF),
                accentGlow = Color(0x66EAAFC8),
                isDark = true
            )
            AppTheme.PURPLE_NEON -> ThemeColors(
                mainBackground = listOf(Color(0xFF0B001A), Color(0xFF1A0033), Color(0xFF10002B)), // Intense Neon dark
                displayCardBg = Color(0x19E000FF), // Glowing Neon Magenta
                displayCardBorder = Color(0x88E000FF),
                numberBtnBg = Color(0x1A7B2CBF),
                numberBtnText = Color(0xFFE0AAFF),
                operatorBtnBg = Color(0xFF9D4EDD),
                operatorBtnText = Color(0xFFFFFFFF),
                functionBtnBg = Color(0x33C77DFF),
                functionBtnText = Color(0xFFE0AAFF),
                equalsBtnBg = Color(0xFFE000FF),
                equalsBtnText = Color(0xFFFFFFFF),
                accentGlow = Color(0xCCE000FF),
                isDark = true
            )
            AppTheme.SLEEK_INTERFACE -> ThemeColors(
                mainBackground = listOf(Color(0xFF0A0B10), Color(0xFF0C0D15)), // Pitch black/charcoal dark background
                displayCardBg = Color(0x02FFFFFF), // transparent glass display area
                displayCardBorder = Color(0x0DFFFFFF), // extremely subtle white outline
                numberBtnBg = Color(0x0DFFFFFF), // white/5 translucent keys
                numberBtnText = Color(0xFFFFFFFF), // pure clean white labels
                operatorBtnBg = Color(0x0DFFFFFF), // white/5 translucent keys
                operatorBtnText = Color(0xFFC084FC), // purple-400 operator symbols
                functionBtnBg = Color(0x1AA855F7), // purple-500/10 active background
                functionBtnText = Color(0xFFC084FC), // purple-400 function labels
                equalsBtnBg = Color(0xFF8B5CF6), // purple-500 equivalent solid button
                equalsBtnText = Color(0xFFFFFFFF), // pure white text on equals
                accentGlow = Color(0xFFA855F7), // violet highlight glow
                isDark = true
            )
        }
    }
}
