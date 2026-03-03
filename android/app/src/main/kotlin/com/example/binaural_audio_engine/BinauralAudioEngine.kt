package com.example.binaural_audio_engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import kotlin.math.*
import kotlin.random.Random

/**
 * BinauralAudioEngine - Real-time DSP synthesis engine for binaural beat generation
 *
 * Architecture:
 * - Voice 0: Dedicated binaural carrier (pure sine, left=baseFreq, right=baseFreq+beatFreq)
 * - Voices 1-3: Harmonic oscillator voices (harmonic partial stacks with slow LFO modulation)
 * - Voice 4: Harmonic shimmer layer (2x/3x root, slow amplitude breathing)
 * - Voices 5-7: Tonal chordal pad (harmonic stacks, ultra-slow LFO, ±2 cent detune)
 * - Chord progression: I → IV → vi → I (minimum 3 minutes per transition)
 * - CPU target: <15% via precomputed per-buffer coefficients
 */

// ============================================================
// HarmonicOscillatorVoice - Internal DSP voice class
// ============================================================
class HarmonicOscillatorVoice(
    var baseFrequency: Double,
    var amplitude: Double,
    lfoRateMin: Double = 0.002,
    lfoRateMax: Double = 0.01
) {
    var phase: Double = 0.0
    // harmonicCoefficients[0]=A1, [1]=A2, [2]=A3, [3]=A5
    val harmonicCoefficients: DoubleArray = doubleArrayOf(0.6, 0.2, 0.12, 0.06)
    // Base coefficients for drift reference
    private val baseCoefficients: DoubleArray = doubleArrayOf(0.6, 0.2, 0.12, 0.06)
    var lfoPhase: Double = Random.nextDouble() * 2.0 * PI
    var lfoRate: Double = lfoRateMin + Random.nextDouble() * (lfoRateMax - lfoRateMin)

    // Precomputed per-buffer values (set before inner sample loop)
    var phaseIncrement: Double = 0.0
    var lfoIncrement: Double = 0.0
    // Precomputed harmonic coefficients for this buffer (after drift)
    var bufA1: Double = 0.6
    var bufA2: Double = 0.2
    var bufA3: Double = 0.12
    var bufA5: Double = 0.06

    /**
     * Precompute per-buffer values to avoid per-sample allocations.
     * Call once per buffer before the sample loop.
     */
    fun precomputeBuffer(sampleRate: Int, detuneMultiplier: Double) {
        val actualFreq = baseFrequency * detuneMultiplier
        phaseIncrement = 2.0 * PI * actualFreq / sampleRate
        lfoIncrement = 2.0 * PI * lfoRate / sampleRate

        // Drift harmonic coefficients using current LFO phase
        // A2 drifts with sin(lfoPhase), A3 with sin(lfoPhase*0.8)
        val driftA2 = sin(lfoPhase) * 0.05
        val driftA3 = sin(lfoPhase * 0.8) * 0.04
        val driftA1 = -driftA2 * 0.5 - driftA3 * 0.3 // compensate to keep sum stable

        bufA1 = (baseCoefficients[0] + driftA1).coerceIn(0.4, 0.75)
        bufA2 = (baseCoefficients[1] + driftA2).coerceIn(0.08, 0.32)
        bufA3 = (baseCoefficients[2] + driftA3).coerceIn(0.04, 0.22)
        bufA5 = baseCoefficients[3] // A5 stays stable

        // Normalize so total <= 1.0
        val total = bufA1 + bufA2 + bufA3 + bufA5
        if (total > 1.0) {
            val inv = 1.0 / total
            bufA1 *= inv; bufA2 *= inv; bufA3 *= inv; bufA5 *= inv
        }
    }

    /**
     * Generate one sample using precomputed buffer values.
     * Advances phase and lfoPhase each call.
     */
    fun generateSample(): Double {
        val p = phase
        val sample = sin(p) * bufA1 +
                     sin(p * 2.0) * bufA2 +
                     sin(p * 3.0) * bufA3 +
                     sin(p * 5.0) * bufA5
        phase += phaseIncrement
        if (phase >= 2.0 * PI) phase -= 2.0 * PI
        lfoPhase += lfoIncrement
        if (lfoPhase >= 2.0 * PI) lfoPhase -= 2.0 * PI
        return sample * amplitude
    }
}

// ============================================================
// BinauralAudioEngine
// ============================================================
class BinauralAudioEngine(private val context: Context) {

    companion object {
        private const val TAG = "BinauralAudioEngine"
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_STEREO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_MULTIPLIER = 4
        private const val TWO_PI = 2.0 * PI
        private const val MIN_BASE_FREQ = 100.0
        private const val MAX_BASE_FREQ = 400.0
        private const val MIN_BEAT_FREQ = 0.5
        private const val MAX_BEAT_FREQ = 20.0
        private const val ATTACK_TIME_SEC = 10.0
        private const val RELEASE_TIME_SEC = 10.0
        private const val SOFT_CLIP_THRESHOLD = 0.95

        // Chord change timing (minimum 3 minutes)
        private const val CHORD_CHANGE_MIN = 180.0  // 3 minutes
        private const val CHORD_CHANGE_MAX = 360.0  // 6 minutes

        // Harmonic voice count (not counting binaural carrier)
        private const val HARMONIC_VOICE_COUNT = 3   // main harmonic voices
        private const val PAD_VOICE_COUNT = 3         // tonal pad voices
    }

    // ── Audio components ──────────────────────────────────────
    private var audioTrack: AudioTrack? = null
    private var audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // ── Audio thread ──────────────────────────────────────────
    private var audioThread: HandlerThread? = null
    private var audioHandler: Handler? = null
    private var isRunning = false

    // ── DSP parameters ────────────────────────────────────────
    private var baseFrequency = 250.0
    private var beatFrequency = 10.0
    private var targetBeatFrequency = 10.0
    private var currentGain = 0.0
    private var targetGain = 1.0

    // ── Binaural carrier phase accumulators (Voice 0) ─────────
    // Pure sine, phase-continuous, independent from harmonic voices
    private var binauralLeftPhase = 0.0
    private var binauralRightPhase = 0.0
    private val binauralCarrierAmplitude = 0.30  // fixed, not modulated

    // ── Brown noise ───────────────────────────────────────────
    private var brownNoiseEnabled = false
    private var brownNoiseLevel = 0.5
    private var brownNoiseState = 0.0
    private var brownNoiseLPFState = 0.0
    private var brownNoiseBreathingPhase = 0.0
    private var brownNoiseWarmth = 0.5
    private var brownNoiseTexture = 0.5

    // ── Ramping state ─────────────────────────────────────────
    private var isRamping = false
    private var rampStartFreq = 0.0
    private var rampTargetFreq = 0.0
    private var rampDuration = 30.0
    private var rampElapsed = 0.0
    private var isLinearRamp = true

    // ── Envelope state ────────────────────────────────────────
    private var envelopePhase = EnvelopePhase.IDLE
    private var envelopeTime = 0.0
    private var attackTime = ATTACK_TIME_SEC
    private var releaseTime = RELEASE_TIME_SEC

    // ── Performance monitoring ────────────────────────────────
    private var cpuUsagePercent = 0.0
    private var underrunCount = 0

    // ── Audio focus ───────────────────────────────────────────
    private var hasAudioFocus = false

    // ── Tonal system ──────────────────────────────────────────
    private var rootNote = 73.42   // D2
    private var tuningReference = 440.0
    private var useTuning432 = false

    // ── Harmonic field voices (Voices 1-3) ────────────────────
    // Each uses HarmonicOscillatorVoice with partial stacks
    private val harmonicVoices = Array(HARMONIC_VOICE_COUNT) {
        HarmonicOscillatorVoice(
            baseFrequency = 73.42,
            amplitude = 0.0,
            lfoRateMin = 0.002,
            lfoRateMax = 0.01
        )
    }
    // Stereo pan positions for harmonic voices
    private val harmonicVoicePan = doubleArrayOf(-0.3, 0.0, 0.3)
    // Detune offsets in cents (precomputed per buffer)
    private val harmonicDetuneMultipliers = DoubleArray(HARMONIC_VOICE_COUNT) { 1.0 }
    private val harmonicDetuneCents = doubleArrayOf(-0.5, 0.0, 0.5)

    // ── Harmonic shimmer layer (replaces subharmonic) ─────────
    // Frequency = rootNote * 2.0 or 3.0, very low amplitude, slow breathing
    private var shimmerPhase = 0.0
    private var shimmerBreathingPhase = 0.0
    private var shimmerAmplitude = 0.04
    private var shimmerFreqMultiplier = 2.0  // 2x or 3x root
    private var shimmerBreathingRate = 0.003 // Hz

    // ── Tonal chordal pad voices (Voices 5-7) ─────────────────
    // Warm ambient synth pad using harmonic oscillator voices
    private val padVoices = Array(PAD_VOICE_COUNT) {
        HarmonicOscillatorVoice(
            baseFrequency = 73.42,
            amplitude = 0.0,
            lfoRateMin = 0.001,
            lfoRateMax = 0.005
        )
    }
    private val padVoicePan = doubleArrayOf(-0.4, 0.0, 0.4)
    // ±2 cents detune for pad voices
    private val padDetuneCents = doubleArrayOf(-2.0, 0.0, 2.0)
    private val padDetuneMultipliers = DoubleArray(PAD_VOICE_COUNT) { 1.0 }
    private var padLayerEnabled = false
    private var padLayerLevel = 0.12

    // ── Chord progression (I → IV → vi → I) ──────────────────
    private var currentChordType = ChordType.ROOT
    private var targetChordType = ChordType.ROOT
    private var chordTransitionProgress = 0.0
    private var chordTransitionDuration = 30.0  // 30s equal-power crossfade
    private var timeSinceLastChordChange = 0.0
    private var nextChordChangeTime = 180.0
    private var isTransitioningChord = false

    // ── Generative controls (public API) ─────────────────────
    private var harmonicDensity = 0.7
    private var evolutionSpeed = 0.5
    private var stereoWidth = 0.5
    private var saturationAmount = 0.3
    private var padIntensity = 0.5
    private var harmonicRichness = 0.5
    private var driftIntensity = 0.5

    // ── Emotional mode ────────────────────────────────────────
    private var emotionalMode = EmotionalMode.CALM

    // ── Anti-fatigue / DC block ───────────────────────────────
    private var antiHarshness = true

    // ── Pad filter state (kept for emotional mode API) ────────
    private var padFilterBaseCutoff = 800.0

    // ── Legacy fields kept for API compatibility ──────────────
    private var smoothingEnabled = true
    private var brownNoiseBreathingState = 0.0

    // ── Precomputed per-buffer values ─────────────────────────
    // These are computed once per buffer, not per sample
    private var bufBinauralLeftInc = 0.0
    private var bufBinauralRightInc = 0.0
    private var bufShimmerInc = 0.0
    private var bufShimmerBreathInc = 0.0

    // ============================================================
    // Enums
    // ============================================================

    enum class ChordType {
        ROOT,      // I
        FOURTH,    // IV
        SIXTH,     // vi
        SUS2,      // Isus2
        SUS4       // Isus4
    }
    
    enum class EmotionalMode {
        SLEEP,  // Lower root, slower evolution, darker filter, reduced upper harmonics
        CALM,   // Balanced tonal field, gentle evolution, warm pad density
        FOCUS   // Slightly brighter, reduced complexity, stable tonal center
    }

    enum class EnvelopePhase {
        IDLE,
        ATTACK,
        SUSTAIN,
        RELEASE
    }

    // ============================================================
    // Initialization
    // ============================================================

    fun initialize(): Boolean {
        return try {
            val minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            if (minBufferSize == AudioTrack.ERROR || minBufferSize == AudioTrack.ERROR_BAD_VALUE) {
                Log.e(TAG, "Invalid buffer size: $minBufferSize")
                return false
            }
            val bufferSize = minBufferSize * BUFFER_SIZE_MULTIPLIER
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AUDIO_FORMAT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(CHANNEL_CONFIG)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            Log.i(TAG, "AudioTrack initialized: buffer=$bufferSize, sampleRate=$SAMPLE_RATE")
            initializeHarmonicVoices()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioTrack", e)
            false
        }
    }

    private fun initializeHarmonicVoices() {
        updateRootFrequency()
        updateAllVoiceFrequencies()

        // Harmonic field voices: chord-based frequencies, moderate amplitude
        val chordRatios = getChordRatios(currentChordType)
        for (i in 0 until HARMONIC_VOICE_COUNT) {
            harmonicVoices[i].baseFrequency = rootNote * chordRatios[i]
            harmonicVoices[i].amplitude = (0.10 + 0.08 * harmonicDensity)
            harmonicVoices[i].phase = Random.nextDouble() * TWO_PI
            harmonicDetuneMultipliers[i] = centsToMultiplier(harmonicDetuneCents[i])
        }

        // Shimmer layer: 2x root, very low amplitude
        shimmerPhase = Random.nextDouble() * TWO_PI
        shimmerBreathingPhase = Random.nextDouble() * TWO_PI
        shimmerFreqMultiplier = 2.0

        // Tonal pad voices: chord-based, ultra-slow LFO, ±2 cents detune
        for (i in 0 until PAD_VOICE_COUNT) {
            padVoices[i].baseFrequency = rootNote * chordRatios[i % chordRatios.size]
            padVoices[i].amplitude = padLayerLevel / PAD_VOICE_COUNT
            padVoices[i].phase = Random.nextDouble() * TWO_PI
            padDetuneMultipliers[i] = centsToMultiplier(padDetuneCents[i])
        }

        Log.i(TAG, "Initialized harmonic field engine. Root=${String.format("%.2f", rootNote)} Hz")
    }

    private fun centsToMultiplier(cents: Double): Double = 2.0.pow(cents / 1200.0)

    // ============================================================
    // Public API - Start / Stop (unchanged)
    // ============================================================

    fun start(): Boolean {
        if (isRunning) { Log.w(TAG, "Audio engine already running"); return true }

        // If a previous thread exists, fully stop it before starting fresh
        if (audioThread != null) {
            Log.i(TAG, "start(): cleaning up previous thread before restart")
            stopImmediate()
        }

        if (!requestAudioFocus()) { Log.e(TAG, "Failed to gain audio focus"); return false }

        return try {
            // Reinitialize AudioTrack if it was released or is in a bad state
            if (audioTrack == null || audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
                Log.i(TAG, "start(): AudioTrack not initialized, reinitializing...")
                if (!initialize()) {
                    Log.e(TAG, "start(): Failed to reinitialize AudioTrack")
                    return false
                }
            }

            // Reset envelope for clean ATTACK
            envelopePhase = EnvelopePhase.ATTACK
            envelopeTime = 0.0
            currentGain = 0.0

            // 1. Set isRunning = true BEFORE starting thread so generateAudioLoop() can run
            isRunning = true
            Log.i(TAG, "start(): isRunning=true")

            // 2. Call play() BEFORE starting the thread — AudioTrack must be in PLAYING state
            //    before the first write() in MODE_STREAM, otherwise write() may block indefinitely
            audioTrack?.play()
            Log.i(TAG, "start(): AudioTrack.play() called BEFORE thread start")

            // 3. Start the audio thread — generateAudioLoop() will see isRunning=true and
            //    AudioTrack already in PLAYING state, so write() will not block
            startAudioThread()
            Log.i(TAG, "start(): audio thread started")

            Log.i(TAG, "Audio engine started — isRunning=true, envelope=ATTACK")
            true
        } catch (e: Exception) {
            isRunning = false
            Log.e(TAG, "Failed to start audio engine", e)
            false
        }
    }

    fun stop() {
        if (!isRunning) return
        envelopePhase = EnvelopePhase.RELEASE
        envelopeTime = 0.0
        audioHandler?.postDelayed({ stopImmediate() }, (releaseTime * 1000).toLong())
    }

    private fun stopImmediate() {
        // 1. Signal the loop to exit FIRST
        isRunning = false
        Log.i(TAG, "stopImmediate(): isRunning set to false")

        // 2. Pause and flush AudioTrack to clear buffered data
        try { audioTrack?.pause() } catch (e: Exception) { Log.w(TAG, "pause() failed", e) }
        try { audioTrack?.flush() } catch (e: Exception) { Log.w(TAG, "flush() failed", e) }
        try { audioTrack?.stop() }  catch (e: Exception) { Log.w(TAG, "stop() failed",  e) }

        // 3. Quit the thread after AudioTrack is stopped
        audioThread?.quitSafely()
        audioThread = null
        audioHandler = null

        envelopePhase = EnvelopePhase.IDLE
        abandonAudioFocus()
        Log.i(TAG, "stopImmediate(): audio engine fully stopped")
    }

    // ============================================================
    // Public Setters (all preserved)
    // ============================================================

    fun setBaseFrequency(frequency: Double) {
        baseFrequency = frequency.coerceIn(MIN_BASE_FREQ, MAX_BASE_FREQ)
        Log.d(TAG, "Base frequency set to: $baseFrequency Hz")
    }

    fun setBeatFrequency(frequency: Double) {
        beatFrequency = frequency.coerceIn(MIN_BEAT_FREQ, MAX_BEAT_FREQ)
        targetBeatFrequency = beatFrequency
        Log.d(TAG, "Beat frequency set to: $beatFrequency Hz")
    }

    fun rampBeatFrequency(targetFreq: Double, durationSec: Double, linear: Boolean) {
        rampStartFreq = beatFrequency
        rampTargetFreq = targetFreq.coerceIn(MIN_BEAT_FREQ, MAX_BEAT_FREQ)
        rampDuration = durationSec.coerceIn(1.0, 120.0)
        rampElapsed = 0.0
        isLinearRamp = linear
        isRamping = true
        Log.i(TAG, "Starting ${if (linear) "linear" else "exponential"} ramp: $rampStartFreq -> $rampTargetFreq Hz over $rampDuration s")
    }

    fun setBrownNoiseEnabled(enabled: Boolean) {
        brownNoiseEnabled = enabled
        Log.d(TAG, "Brown noise ${if (enabled) "enabled" else "disabled"}")
    }

    fun setBrownNoiseLevel(level: Double) {
        brownNoiseLevel = level.coerceIn(0.0, 1.0)
        Log.d(TAG, "Brown noise level set to: ${brownNoiseLevel * 100}%")
    }

    fun setHarmonicRichness(richness: Double) {
        harmonicRichness = richness.coerceIn(0.0, 1.0)
        Log.d(TAG, "Harmonic richness set to: ${harmonicRichness * 100}%")
    }

    fun setDriftIntensity(intensity: Double) {
        driftIntensity = intensity.coerceIn(0.0, 1.0)
        Log.d(TAG, "Drift intensity set to: ${driftIntensity * 100}%")
    }

    fun setBrownNoiseWarmth(warmth: Double) {
        brownNoiseWarmth = warmth.coerceIn(0.0, 1.0)
        Log.d(TAG, "Brown noise warmth set to: ${brownNoiseWarmth * 100}%")
    }

    fun setBrownNoiseTexture(texture: Double) {
        brownNoiseTexture = texture.coerceIn(0.0, 1.0)
        Log.d(TAG, "Brown noise texture set to: ${brownNoiseTexture * 100}%")
    }

    fun setPadLayerEnabled(enabled: Boolean) {
        padLayerEnabled = enabled
        Log.d(TAG, "Tonal pad layer ${if (enabled) "enabled" else "disabled"}")
    }

    fun setPsychoacousticSmoothing(enabled: Boolean) {
        smoothingEnabled = enabled
        Log.d(TAG, "Psychoacoustic smoothing ${if (enabled) "enabled" else "disabled"}")
    }

    fun setRootNote(noteFrequency: Double) {
        rootNote = noteFrequency.coerceIn(50.0, 200.0)
        updateAllVoiceFrequencies()
        Log.d(TAG, "Root note set to: ${String.format("%.2f", rootNote)} Hz")
    }

    fun setTuningReference(use432: Boolean) {
        useTuning432 = use432
        tuningReference = if (use432) 432.0 else 440.0
        updateRootFrequency()
        updateAllVoiceFrequencies()
        Log.d(TAG, "Tuning reference set to: ${if (use432) "432Hz" else "440Hz"}")
    }

    fun setHarmonicDensity(density: Double) {
        harmonicDensity = density.coerceIn(0.0, 1.0)
        val amp = 0.10 + 0.08 * harmonicDensity
        for (v in harmonicVoices) v.amplitude = amp
        Log.d(TAG, "Harmonic density set to: ${harmonicDensity * 100}%")
    }

    fun setEvolutionSpeed(speed: Double) {
        evolutionSpeed = speed.coerceIn(0.0, 1.0)
        val speedMul = 0.5 + evolutionSpeed * 1.5
        for (v in harmonicVoices) {
            v.lfoRate = (0.002 + Random.nextDouble() * 0.008) * speedMul
        }
        Log.d(TAG, "Evolution speed set to: ${evolutionSpeed * 100}%")
    }

    fun setStereoWidth(width: Double) {
        stereoWidth = width.coerceIn(0.0, 1.0)
        Log.d(TAG, "Stereo width set to: ${stereoWidth * 100}%")
    }

    fun setSaturationAmount(amount: Double) {
        saturationAmount = amount.coerceIn(0.0, 1.0)
        Log.d(TAG, "Saturation amount set to: ${saturationAmount * 100}%")
    }

    fun setPadIntensity(intensity: Double) {
        padIntensity = intensity.coerceIn(0.0, 1.0)
        padLayerLevel = 0.05 + 0.12 * padIntensity
        val perVoiceAmp = padLayerLevel / PAD_VOICE_COUNT
        for (v in padVoices) v.amplitude = perVoiceAmp
        Log.d(TAG, "Pad intensity set to: ${padIntensity * 100}%")
    }

    fun setEmotionalMode(mode: Int) {
        emotionalMode = when (mode) {
            0 -> EmotionalMode.SLEEP
            1 -> EmotionalMode.CALM
            2 -> EmotionalMode.FOCUS
            else -> EmotionalMode.CALM
        }
        applyEmotionalMode()
        Log.d(TAG, "Emotional mode set to: $emotionalMode")
    }

    fun setAttackTime(time: Double) {
        attackTime = time.coerceIn(0.1, 60.0)
        Log.d(TAG, "Attack time set to: $attackTime s")
    }

    fun setReleaseTime(time: Double) {
        releaseTime = time.coerceIn(0.1, 60.0)
        Log.d(TAG, "Release time set to: $releaseTime s")
    }

    fun fadeIn() {
        envelopePhase = EnvelopePhase.ATTACK
        envelopeTime = 0.0
        Log.d(TAG, "Fading in")
    }

    fun fadeOut() {
        envelopePhase = EnvelopePhase.RELEASE
        envelopeTime = 0.0
        Log.d(TAG, "Fading out")
    }

    fun getCpuUsage(): Double = cpuUsagePercent

    fun getLatency(): Double =
        audioTrack?.let { it.bufferSizeInFrames.toDouble() / SAMPLE_RATE * 1000.0 } ?: 0.0

    fun getUnderrunCount(): Int = underrunCount

    fun getAudioFocusState(): Boolean = hasAudioFocus

    fun release() {
        stopImmediate()
        audioTrack?.release()
        audioTrack = null
        Log.i(TAG, "Audio engine released")
    }

    // ============================================================
    // Emotional Mode
    // ============================================================

    private fun applyEmotionalMode() {
        when (emotionalMode) {
            EmotionalMode.SLEEP -> {
                rootNote = 65.41  // C2
                nextChordChangeTime = CHORD_CHANGE_MAX
                evolutionSpeed = 0.3
                padFilterBaseCutoff = 600.0
                harmonicRichness = 0.3
                antiHarshness = true
                shimmerAmplitude = 0.02
            }
            EmotionalMode.CALM -> {
                rootNote = 73.42  // D2
                nextChordChangeTime = (CHORD_CHANGE_MIN + CHORD_CHANGE_MAX) / 2.0
                evolutionSpeed = 0.5
                padFilterBaseCutoff = 800.0
                harmonicRichness = 0.5
                shimmerAmplitude = 0.04
            }
            EmotionalMode.FOCUS -> {
                rootNote = 82.41  // E2
                nextChordChangeTime = CHORD_CHANGE_MAX
                evolutionSpeed = 0.4
                harmonicDensity = 0.5
                harmonicRichness = 0.4
                padFilterBaseCutoff = 1000.0
                shimmerAmplitude = 0.03
            }
        }
        updateRootFrequency()
        updateAllVoiceFrequencies()
    }

    // ============================================================
    // Tonal / Frequency helpers
    // ============================================================

    private fun updateRootFrequency() {
        val semitoneRatio = 2.0.pow(1.0 / 12.0)
        rootNote = tuningReference / semitoneRatio.pow(33.0)
        Log.d(TAG, "Root note updated to ${String.format("%.2f", rootNote)} Hz")
    }

    /**
     * Chord ratios for simplified I → IV → vi → I progression.
     * Returns 3 ratios (one per harmonic voice).
     */
    private fun getChordRatios(chord: ChordType): DoubleArray {
        return when (chord) {
            ChordType.ROOT   -> doubleArrayOf(1.0,   1.25,  1.5)   // I:  root, M3, P5
            ChordType.FOURTH -> doubleArrayOf(1.333, 1.667, 2.0)   // IV: P4, M6, octave
            ChordType.SIXTH  -> doubleArrayOf(1.667, 2.0,   2.5)   // vi: M6, octave, M10
            ChordType.SUS2   -> doubleArrayOf(1.0,   1.125, 1.5)   // Isus2: root, M2, P5
            ChordType.SUS4   -> doubleArrayOf(1.0,   1.333, 1.5)   // Isus4: root, P4, P5
        }
    }

    private fun updateAllVoiceFrequencies() {
        val chordRatios = getChordRatios(currentChordType)
        for (i in 0 until HARMONIC_VOICE_COUNT) {
            harmonicVoices[i].baseFrequency = rootNote * chordRatios[i]
        }
        // Shimmer: 2x root
        // (shimmerPhase is continuous; frequency updated per-buffer)
        // Pad voices: same chord ratios
        for (i in 0 until PAD_VOICE_COUNT) {
            padVoices[i].baseFrequency = rootNote * chordRatios[i % chordRatios.size]
        }
    }

    private fun interpolateRatio(from: Double, to: Double, progress: Double): Double {
        val p = sqrt(progress.coerceIn(0.0, 1.0))
        return from * (1.0 - p) + to * p
    }

    // ============================================================
    // Chord progression engine
    // ============================================================

    private fun updateHarmonicProgression(deltaTime: Double) {
        timeSinceLastChordChange += deltaTime

        if (!isTransitioningChord && timeSinceLastChordChange >= nextChordChangeTime) {
            targetChordType = getNextChord()
            isTransitioningChord = true
            chordTransitionProgress = 0.0
            timeSinceLastChordChange = 0.0
            nextChordChangeTime = CHORD_CHANGE_MIN +
                (CHORD_CHANGE_MAX - CHORD_CHANGE_MIN) * Random.nextDouble()
        }

        if (isTransitioningChord) {
            chordTransitionProgress += deltaTime / chordTransitionDuration
            if (chordTransitionProgress >= 1.0) {
                chordTransitionProgress = 1.0
                currentChordType = targetChordType
                isTransitioningChord = false
                updateAllVoiceFrequencies()
            } else {
                // Interpolate voice frequencies during crossfade
                val fromRatios = getChordRatios(currentChordType)
                val toRatios = getChordRatios(targetChordType)
                for (i in 0 until HARMONIC_VOICE_COUNT) {
                    harmonicVoices[i].baseFrequency = rootNote *
                        interpolateRatio(fromRatios[i], toRatios[i], chordTransitionProgress)
                }
                for (i in 0 until PAD_VOICE_COUNT) {
                    val idx = i % fromRatios.size
                    padVoices[i].baseFrequency = rootNote *
                        interpolateRatio(fromRatios[idx], toRatios[idx], chordTransitionProgress)
                }
            }
        }
    }

    /** Simplified I → IV → vi → I progression */
    private fun getNextChord(): ChordType = when (currentChordType) {
        ChordType.ROOT   -> ChordType.FOURTH
        ChordType.FOURTH -> ChordType.SIXTH
        ChordType.SIXTH  -> ChordType.ROOT
        ChordType.SUS2   -> ChordType.ROOT
        ChordType.SUS4   -> ChordType.ROOT
    }

    // ============================================================
    // Envelope
    // ============================================================

    private fun updateEnvelope(deltaTime: Double) {
        when (envelopePhase) {
            EnvelopePhase.ATTACK -> {
                envelopeTime += deltaTime
                currentGain = (envelopeTime / attackTime).coerceIn(0.0, 1.0)
                if (envelopeTime >= attackTime) { envelopePhase = EnvelopePhase.SUSTAIN; currentGain = 1.0 }
            }
            EnvelopePhase.SUSTAIN -> currentGain = 1.0
            EnvelopePhase.RELEASE -> {
                envelopeTime += deltaTime
                currentGain = 1.0 - (envelopeTime / releaseTime).coerceIn(0.0, 1.0)
                if (envelopeTime >= releaseTime) { envelopePhase = EnvelopePhase.IDLE; currentGain = 0.0 }
            }
            EnvelopePhase.IDLE -> currentGain = 0.0
        }
    }

    // ============================================================
    // Ramping
    // ============================================================

    private fun updateRamping(deltaTime: Double) {
        rampElapsed += deltaTime
        if (rampElapsed >= rampDuration) {
            beatFrequency = rampTargetFreq
            isRamping = false
            Log.i(TAG, "Ramp completed at $beatFrequency Hz")
            return
        }
        val progress = rampElapsed / rampDuration
        beatFrequency = if (isLinearRamp) {
            // Linear interpolation
            rampStartFreq + (rampTargetFreq - rampStartFreq) * progress
        } else {
            // Exponential interpolation
            rampStartFreq * exp(ln(rampTargetFreq / rampStartFreq) * progress)
        }
    }

    // ============================================================
    // Brown noise (unchanged logic, kept for API)
    // ============================================================

    private fun generateEnhancedBrownNoise(deltaTime: Double): Double {
        val white = (Random.nextDouble() * 2.0 - 1.0) * 0.1 * brownNoiseTexture
        brownNoiseState = (brownNoiseState + white).coerceIn(-1.0, 1.0)
        val lpfCoeff = 0.98 - 0.1 * brownNoiseWarmth
        brownNoiseLPFState = brownNoiseLPFState * lpfCoeff + brownNoiseState * (1.0 - lpfCoeff)
        brownNoiseBreathingPhase += TWO_PI * 0.05 * deltaTime
        if (brownNoiseBreathingPhase >= TWO_PI) brownNoiseBreathingPhase -= TWO_PI
        val breathing = 1.0 + sin(brownNoiseBreathingPhase) * 0.1
        val warmthFactor = 1.0 - 0.2 * brownNoiseWarmth
        return brownNoiseLPFState * breathing * warmthFactor
    }

    // ============================================================
    // Anti-fatigue / DC block
    // ============================================================

    private fun applyAntiFatigueL(sample: Double): Double {
        var s = sample

        // Soft clipping only
        if (abs(s) > SOFT_CLIP_THRESHOLD) {
            val sign = if (s > 0) 1.0 else -1.0
            val excess = abs(s) - SOFT_CLIP_THRESHOLD
            s = sign * (SOFT_CLIP_THRESHOLD + tanh(excess * 3.0) * (1.0 - SOFT_CLIP_THRESHOLD))
        }

        if (antiHarshness) s *= 0.98

        return s.coerceIn(-1.0, 1.0)
    }

    private fun applyAntiFatigueR(sample: Double): Double {
        var s = sample

        // Soft clipping only
        if (abs(s) > SOFT_CLIP_THRESHOLD) {
            val sign = if (s > 0) 1.0 else -1.0
            val excess = abs(s) - SOFT_CLIP_THRESHOLD
            s = sign * (SOFT_CLIP_THRESHOLD + tanh(excess * 3.0) * (1.0 - SOFT_CLIP_THRESHOLD))
        }

        if (antiHarshness) s *= 0.98

        return s.coerceIn(-1.0, 1.0)
    }

    // ============================================================
    // Audio focus
    // ============================================================

    private fun requestAudioFocus(): Boolean {
        val result = audioManager.requestAudioFocus(
            null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
        )
        hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        return hasAudioFocus
    }

    private fun abandonAudioFocus() {
        audioManager.abandonAudioFocus(null)
        hasAudioFocus = false
    }

    // ============================================================
    // Audio thread
    // ============================================================

    private fun startAudioThread() {
        // Always create a NEW HandlerThread — HandlerThread cannot be restarted after quitSafely()
        val newThread = HandlerThread("AudioGenerationThread", Process.THREAD_PRIORITY_AUDIO)
        newThread.start()
        audioThread = newThread
        audioHandler = Handler(newThread.looper)
        audioHandler?.post { generateAudioLoop() }
        Log.i(TAG, "startAudioThread(): new HandlerThread created and started")
    }

    // ============================================================
    // Main audio generation loop
    // ============================================================

    private fun generateAudioLoop() {
        Log.i(TAG, "generateAudioLoop(): loop entered, isRunning=$isRunning")

        val bufferSize = 1024
        val buffer = ShortArray(bufferSize * 2)  // stereo
        val deltaTime = bufferSize.toDouble() / SAMPLE_RATE
        val sampleDelta = 1.0 / SAMPLE_RATE
        val bufferDurationNanos = (deltaTime * 1_000_000_000).toLong()
        var processingTimeAccumulator = 0L
        var bufferCount = 0
        var firstBufferWritten = false
        var zeroGainAccumulator = 0.0  // tracks how long currentGain has been 0

        while (isRunning) {
            val bufferStartTime = System.nanoTime()

            // ── Per-buffer updates (not per-sample) ──────────────
            if (isRamping) updateRamping(deltaTime)
            updateEnvelope(deltaTime)
            updateHarmonicProgression(deltaTime)

            // Debug: warn if gain stays at 0 for more than 1 second
            if (currentGain <= 0.0001) {
                zeroGainAccumulator += deltaTime
                if (zeroGainAccumulator > 1.0) {
                    Log.w(TAG, "generateAudioLoop(): currentGain has been ~0 for ${String.format("%.1f", zeroGainAccumulator)}s — envelopePhase=$envelopePhase")
                }
            } else {
                zeroGainAccumulator = 0.0
            }

            // Precompute binaural carrier increments
            bufBinauralLeftInc  = TWO_PI * baseFrequency / SAMPLE_RATE
            bufBinauralRightInc = TWO_PI * (baseFrequency + beatFrequency) / SAMPLE_RATE

            // Precompute shimmer increments
            val shimmerFreq = rootNote * shimmerFreqMultiplier
            bufShimmerInc      = TWO_PI * shimmerFreq / SAMPLE_RATE
            bufShimmerBreathInc = TWO_PI * shimmerBreathingRate / SAMPLE_RATE

            // Precompute harmonic voice buffers (drift coefficients once per buffer)
            for (i in 0 until HARMONIC_VOICE_COUNT) {
                harmonicVoices[i].precomputeBuffer(SAMPLE_RATE, harmonicDetuneMultipliers[i])
            }

            // Precompute pad voice buffers
            for (i in 0 until PAD_VOICE_COUNT) {
                padVoices[i].precomputeBuffer(SAMPLE_RATE, padDetuneMultipliers[i])
            }

            // Precompute stereo pan gains (equal-power)
            val hLeftGains  = DoubleArray(HARMONIC_VOICE_COUNT)
            val hRightGains = DoubleArray(HARMONIC_VOICE_COUNT)
            for (i in 0 until HARMONIC_VOICE_COUNT) {
                val pan = harmonicVoicePan[i] * stereoWidth
                hLeftGains[i]  = sqrt((1.0 - pan) * 0.5)
                hRightGains[i] = sqrt((1.0 + pan) * 0.5)
            }
            val pLeftGains  = DoubleArray(PAD_VOICE_COUNT)
            val pRightGains = DoubleArray(PAD_VOICE_COUNT)
            for (i in 0 until PAD_VOICE_COUNT) {
                val pan = padVoicePan[i] * stereoWidth
                pLeftGains[i]  = sqrt((1.0 - pan) * 0.5)
                pRightGains[i] = sqrt((1.0 + pan) * 0.5)
            }

            // ── Per-sample loop ───────────────────────────────────
            for (i in 0 until bufferSize) {

                // ── RAW 440Hz TEST TONE (diagnostic) ──
                val testFreq = 440.0
                val sampleValue = sin(2.0 * Math.PI * testFreq * i / SAMPLE_RATE)
                val leftSample = sampleValue * 0.4
                val rightSample = sampleValue * 0.4

                buffer[i * 2]     = (leftSample  * 32767).toInt().toShort()
                buffer[i * 2 + 1] = (rightSample * 32767).toInt().toShort()
            }

            // Measure processing time
            val bufferEndTime = System.nanoTime()
            processingTimeAccumulator += bufferEndTime - bufferStartTime
            bufferCount++

            // Write to AudioTrack
            val written = audioTrack?.write(buffer, 0, buffer.size) ?: -1
            if (written < 0) {
                Log.e(TAG, "generateAudioLoop(): AudioTrack.write() returned error $written — stopping loop")
                break
            }

            // Log first buffer write to confirm loop is active
            if (!firstBufferWritten) {
                firstBufferWritten = true
                Log.i(TAG, "generateAudioLoop(): first buffer written to AudioTrack — loop confirmed active")
            }

            // Update CPU usage every 10 buffers (~0.23s)
            if (bufferCount >= 10) {
                val totalBufferDuration = bufferDurationNanos * bufferCount
                cpuUsagePercent = (processingTimeAccumulator.toDouble() /
                    totalBufferDuration.toDouble() * 100.0).coerceIn(0.0, 100.0)
                processingTimeAccumulator = 0L
                bufferCount = 0
            }
        }
        Log.i(TAG, "generateAudioLoop(): loop exited — isRunning=$isRunning")
    }
}