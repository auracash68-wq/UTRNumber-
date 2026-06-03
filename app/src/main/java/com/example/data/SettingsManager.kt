package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BackgroundStyle

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("aura_calculator_prefs", Context.MODE_PRIVATE)

    var theme: AppTheme
        get() {
            val name = prefs.getString("key_theme", AppTheme.OCEAN.name) ?: AppTheme.OCEAN.name
            return try {
                AppTheme.valueOf(name)
            } catch (e: Exception) {
                AppTheme.OCEAN
            }
        }
        set(value) {
            prefs.edit().putString("key_theme", value.name).apply()
        }

    var background: BackgroundStyle
        get() {
            val name = prefs.getString("key_background", BackgroundStyle.GRADIENT.name) ?: BackgroundStyle.GRADIENT.name
            return try {
                BackgroundStyle.valueOf(name)
            } catch (e: Exception) {
                BackgroundStyle.GRADIENT
            }
        }
        set(value) {
            prefs.edit().putString("key_background", value.name).apply()
        }

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("key_sound_enabled", true)
        set(value) {
            prefs.edit().putBoolean("key_sound_enabled", value).apply()
        }

    var soundVolume: Float
        get() = prefs.getFloat("key_sound_volume", 0.5f)
        set(value) {
            prefs.edit().putFloat("key_sound_volume", value).apply()
        }

    var isAnimationEnabled: Boolean
        get() = prefs.getBoolean("key_animation_enabled", true)
        set(value) {
            prefs.edit().putBoolean("key_animation_enabled", value).apply()
        }
}
