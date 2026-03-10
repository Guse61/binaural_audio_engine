package com.example.binaural_audio_engine

import android.content.Context
import android.media.MediaPlayer

class NatureSoundManager(private val context: Context) {

    private var player: MediaPlayer? = null
    private var currentSound: NatureSound = NatureSound.NONE

    fun play(sound: NatureSound) {
        if (sound == currentSound) return

        stop()

        val resId = when (sound) {
            NatureSound.WIND_CHIMES -> R.raw.wind_chimes
            NatureSound.FOREST_BIRDSONG -> R.raw.forest_birds
            NatureSound.RIVER_STREAM -> R.raw.river_stream
            NatureSound.BEACH_WAVES -> R.raw.beach_waves
            NatureSound.LIGHT_RAIN -> R.raw.light_rain
            NatureSound.NONE -> null
        }

        if (resId != null) {
            player = MediaPlayer.create(context, resId)
            player?.isLooping = true
            player?.setVolume(0.35f, 0.35f)
            player?.start()
        }

        currentSound = sound
    }

    fun setVolume(volume: Float) {
        player?.setVolume(volume, volume)
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
        currentSound = NatureSound.NONE
    }
}
