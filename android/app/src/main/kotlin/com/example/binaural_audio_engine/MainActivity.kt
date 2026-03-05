package com.example.binaural_audio_engine

import android.os.Bundle
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

/**
 * MainActivity - Platform channel bridge for BinauralAudioEngine
 * 
 * Exposes native audio engine methods to Flutter via MethodChannel:
 * - initialize: Initialize audio engine
 * - start: Start audio playback with fade-in
 * - stop: Stop audio playback with fade-out
 * - setBaseFrequency: Set base frequency (100-400 Hz)
 * - setBeatFrequency: Set beat frequency (0.5-20 Hz)
 * - rampBeatFrequency: Ramp beat frequency smoothly
 * - setBrownNoiseEnabled: Enable/disable brown noise
 * - setBrownNoiseLevel: Set brown noise mix level (0-100%)
 * - setAttackTime: Set envelope attack time
 * - setReleaseTime: Set envelope release time
 * - fadeIn: Fade in audio
 * - fadeOut: Fade out audio
 * - getCpuUsage: Get current CPU usage
 * - getLatency: Get current latency
 * - getUnderrunCount: Get audio underrun count
 * - hasAudioFocus: Check audio focus status
 */
class MainActivity: FlutterFragmentActivity() {
    
    private val CHANNEL = "com.example.binaural_audio_engine/audio"
    private var audioEngine: BinauralAudioEngine? = null
    
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        // Initialize audio engine
        audioEngine = BinauralAudioEngine(applicationContext)
        
        // Setup method channel
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "initialize" -> {
                    val success = audioEngine?.initialize() ?: false
                    result.success(success)
                }
                
                "start" -> {
                    val success = audioEngine?.start() ?: false
                    result.success(success)
                }
                
                "stop" -> {
                    audioEngine?.stop()
                    result.success(true)
                }
                
                "setBaseFrequency" -> {
                    val frequency = call.argument<Double>("frequency") ?: 250.0
                    audioEngine?.setBaseFrequency(frequency)
                    result.success(true)
                }
                
                "setBeatFrequency" -> {
                    val frequency = call.argument<Double>("frequency") ?: 10.0
                    audioEngine?.setBeatFrequency(frequency)
                    result.success(true)
                }
                
                "rampBeatFrequency" -> {
                    val targetFreq = call.argument<Double>("targetFrequency") ?: 10.0
                    val duration = call.argument<Double>("duration") ?: 30.0
                    val linear = call.argument<Boolean>("linear") ?: true
                    audioEngine?.rampBeatFrequency(targetFreq, duration, linear)
                    result.success(true)
                }
                
                "setBrownNoiseEnabled" -> {
                    val enabled = call.argument<Boolean>("enabled") ?: false
                    audioEngine?.setBrownNoiseEnabled(enabled)
                    result.success(true)
                }
                
                "setBrownNoiseLevel" -> {
                    val level = call.argument<Double>("level") ?: 0.5
                    audioEngine?.setBrownNoiseLevel(level)
                    result.success(true)
                }
                
                "setAttackTime" -> {
                    val time = call.argument<Double>("time") ?: 10.0
                    audioEngine?.setAttackTime(time)
                    result.success(true)
                }
                
                "setReleaseTime" -> {
                    val time = call.argument<Double>("time") ?: 10.0
                    audioEngine?.setReleaseTime(time)
                    result.success(true)
                }
                
                "fadeIn" -> {
                    audioEngine?.fadeIn()
                    result.success(true)
                }
                
                "fadeOut" -> {
                    audioEngine?.fadeOut()
                    result.success(true)
                }
                
                "getCpuUsage" -> {
                    val cpuUsage = audioEngine?.getCpuUsage() ?: 0.0
                    result.success(cpuUsage)
                }
                
                "getLatency" -> {
                    val latency = audioEngine?.getLatency() ?: 0.0
                    result.success(latency)
                }
                
                "getUnderrunCount" -> {
                    val count = audioEngine?.getUnderrunCount() ?: 0
                    result.success(count)
                }
                
                "hasAudioFocus" -> {
                    val hasFocus = audioEngine?.getAudioFocusState() ?: false
                    result.success(hasFocus)
                }
                
                // ========== PHASE 2: Premium Sound Design Controls ==========
                
                "setHarmonicRichness" -> {
                    val richness = call.argument<Double>("richness") ?: 0.5
                    audioEngine?.setHarmonicRichness(richness)
                    result.success(true)
                }
                
                "setDriftIntensity" -> {
                    val intensity = call.argument<Double>("intensity") ?: 0.5
                    audioEngine?.setDriftIntensity(intensity)
                    result.success(true)
                }
                
                "setBrownNoiseWarmth" -> {
                    val warmth = call.argument<Double>("warmth") ?: 0.5
                    audioEngine?.setBrownNoiseWarmth(warmth)
                    result.success(true)
                }
                
                "setBrownNoiseTexture" -> {
                    val texture = call.argument<Double>("texture") ?: 0.5
                    audioEngine?.setBrownNoiseTexture(texture)
                    result.success(true)
                }
                
                "setPadLayerEnabled" -> {
                    val enabled = call.argument<Boolean>("enabled") ?: false
                    audioEngine?.setPadLayerEnabled(enabled)
                    result.success(true)
                }
                
                "setPsychoacousticSmoothing" -> {
                    val enabled = call.argument<Boolean>("enabled") ?: true
                    audioEngine?.setPsychoacousticSmoothing(enabled)
                    result.success(true)
                }
                
                // ========== PHASE 3: Generative Harmonic Ambient Engine Controls ==========
                
                "setRootNote" -> {
                    val noteFrequency = call.argument<Double>("noteFrequency") ?: 73.42
                    audioEngine?.setRootNote(noteFrequency)
                    result.success(true)
                }
                
                "setTuningReference" -> {
                    val use432 = call.argument<Boolean>("use432") ?: false
                    audioEngine?.setTuningReference(use432)
                    result.success(true)
                }
                
                "setHarmonicDensity" -> {
                    val density = call.argument<Double>("density") ?: 0.7
                    audioEngine?.setHarmonicDensity(density)
                    result.success(true)
                }
                
                "setEvolutionSpeed" -> {
                    val speed = call.argument<Double>("speed") ?: 0.5
                    audioEngine?.setEvolutionSpeed(speed)
                    result.success(true)
                }
                
                "setStereoWidth" -> {
                    val width = call.argument<Double>("width") ?: 0.5
                    audioEngine?.setStereoWidth(width)
                    result.success(true)
                }
                
                "setSaturationAmount" -> {
                    val amount = call.argument<Double>("amount") ?: 0.3
                    audioEngine?.setSaturationAmount(amount)
                    result.success(true)
                }
                
                "setPadIntensity" -> {
                    val intensity = call.argument<Double>("intensity") ?: 0.5
                    audioEngine?.setPadIntensity(intensity)
                    result.success(true)
                }
                
                "setEmotionalMode" -> {
                    val mode = call.argument<Int>("mode") ?: 1
                    audioEngine?.setEmotionalMode(mode)
                    result.success(true)
                }
                
                else -> {
                    result.notImplemented()
                }
            }
        }
    }
    
    override fun onDestroy() {
        audioEngine?.release()
        audioEngine = null
        super.onDestroy()
    }
}
