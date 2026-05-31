package com.example.sound

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.exp
import kotlin.math.sin

enum class ClickType {
    STANDARD, OPERATOR, FUNCTION, EQUALS
}

class SoundManager(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var isEnabled = true
    private var volume = 0.5f

    fun playClick(type: ClickType) {
        if (!isEnabled) return
        
        Thread {
            try {
                val sampleRate = 22050
                // Match sound durations to keep response times under 100ms
                val durationSec = when (type) {
                    ClickType.EQUALS -> 0.15
                    else -> 0.04
                }
                val numSamples = (durationSec * sampleRate).toInt()
                val buffer = ShortArray(numSamples)
                
                // Audio synthesis parameters
                val frequency = when (type) {
                    ClickType.STANDARD -> 1200.0 // crisp tactile pop
                    ClickType.OPERATOR -> 800.0  // deep round tap
                    ClickType.FUNCTION -> 1600.0 // bright tick
                    ClickType.EQUALS -> 523.25   // C5 clean ring
                }
                
                // Synthesize wave with exponential decay envelope
                val decayConst = when (type) {
                    ClickType.EQUALS -> 25.0
                    else -> 120.0
                }
                
                val systemVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() /
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
                
                val finalVolume = volume * systemVolume * 32767.0
                
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val envelope = exp(-t * decayConst)
                    val value = sin(2.0 * Math.PI * frequency * t) * envelope
                    buffer[i] = (value * finalVolume).toInt().coerceIn(-32768, 32767).toShort()
                }
                
                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    buffer.size * 2,
                    AudioTrack.MODE_STATIC
                )
                
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                
                Thread.sleep((durationSec * 1000).toLong() + 30)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun isEnabled(): Boolean = isEnabled

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0.0f, 1.0f)
    }

    fun getVolume(): Float = volume
}
