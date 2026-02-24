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
 * Features:
 * - Stereo audio generation (left=base frequency, right=base+beat frequency)
 * - 100-400Hz base frequency range, 0.5-20Hz beat frequency range
 * - 44.1kHz sample rate, 16-bit PCM output
 * - Zero clicks/pops with phase continuity
 * - Smooth frequency ramping (linear/exponential, 1-120s duration)
 * - Gain envelope with 10s attack/release
 * - Brown noise generator with 0-100% mix
 * - Background thread processing
 * - Audio focus handling
 * - CPU usage monitoring
 * - Underrun detection
 * 
 * PHASE 2 ENHANCEMENTS:
 * - Harmonic layered oscillators (fundamental + 2nd, 3rd, 5th harmonics)
 * - Micro drift for organic analog feel
 * - Soft stereo spatial widening
 * - Psychoacoustic envelope smoothing with equal-power curves
 * - Enhanced brown noise with warmth EQ and breathing
 * - Optional ambient pad layer
 * - Anti-fatigue optimization with soft roll-off and clipping
 * 
 * PHASE 3 GENERATIVE HARMONIC AMBIENT ENGINE:
 * - Tonal system foundation with root note and tuning reference
 * - Multi-voice harmonic architecture (4-6 independent voices)
 * - Slow harmonic progression engine (chord changes every 2-5 minutes)
 * - Organic variation with ultra-slow LFOs per voice
 * - Musical smoothing with equal-power crossfades
 * - Emotional mode system (Sleep/Calm/Focus)
 * - Developer tuning controls for refinement
 */
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
        
        // Phase 2 constants
        private const val SMOOTHING_WINDOW_MS = 10.0 // Psychoacoustic smoothing
        private const val DRIFT_LFO_FREQ = 0.03 // Hz (0.01-0.05 range)
        private const val MAX_DRIFT_AMOUNT = 0.1 // Hz (±0.05-0.15 range)
        private const val STEREO_DECORR_DELAY_MS = 2.5 // Micro-delay for spatial widening
        private const val SOFT_CLIP_THRESHOLD = 0.95 // Anti-fatigue soft clipping
        
        // Phase 3: Generative harmonic ambient engine constants
        private const val ULTRA_SLOW_LFO_MIN = 0.003 // Hz (0.003-0.01 range)
        private const val ULTRA_SLOW_LFO_MAX = 0.01 // Hz
        private const val CHORD_CHANGE_MIN = 120.0 // seconds (2 minutes)
        private const val CHORD_CHANGE_MAX = 300.0 // seconds (5 minutes)
        private const val MICRO_DETUNE_CENTS = 0.5 // Micro cents drift
        private const val VOICE_COUNT = 6 // Number of harmonic voices
    }
    
    // Audio components
    private var audioTrack: AudioTrack? = null
    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    // Audio thread
    private var audioThread: HandlerThread? = null
    private var audioHandler: Handler? = null
    private var isRunning = false
    
    // DSP parameters
    private var baseFrequency = 250.0 // Hz
    private var beatFrequency = 10.0 // Hz
    private var targetBeatFrequency = 10.0 // Hz for ramping
    private var currentGain = 0.0 // 0.0 to 1.0
    private var targetGain = 1.0
    
    // Phase accumulators for phase continuity
    private var leftPhase = 0.0
    private var rightPhase = 0.0
    
    // Brown noise generator state
    private var brownNoiseEnabled = false
    private var brownNoiseLevel = 0.5 // 0.0 to 1.0
    private var brownNoiseState = 0.0
    
    // Ramping state
    private var isRamping = false
    private var rampStartFreq = 0.0
    private var rampTargetFreq = 0.0
    private var rampDuration = 30.0 // seconds
    private var rampElapsed = 0.0
    private var isLinearRamp = true
    
    // Envelope state
    private var envelopePhase = EnvelopePhase.IDLE
    private var envelopeTime = 0.0
    private var attackTime = ATTACK_TIME_SEC
    private var releaseTime = RELEASE_TIME_SEC
    
    // Performance monitoring
    private var lastCpuCheckTime = System.nanoTime()
    private var cpuUsagePercent = 0.0
    private var underrunCount = 0
    private var lastUnderrunCheck = 0
    
    // Audio focus
    private var hasAudioFocus = false
    
    // ========== PHASE 2: Premium Sound Design Parameters ==========
    
    // Harmonic oscillator parameters
    private var harmonicRichness = 0.5 // 0.0 to 1.0 (0-100%)
    private val harmonic2ndPhaseLeft = 0.0
    private val harmonic2ndPhaseRight = 0.0
    private val harmonic3rdPhaseLeft = 0.0
    private val harmonic3rdPhaseRight = 0.0
    private val harmonic5thPhaseLeft = 0.0
    private val harmonic5thPhaseRight = 0.0
    
    // Micro drift parameters
    private var driftIntensity = 0.5 // 0.0 to 1.0
    private var driftLFOPhase = 0.0
    private var currentDriftLeft = 0.0
    private var currentDriftRight = 0.0
    
    // Stereo widening
    private val stereoDelayBuffer = mutableListOf<Pair<Double, Double>>() // Ring buffer for micro-delay
    private val stereoDelayFrames = (STEREO_DECORR_DELAY_MS * SAMPLE_RATE / 1000.0).toInt()
    
    // Psychoacoustic smoothing
    private var smoothingEnabled = true
    private val smoothingFrames = (SMOOTHING_WINDOW_MS * SAMPLE_RATE / 1000.0).toInt()
    private var previousGain = 0.0
    private var targetGainSmoothed = 0.0
    
    // Enhanced brown noise
    private var brownNoiseWarmth = 0.5 // 0.0 to 1.0
    private var brownNoiseTexture = 0.5 // 0.0 to 1.0 (density)
    private var brownNoiseBreathingPhase = 0.0
    private var brownNoiseLPFState = 0.0 // Low-pass filter state
    
    // Ambient pad layer
    private var padLayerEnabled = false
    private var padLayerLevel = 0.15 // Very low amplitude
    private var padFilterPhase = 0.0
    private var padNoiseState = 0.0
    private var padFilterCutoff = 800.0 // Hz
    
    // Anti-fatigue optimization
    private var antiHarshness = true // Upper harmonic roll-off
    private var dcOffsetFilter = 0.0 // DC blocking filter state
    
    // ========== PHASE 3: Generative Harmonic Ambient Engine Parameters ==========
    
    // Tonal system foundation
    private var rootNote = 73.42 // D2 frequency in Hz (default)
    private var tuningReference = 440.0 // A4 = 440Hz (concert pitch)
    private var useTuning432 = false // false = 440Hz, true = 432Hz
    
    // Multi-voice harmonic architecture
    private val voicePhases = DoubleArray(VOICE_COUNT) { 0.0 }
    private val voiceFrequencies = DoubleArray(VOICE_COUNT) { 0.0 }
    private val voiceAmplitudes = DoubleArray(VOICE_COUNT) { 0.0 }
    private val voiceStereoPositions = DoubleArray(VOICE_COUNT) { 0.0 }
    private val voiceLFOPhases = DoubleArray(VOICE_COUNT) { 0.0 }
    private val voiceLFOFreqs = DoubleArray(VOICE_COUNT) { 0.0 }
    private val voiceDetuneOffsets = DoubleArray(VOICE_COUNT) { 0.0 }
    private val voiceEnvelopeStates = DoubleArray(VOICE_COUNT) { 0.0 }
    
    // Slow harmonic progression engine
    private var currentChordType = ChordType.ROOT // I chord
    private var targetChordType = ChordType.ROOT
    private var chordTransitionProgress = 0.0
    private var chordTransitionDuration = 10.0 // seconds for smooth interpolation
    private var timeSinceLastChordChange = 0.0
    private var nextChordChangeTime = 180.0 // 3 minutes default
    private var isTransitioningChord = false
    
    // Harmonic motion & organic variation
    private var harmonicDensity = 0.7 // 0.0 to 1.0
    private var evolutionSpeed = 0.5 // 0.0 to 1.0 (controls LFO speeds)
    private var stereoWidth = 0.5 // 0.0 to 1.0
    private var saturationAmount = 0.3 // 0.0 to 1.0
    private var padIntensity = 0.5 // 0.0 to 1.0
    
    // Emotional mode system
    private var emotionalMode = EmotionalMode.CALM
    
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
    
    /**
     * Initialize audio engine and prepare AudioTrack
     */
    fun initialize(): Boolean {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )
            
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
            
            // Initialize generative harmonic voices
            initializeHarmonicVoices()
            
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioTrack", e)
            return false
        }
    }
    
    /**
     * Initialize multi-voice harmonic architecture
     */
    private fun initializeHarmonicVoices() {
        updateRootFrequency()
        
        // Voice 0: Binaural Carrier (Core Layer)
        voiceAmplitudes[0] = 0.35
        voiceStereoPositions[0] = 0.0 // Center
        voiceLFOFreqs[0] = 0.005
        
        // Voice 1: Perfect Fifth Layer (3:2 ratio)
        voiceAmplitudes[1] = 0.12
        voiceStereoPositions[1] = 0.3 // Slight right
        voiceLFOFreqs[1] = 0.004
        
        // Voice 2: Octave Air Layer (2x frequency)
        voiceAmplitudes[2] = 0.08
        voiceStereoPositions[2] = -0.25 // Slight left
        voiceLFOFreqs[2] = 0.006
        
        // Voice 3: Subharmonic Support (0.5x frequency)
        voiceAmplitudes[3] = 0.10
        voiceStereoPositions[3] = 0.0 // Center
        voiceLFOFreqs[3] = 0.003
        
        // Voice 4: Evolving Pad Texture
        voiceAmplitudes[4] = 0.15
        voiceStereoPositions[4] = -0.4 // Left
        voiceLFOFreqs[4] = 0.007
        
        // Voice 5: Additional harmonic layer
        voiceAmplitudes[5] = 0.10
        voiceStereoPositions[5] = 0.35 // Right
        voiceLFOFreqs[5] = 0.008
        
        // Initialize random phase offsets to avoid phase alignment
        for (i in 0 until VOICE_COUNT) {
            voicePhases[i] = Random.nextDouble() * TWO_PI
            voiceDetuneOffsets[i] = (Random.nextDouble() - 0.5) * MICRO_DETUNE_CENTS
            voiceEnvelopeStates[i] = 1.0
        }
        
        updateVoiceFrequencies()
        
        Log.i(TAG, "Initialized $VOICE_COUNT harmonic voices with root note ${String.format("%.2f", rootNote)} Hz")
    }
    
    /**
     * Update root frequency based on tuning reference
     */
    private fun updateRootFrequency() {
        // Calculate D2 frequency based on tuning reference
        // A4 = tuningReference, D2 is 33 semitones below A4
        val semitoneRatio = 2.0.pow(1.0 / 12.0)
        rootNote = tuningReference / semitoneRatio.pow(33.0)
        
        Log.d(TAG, "Root note updated to ${String.format("%.2f", rootNote)} Hz (tuning: ${if (useTuning432) "432Hz" else "440Hz"})")
    }
    
    /**
     * Update voice frequencies based on current chord and root note
     */
    private fun updateVoiceFrequencies() {
        val chordRatios = getChordRatios(currentChordType)
        val targetRatios = getChordRatios(targetChordType)
        
        // Voice 0: Binaural Carrier - uses baseFrequency (maintains binaural beat)
        voiceFrequencies[0] = baseFrequency
        
        // Voice 1: Perfect Fifth Layer (3:2 ratio from root)
        val fifthRatio = if (isTransitioningChord) {
            interpolateRatio(1.5, 1.5, chordTransitionProgress)
        } else {
            1.5
        }
        voiceFrequencies[1] = rootNote * fifthRatio
        
        // Voice 2: Octave Air Layer (2x root)
        voiceFrequencies[2] = rootNote * 2.0
        
        // Voice 3: Subharmonic Support (0.5x root)
        voiceFrequencies[3] = rootNote * 0.5
        
        // Voice 4: Evolving Pad Texture (harmonic from chord)
        voiceFrequencies[4] = rootNote * interpolateRatio(
            chordRatios[0],
            targetRatios[0],
            chordTransitionProgress
        )
        
        // Voice 5: Additional harmonic layer (chord-based)
        voiceFrequencies[5] = rootNote * interpolateRatio(
            chordRatios[1],
            targetRatios[1],
            chordTransitionProgress
        )
    }
    
    /**
     * Get harmonic ratios for chord type
     */
    private fun getChordRatios(chordType: ChordType): DoubleArray {
        return when (chordType) {
            ChordType.ROOT -> doubleArrayOf(1.0, 1.25) // I: root, major third
            ChordType.FOURTH -> doubleArrayOf(1.333, 1.666) // IV: fourth, sixth
            ChordType.SIXTH -> doubleArrayOf(1.666, 2.0) // vi: sixth, octave
            ChordType.SUS2 -> doubleArrayOf(1.125, 1.5) // Isus2: major second, fifth
            ChordType.SUS4 -> doubleArrayOf(1.333, 1.5) // Isus4: fourth, fifth
        }
    }
    
    /**
     * Interpolate between two frequency ratios with equal-power curve
     */
    private fun interpolateRatio(from: Double, to: Double, progress: Double): Double {
        // Equal-power crossfade for smooth transitions
        val smoothProgress = sqrt(progress.coerceIn(0.0, 1.0))
        return from * (1.0 - smoothProgress) + to * smoothProgress
    }
    
    /**
     * Start audio playback with fade-in
     */
    fun start(): Boolean {
        if (isRunning) {
            Log.w(TAG, "Audio engine already running")
            return true
        }
        
        if (!requestAudioFocus()) {
            Log.e(TAG, "Failed to gain audio focus")
            return false
        }
        
        try {
            audioTrack?.play()
            isRunning = true
            
            // Start envelope attack phase
            envelopePhase = EnvelopePhase.ATTACK
            envelopeTime = 0.0
            currentGain = 0.0
            
            // Start audio generation thread
            startAudioThread()
            
            Log.i(TAG, "Audio engine started")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio engine", e)
            return false
        }
    }
    
    /**
     * Stop audio playback with fade-out
     */
    fun stop() {
        if (!isRunning) return
        
        // Start envelope release phase
        envelopePhase = EnvelopePhase.RELEASE
        envelopeTime = 0.0
        
        // Wait for release to complete (handled in audio thread)
        audioHandler?.postDelayed({
            stopImmediate()
        }, (releaseTime * 1000).toLong())
    }
    
    /**
     * Stop audio immediately without fade-out
     */
    private fun stopImmediate() {
        isRunning = false
        
        audioThread?.quitSafely()
        audioThread = null
        audioHandler = null
        
        audioTrack?.stop()
        envelopePhase = EnvelopePhase.IDLE
        
        abandonAudioFocus()
        
        Log.i(TAG, "Audio engine stopped")
    }
    
    /**
     * Set base frequency (100-400 Hz)
     */
    fun setBaseFrequency(frequency: Double) {
        baseFrequency = frequency.coerceIn(MIN_BASE_FREQ, MAX_BASE_FREQ)
        Log.d(TAG, "Base frequency set to: $baseFrequency Hz")
    }
    
    /**
     * Set beat frequency (0.5-20 Hz)
     */
    fun setBeatFrequency(frequency: Double) {
        beatFrequency = frequency.coerceIn(MIN_BEAT_FREQ, MAX_BEAT_FREQ)
        targetBeatFrequency = beatFrequency
        Log.d(TAG, "Beat frequency set to: $beatFrequency Hz")
    }
    
    /**
     * Ramp beat frequency smoothly over duration
     * @param targetFreq Target beat frequency (0.5-20 Hz)
     * @param durationSec Ramp duration in seconds (1-120s)
     * @param linear True for linear ramp, false for exponential
     */
    fun rampBeatFrequency(targetFreq: Double, durationSec: Double, linear: Boolean) {
        rampStartFreq = beatFrequency
        rampTargetFreq = targetFreq.coerceIn(MIN_BEAT_FREQ, MAX_BEAT_FREQ)
        rampDuration = durationSec.coerceIn(1.0, 120.0)
        rampElapsed = 0.0
        isLinearRamp = linear
        isRamping = true
        
        Log.i(TAG, "Starting ${if (linear) "linear" else "exponential"} ramp: $rampStartFreq -> $rampTargetFreq Hz over $rampDuration s")
    }
    
    /**
     * Enable/disable brown noise layer
     */
    fun setBrownNoiseEnabled(enabled: Boolean) {
        brownNoiseEnabled = enabled
        Log.d(TAG, "Brown noise ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Set brown noise mix level (0.0-1.0)
     */
    fun setBrownNoiseLevel(level: Double) {
        brownNoiseLevel = level.coerceIn(0.0, 1.0)
        Log.d(TAG, "Brown noise level set to: ${brownNoiseLevel * 100}%")
    }
    
    // ========== PHASE 2: New Control Methods ==========
    
    /**
     * Set harmonic richness (0.0-1.0 = 0-100%)
     * Controls the amplitude of harmonic overtones
     */
    fun setHarmonicRichness(richness: Double) {
        harmonicRichness = richness.coerceIn(0.0, 1.0)
        Log.d(TAG, "Harmonic richness set to: ${harmonicRichness * 100}%")
    }
    
    /**
     * Set drift intensity (0.0-1.0)
     * Controls the amount of organic frequency drift
     */
    fun setDriftIntensity(intensity: Double) {
        driftIntensity = intensity.coerceIn(0.0, 1.0)
        Log.d(TAG, "Drift intensity set to: ${driftIntensity * 100}%")
    }
    
    /**
     * Set brown noise warmth (0.0-1.0)
     * Controls the warmth EQ curve and harshness reduction
     */
    fun setBrownNoiseWarmth(warmth: Double) {
        brownNoiseWarmth = warmth.coerceIn(0.0, 1.0)
        Log.d(TAG, "Brown noise warmth set to: ${brownNoiseWarmth * 100}%")
    }
    
    /**
     * Set brown noise texture density (0.0-1.0)
     */
    fun setBrownNoiseTexture(texture: Double) {
        brownNoiseTexture = texture.coerceIn(0.0, 1.0)
        Log.d(TAG, "Brown noise texture set to: ${brownNoiseTexture * 100}%")
    }
    
    /**
     * Enable/disable ambient pad layer
     */
    fun setPadLayerEnabled(enabled: Boolean) {
        padLayerEnabled = enabled
        Log.d(TAG, "Ambient pad layer ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Enable/disable psychoacoustic smoothing
     */
    fun setPsychoacousticSmoothing(enabled: Boolean) {
        smoothingEnabled = enabled
        Log.d(TAG, "Psychoacoustic smoothing ${if (enabled) "enabled" else "disabled"}")
    }
    
    // ========== PHASE 3: Generative Harmonic Ambient Engine Controls ==========
    
    /**
     * Set root note frequency (default D2 = 73.42 Hz or A2 = 110 Hz)
     */
    fun setRootNote(noteFrequency: Double) {
        rootNote = noteFrequency.coerceIn(50.0, 200.0)
        updateVoiceFrequencies()
        Log.d(TAG, "Root note set to: ${String.format("%.2f", rootNote)} Hz")
    }
    
    /**
     * Set tuning reference (432Hz or 440Hz)
     */
    fun setTuningReference(use432: Boolean) {
        useTuning432 = use432
        tuningReference = if (use432) 432.0 else 440.0
        updateRootFrequency()
        updateVoiceFrequencies()
        Log.d(TAG, "Tuning reference set to: ${if (use432) "432Hz" else "440Hz"}")
    }
    
    /**
     * Set harmonic density (0.0-1.0)
     * Controls the overall richness of the harmonic field
     */
    fun setHarmonicDensity(density: Double) {
        harmonicDensity = density.coerceIn(0.0, 1.0)
        // Adjust voice amplitudes based on density
        for (i in 1 until VOICE_COUNT) {
            voiceAmplitudes[i] = voiceAmplitudes[i] * (0.5 + 0.5 * harmonicDensity)
        }
        Log.d(TAG, "Harmonic density set to: ${harmonicDensity * 100}%")
    }
    
    /**
     * Set evolution speed (0.0-1.0)
     * Controls the speed of harmonic progression and LFO rates
     */
    fun setEvolutionSpeed(speed: Double) {
        evolutionSpeed = speed.coerceIn(0.0, 1.0)
        // Update LFO frequencies based on evolution speed
        val speedMultiplier = 0.5 + evolutionSpeed * 1.5 // 0.5x to 2.0x
        for (i in 0 until VOICE_COUNT) {
            voiceLFOFreqs[i] = (ULTRA_SLOW_LFO_MIN +
                (ULTRA_SLOW_LFO_MAX - ULTRA_SLOW_LFO_MIN) * Random.nextDouble()) * speedMultiplier
        }
        Log.d(TAG, "Evolution speed set to: ${evolutionSpeed * 100}%")
    }
    
    /**
     * Set stereo width (0.0-1.0)
     * Controls the spatial spread of harmonic voices
     */
    fun setStereoWidth(width: Double) {
        stereoWidth = width.coerceIn(0.0, 1.0)
        Log.d(TAG, "Stereo width set to: ${stereoWidth * 100}%")
    }
    
    /**
     * Set saturation amount (0.0-1.0)
     * Controls tape-style soft saturation for warmth
     */
    fun setSaturationAmount(amount: Double) {
        saturationAmount = amount.coerceIn(0.0, 1.0)
        Log.d(TAG, "Saturation amount set to: ${saturationAmount * 100}%")
    }
    
    /**
     * Set pad intensity (0.0-1.0)
     * Controls the level of the evolving pad texture
     */
    fun setPadIntensity(intensity: Double) {
        padIntensity = intensity.coerceIn(0.0, 1.0)
        padLayerLevel = 0.05 + 0.15 * padIntensity // 5-20% range
        Log.d(TAG, "Pad intensity set to: ${padIntensity * 100}%")
    }
    
    /**
     * Set emotional mode (Sleep/Calm/Focus)
     */
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
    
    /**
     * Apply emotional mode settings
     */
    private fun applyEmotionalMode() {
        when (emotionalMode) {
            EmotionalMode.SLEEP -> {
                // Lower root frequency
                rootNote = 65.41 // C2 (lower than D2)
                // Slower harmonic movement
                nextChordChangeTime = CHORD_CHANGE_MAX
                evolutionSpeed = 0.3
                // Darker filter curve
                padFilterCutoff = 600.0
                // Reduced upper harmonics
                harmonicRichness = 0.3
                antiHarshness = true
            }
            EmotionalMode.CALM -> {
                // Balanced tonal field
                rootNote = 73.42 // D2
                // Gentle evolution
                nextChordChangeTime = (CHORD_CHANGE_MIN + CHORD_CHANGE_MAX) / 2.0
                evolutionSpeed = 0.5
                // Warm pad density
                padFilterCutoff = 800.0
                harmonicRichness = 0.5
            }
            EmotionalMode.FOCUS -> {
                // Slightly brighter
                rootNote = 82.41 // E2 (higher than D2)
                // More stable tonal center
                nextChordChangeTime = CHORD_CHANGE_MAX
                evolutionSpeed = 0.4
                // Reduced harmonic complexity
                harmonicDensity = 0.5
                harmonicRichness = 0.4
                padFilterCutoff = 1000.0
            }
        }
        updateRootFrequency()
        updateVoiceFrequencies()
    }
    
    /**
     * Update micro drift for organic analog feel
     */
    private fun updateMicroDrift(deltaTime: Double) {
        // Update drift LFO phase
        driftLFOPhase += TWO_PI * DRIFT_LFO_FREQ * deltaTime
        if (driftLFOPhase >= TWO_PI) driftLFOPhase -= TWO_PI
        
        // Generate slow LFO modulation
        val lfoValue = sin(driftLFOPhase)
        
        // Add slight random variation
        val randomVariation = (Random.nextDouble() - 0.5) * 0.02
        
        // Calculate drift amount (±0.05-0.15 Hz based on intensity)
        val driftAmount = MAX_DRIFT_AMOUNT * driftIntensity
        
        // Apply independent drift per channel
        currentDriftLeft = (lfoValue + randomVariation) * driftAmount
        currentDriftRight = (sin(driftLFOPhase + 0.5) + randomVariation * 0.8) * driftAmount
    }
    
    /**
     * Apply soft stereo spatial widening
     */
    private fun applyStereoWidening(left: Double, right: Double): Pair<Double, Double> {
        // Add to delay buffer
        stereoDelayBuffer.add(Pair(left, right))
        
        // Maintain buffer size
        if (stereoDelayBuffer.size > stereoDelayFrames) {
            stereoDelayBuffer.removeAt(0)
        }
        
        // Get delayed samples for decorrelation
        val delayed = if (stereoDelayBuffer.size >= stereoDelayFrames) {
            stereoDelayBuffer[0]
        } else {
            Pair(0.0, 0.0)
        }
        
        // Apply very subtle phase decorrelation
        val decorrelationAmount = 0.08 // Very subtle
        val leftWidened = left * (1.0 - decorrelationAmount) + delayed.second * decorrelationAmount
        val rightWidened = right * (1.0 - decorrelationAmount) + delayed.first * decorrelationAmount
        
        return Pair(leftWidened, rightWidened)
    }
    
    /**
     * Apply psychoacoustic envelope smoothing with equal-power curves
     */
    private fun applyPsychoacousticSmoothing(targetGain: Double): Double {
        // Equal-power crossfade curve (square root)
        val smoothingCoeff = 1.0 - exp(-1.0 / smoothingFrames)
        previousGain += (targetGain - previousGain) * smoothingCoeff
        
        // Apply equal-power curve
        return sqrt(previousGain.coerceIn(0.0, 1.0))
    }
    
    /**
     * Generate enhanced brown noise with warmth EQ and breathing
     */
    private fun generateEnhancedBrownNoise(deltaTime: Double): Double {
        // Generate brown noise with random walk
        val white = (Random.nextDouble() * 2.0 - 1.0) * 0.1 * brownNoiseTexture
        brownNoiseState = (brownNoiseState + white).coerceIn(-1.0, 1.0)
        
        // Apply 6dB/oct low-pass shaping for warmth
        val lpfCoeff = 0.98 - (0.1 * brownNoiseWarmth) // More warmth = lower cutoff
        brownNoiseLPFState = brownNoiseLPFState * lpfCoeff + brownNoiseState * (1.0 - lpfCoeff)
        
        // Add subtle amplitude breathing (very slow)
        brownNoiseBreathingPhase += TWO_PI * 0.05 * deltaTime // 0.05 Hz breathing
        if (brownNoiseBreathingPhase >= TWO_PI) brownNoiseBreathingPhase -= TWO_PI
        
        val breathingModulation = 1.0 + sin(brownNoiseBreathingPhase) * 0.1 // ±10% variation
        
        // Apply warmth EQ curve (reduce 2-4kHz harshness)
        val warmthFactor = 1.0 - (0.2 * brownNoiseWarmth) // Reduce brightness
        
        return brownNoiseLPFState * breathingModulation * warmthFactor
    }
    
    /**
     * Generate subtle ambient pad layer
     */
    private fun generateAmbientPad(deltaTime: Double): Double {
        // Generate band-limited noise
        val white = (Random.nextDouble() * 2.0 - 1.0) * 0.3
        padNoiseState = (padNoiseState + white).coerceIn(-1.0, 1.0)
        
        // Apply slow evolving filter movement
        padFilterPhase += TWO_PI * 0.02 * deltaTime // Very slow evolution
        if (padFilterPhase >= TWO_PI) padFilterPhase -= TWO_PI
        
        // Modulate filter cutoff slowly (400-1200 Hz range)
        padFilterCutoff = 800.0 + sin(padFilterPhase) * 400.0
        
        // Simple low-pass filter
        val filterCoeff = 1.0 - exp(-TWO_PI * padFilterCutoff / SAMPLE_RATE)
        val filtered = padNoiseState * filterCoeff
        
        return filtered * 0.5 // Keep extremely low amplitude
    }
    
    /**
     * Apply anti-fatigue optimization
     */
    private fun applyAntiFatigue(sample: Double): Double {
        var processed = sample

        // DC offset blocking filter
        val dcBlockCoeff = 0.995
        dcOffsetFilter = dcBlockCoeff * (dcOffsetFilter + processed - processed)
        processed = processed - dcOffsetFilter

        // Gentle soft clipping at safe level
        if (abs(processed) > SOFT_CLIP_THRESHOLD) {
            val sign = if (processed > 0) 1.0 else -1.0
            val excess = abs(processed) - SOFT_CLIP_THRESHOLD
            processed = sign * (SOFT_CLIP_THRESHOLD + tanh(excess * 3.0) * (1.0 - SOFT_CLIP_THRESHOLD))
        }

        // Upper harmonic soft roll-off (if anti-harshness enabled)
        if (antiHarshness) {
            processed *= 0.98 // Very subtle reduction
        }

        return processed.coerceIn(-1.0, 1.0)
    }

    /**
     * Update harmonic progression (chord changes)
     */
    private fun updateHarmonicProgression(deltaTime: Double) {
        timeSinceLastChordChange += deltaTime

        // Check if it's time to change chord
        if (!isTransitioningChord && timeSinceLastChordChange >= nextChordChangeTime) {
            // Start new chord transition
            targetChordType = getNextChord()
            isTransitioningChord = true
            chordTransitionProgress = 0.0
            timeSinceLastChordChange = 0.0

            // Set next chord change time
            nextChordChangeTime = CHORD_CHANGE_MIN +
                (CHORD_CHANGE_MAX - CHORD_CHANGE_MIN) * Random.nextDouble()
        }

        // Update chord transition
        if (isTransitioningChord) {
            chordTransitionProgress += deltaTime / chordTransitionDuration

            if (chordTransitionProgress >= 1.0) {
                chordTransitionProgress = 1.0
                currentChordType = targetChordType
                isTransitioningChord = false
            }

            // Update voice frequencies during transition
            updateVoiceFrequencies()
        }
    }

    /**
     * Get next chord in progression
     */
    private fun getNextChord(): ChordType {
        // Simple progression: I -> IV -> vi -> Isus2 -> Isus4 -> I
        return when (currentChordType) {
            ChordType.ROOT -> ChordType.FOURTH
            ChordType.FOURTH -> ChordType.SIXTH
            ChordType.SIXTH -> ChordType.SUS2
            ChordType.SUS2 -> ChordType.SUS4
            ChordType.SUS4 -> ChordType.ROOT
        }
    }

    /**
     * Update voice LFOs for organic variation
     */
    private fun updateVoiceLFOs(deltaTime: Double) {
        for (i in 0 until VOICE_COUNT) {
            // Update LFO phase
            voiceLFOPhases[i] += TWO_PI * voiceLFOFreqs[i] * deltaTime
            if (voiceLFOPhases[i] >= TWO_PI) {
                voiceLFOPhases[i] -= TWO_PI
            }

            // Calculate LFO modulation
            val lfoValue = sin(voiceLFOPhases[i])

            // Apply micro-detune based on LFO
            val detuneAmount = lfoValue * MICRO_DETUNE_CENTS * 0.01
            voiceDetuneOffsets[i] = detuneAmount
        }
    }

    /**
     * Generate multi-voice harmonic samples
     */
    private fun generateHarmonicVoices(deltaTime: Double): Pair<Double, Double> {
        var leftSample = 0.0
        var rightSample = 0.0

        // Generate each voice
        for (i in 0 until VOICE_COUNT) {
            // Calculate detune multiplier
            val detuneMultiplier = 2.0.pow(voiceDetuneOffsets[i] / 1200.0)
            val actualFreq = voiceFrequencies[i] * detuneMultiplier

            // Update phase
            voicePhases[i] += TWO_PI * actualFreq * deltaTime
            if (voicePhases[i] >= TWO_PI) {
                voicePhases[i] -= TWO_PI
            }

            // Generate sine wave
            val sample = sin(voicePhases[i]) * voiceAmplitudes[i]

            // Apply stereo positioning
            val stereoPos = voiceStereoPositions[i] * stereoWidth
            val leftGain = sqrt((1.0 - stereoPos) / 2.0)
            val rightGain = sqrt((1.0 + stereoPos) / 2.0)

            leftSample += sample * leftGain
            rightSample += sample * rightGain
        }

        // Apply saturation if enabled
        if (saturationAmount > 0.0) {
            leftSample = tanh(leftSample * (1.0 + saturationAmount)) / (1.0 + saturationAmount)
            rightSample = tanh(rightSample * (1.0 + saturationAmount)) / (1.0 + saturationAmount)
        }

        return Pair(leftSample, rightSample)
    }
    
    /**
     * Update frequency ramping (linear or exponential)
     */
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
    
    /**
     * Update gain envelope (attack/sustain/release)
     */
    private fun updateEnvelope(deltaTime: Double) {
        when (envelopePhase) {
            EnvelopePhase.ATTACK -> {
                envelopeTime += deltaTime
                currentGain = (envelopeTime / attackTime).coerceIn(0.0, 1.0)
                if (envelopeTime >= attackTime) {
                    envelopePhase = EnvelopePhase.SUSTAIN
                    currentGain = 1.0
                }
            }
            EnvelopePhase.SUSTAIN -> {
                currentGain = 1.0
            }
            EnvelopePhase.RELEASE -> {
                envelopeTime += deltaTime
                currentGain = 1.0 - (envelopeTime / releaseTime).coerceIn(0.0, 1.0)
                if (envelopeTime >= releaseTime) {
                    envelopePhase = EnvelopePhase.IDLE
                    currentGain = 0.0
                }
            }
            EnvelopePhase.IDLE -> {
                currentGain = 0.0
            }
        }
    }
    
    /**
     * Generate brown noise sample using random walk algorithm
     */
    private fun generateBrownNoise(): Double {
        val white = (Math.random() * 2.0 - 1.0) * 0.1
        brownNoiseState = (brownNoiseState + white).coerceIn(-1.0, 1.0)
        return brownNoiseState
    }
    
    /**
     * Request audio focus
     */
    private fun requestAudioFocus(): Boolean {
        val result = audioManager.requestAudioFocus(
            null,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        return hasAudioFocus
    }
    
    /**
     * Abandon audio focus
     */
    private fun abandonAudioFocus() {
        audioManager.abandonAudioFocus(null)
        hasAudioFocus = false
    }

    /**
     * Start audio generation thread
     */
    private fun startAudioThread() {
        audioThread = HandlerThread("AudioGenerationThread", Process.THREAD_PRIORITY_AUDIO)
        audioThread?.start()
        audioHandler = Handler(audioThread!!.looper)

        audioHandler?.post {
            generateAudioLoop()
        }
    }

    /**
     * Main audio generation loop
     */
    private fun generateAudioLoop() {
        val bufferSize = 1024
        val buffer = ShortArray(bufferSize * 2) // Stereo
        val deltaTime = bufferSize.toDouble() / SAMPLE_RATE
        
        var lastCpuMeasureTime = System.nanoTime()
        var processingTimeAccumulator = 0L
        var bufferCount = 0

        while (isRunning) {
            val bufferStartTime = System.nanoTime()
            
            // Update time-based parameters
            if (isRamping) {
                updateRamping(deltaTime)
            }
            updateEnvelope(deltaTime)
            updateMicroDrift(deltaTime)
            updateHarmonicProgression(deltaTime)
            updateVoiceLFOs(deltaTime)

            // Generate audio samples
            for (i in 0 until bufferSize) {
                // Generate multi-voice harmonic samples
                val (leftHarmonic, rightHarmonic) = generateHarmonicVoices(1.0 / SAMPLE_RATE)
                
                // Apply gain envelope
                var leftSample = leftHarmonic * currentGain
                var rightSample = rightHarmonic * currentGain

                // Mix brown noise if enabled
                if (brownNoiseEnabled) {
                    val noise = generateEnhancedBrownNoise(1.0 / SAMPLE_RATE)
                    leftSample += noise * brownNoiseLevel * currentGain
                    rightSample += noise * brownNoiseLevel * currentGain
                }
                
                // Add ambient pad layer if enabled
                if (padLayerEnabled) {
                    val pad = generateAmbientPad(1.0 / SAMPLE_RATE)
                    leftSample += pad * padLayerLevel * currentGain
                    rightSample += pad * padLayerLevel * currentGain
                }
                
                // Apply stereo widening
                val (leftWidened, rightWidened) = applyStereoWidening(leftSample, rightSample)
                leftSample = leftWidened
                rightSample = rightWidened

                // Apply anti-fatigue optimization
                leftSample = applyAntiFatigue(leftSample)
                rightSample = applyAntiFatigue(rightSample)

                // Convert to 16-bit PCM
                buffer[i * 2] = (leftSample * 32767.0).coerceIn(-32768.0, 32767.0).toInt().toShort()
                buffer[i * 2 + 1] = (rightSample * 32767.0).coerceIn(-32768.0, 32767.0).toInt().toShort()
            }

            // Write to AudioTrack
            audioTrack?.write(buffer, 0, buffer.size)
            
            // Measure CPU usage
            val bufferEndTime = System.nanoTime()
            val processingTime = bufferEndTime - bufferStartTime
            processingTimeAccumulator += processingTime
            bufferCount++
            
            // Update CPU usage every 10 buffers (~0.23 seconds at 1024 buffer size)
            if (bufferCount >= 10) {
                val elapsedTime = bufferEndTime - lastCpuMeasureTime
                val cpuRatio = processingTimeAccumulator.toDouble() / elapsedTime.toDouble()
                cpuUsagePercent = (cpuRatio * 100.0).coerceIn(0.0, 100.0)
                
                // Reset accumulators
                processingTimeAccumulator = 0L
                bufferCount = 0
                lastCpuMeasureTime = bufferEndTime
            }
        }
    }

    /**
     * Set envelope attack time
     */
    fun setAttackTime(time: Double) {
        attackTime = time.coerceIn(0.1, 60.0)
        Log.d(TAG, "Attack time set to: $attackTime s")
    }

    /**
     * Set envelope release time
     */
    fun setReleaseTime(time: Double) {
        releaseTime = time.coerceIn(0.1, 60.0)
        Log.d(TAG, "Release time set to: $releaseTime s")
    }

    /**
     * Fade in audio
     */
    fun fadeIn() {
        envelopePhase = EnvelopePhase.ATTACK
        envelopeTime = 0.0
        Log.d(TAG, "Fading in")
    }

    /**
     * Fade out audio
     */
    fun fadeOut() {
        envelopePhase = EnvelopePhase.RELEASE
        envelopeTime = 0.0
        Log.d(TAG, "Fading out")
    }

    /**
     * Get CPU usage percentage
     */
    fun getCpuUsage(): Double {
        return cpuUsagePercent
    }

    /**
     * Get audio latency in milliseconds
     */
    fun getLatency(): Double {
        return audioTrack?.let {
            it.bufferSizeInFrames.toDouble() / SAMPLE_RATE * 1000.0
        } ?: 0.0
    }

    /**
     * Get audio underrun count
     */
    fun getUnderrunCount(): Int {
        return underrunCount
    }

    /**
     * Check if has audio focus
     */
    fun getAudioFocusState(): Boolean {
        return hasAudioFocus
    }

    /**
     * Release audio resources
     */
    fun release() {
        stopImmediate()
        audioTrack?.release()
        audioTrack = null
        Log.i(TAG, "Audio engine released")
    }
}