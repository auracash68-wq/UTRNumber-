package com.example.data

import android.content.Context
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BackgroundStyle

class SettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("calculator_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "app_theme_ordinal"
        private const val KEY_BACKGROUND = "bg_style_ordinal"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_SOUND_VOLUME = "sound_volume"
        private const val KEY_ANIMATION_ENABLED = "animations_enabled"
    }

    var theme: AppTheme
        get() {
            val ordinal = prefs.getInt(KEY_THEME, AppTheme.SLEEK_INTERFACE.ordinal)
            return AppTheme.values().getOrElse(ordinal) { AppTheme.SLEEK_INTERFACE }
        }
        set(value) {
            prefs.edit().putInt(KEY_THEME, value.ordinal).apply()
        }

    var background: BackgroundStyle
        get() {
            val ordinal = prefs.getInt(KEY_BACKGROUND, BackgroundStyle.GRADIENT.ordinal)
            return BackgroundStyle.values().getOrElse(ordinal) { BackgroundStyle.GRADIENT }
        }
        set(value) {
            prefs.edit().putInt(KEY_BACKGROUND, value.ordinal).apply()
        }

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()
        }

    var soundVolume: Float
        get() = prefs.getFloat(KEY_SOUND_VOLUME, 0.5f)
        set(value) {
            prefs.edit().putFloat(KEY_SOUND_VOLUME, value).apply()
        }

    var isAnimationEnabled: Boolean
        get() = prefs.getBoolean(KEY_ANIMATION_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_ANIMATION_ENABLED, value).apply()
        }
}
