package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.example.R

object SoundHelper {
    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0
    private var isLoaded: Boolean = false

    fun init(context: Context) {
        if (soundPool != null) return
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(audioAttributes)
                .build().apply {
                    setOnLoadCompleteListener { _, sampleId, status ->
                        if (status == 0) {
                            clickSoundId = sampleId
                            isLoaded = true
                        }
                    }
                    clickSoundId = load(context.applicationContext, R.raw.click_sound, 1)
                }
        } catch (e: Exception) {
            // Graceful fallback
        }
    }

    fun playClickSound(context: Context) {
        try {
            if (soundPool == null || clickSoundId == 0) {
                init(context)
            }
            val sp = soundPool
            if (sp != null && isLoaded && clickSoundId != 0) {
                sp.play(clickSoundId, 0.85f, 0.85f, 1, 0, 1.0f)
            } else {
                // Immediate fallback to system AudioManager click effect while loading
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.85f)
            }
        } catch (e: Exception) {
            // Silently ignore audio playback errors
        }
    }
}
