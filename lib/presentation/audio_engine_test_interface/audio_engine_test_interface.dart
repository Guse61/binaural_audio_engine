import 'dart:async';

import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';

import '../../core/app_export.dart';
import '../../services/native_audio_service.dart';
import '../../widgets/custom_icon_widget.dart';
import './widgets/audio_focus_indicator_widget.dart';
import './widgets/brown_noise_control_widget.dart';
import './widgets/drift_intensity_control_widget.dart';
import './widgets/emotional_mode_selector_widget.dart';
import './widgets/frequency_control_widget.dart';
import './widgets/gain_envelope_control_widget.dart';
import './widgets/generative_controls_widget.dart';
import './widgets/harmonic_richness_control_widget.dart';
import './widgets/nature_sound_control_widget.dart';
import './widgets/performance_indicators_widget.dart';
import './widgets/premium_features_control_widget.dart';
import './widgets/ramp_control_widget.dart';
import './widgets/tonal_system_control_widget.dart';
import './widgets/warmth_control_widget.dart';
import './widgets/waveform_visualization_widget.dart';

/// Audio Engine Test Interface
/// Professional-grade real-time binaural audio engine test harness
class AudioEngineTestInterface extends StatefulWidget {
  const AudioEngineTestInterface({super.key});

  @override
  State<AudioEngineTestInterface> createState() =>
      _AudioEngineTestInterfaceState();
}

class _AudioEngineTestInterfaceState extends State<AudioEngineTestInterface> {
  // Native audio service
  final NativeAudioService _audioService = NativeAudioService();

  // Performance monitoring timer
  Timer? _performanceTimer;

  // Audio engine state
  bool _isPlaying = false;
  double _baseFrequency = 250.0; // 100-400Hz range
  double _beatFrequency = 10.0; // 0.5-20Hz range
  bool _brownNoiseEnabled = false;
  double _brownNoiseLevel = 50.0; // 0-100% mix
  double _rampDuration = 30.0; // 1-120s
  bool _isLinearRamp = true; // true = linear, false = exponential
  double _attackTime = 10.0; // 10s attack
  double _releaseTime = 10.0; // 10s release
  double _cpuUsage = 0.0;
  double _latency = 0.0;
  bool _hasAudioFocus = false;
  List<double> _waveformData = List.generate(100, (index) => 0.0);

  // Phase 2: Premium sound design parameters
  double _harmonicRichness = 50.0; // 0-100%
  double _driftIntensity = 50.0; // 0-100%
  double _brownNoiseWarmth = 50.0; // 0-100%
  double _brownNoiseTexture = 50.0; // 0-100%
  bool _padLayerEnabled = false;
  bool _psychoacousticSmoothing = true;

  // Phase 3: Generative harmonic ambient engine parameters
  double _rootNote = 73.42; // D2 default
  bool _useTuning432 = false; // false = 440Hz, true = 432Hz
  double _harmonicDensity = 70.0; // 0-100%
  double _evolutionSpeed = 50.0; // 0-100%
  double _stereoWidth = 50.0; // 0-100%
  double _saturationAmount = 30.0; // 0-100%
  double _padIntensity = 50.0; // 0-100%
  int _emotionalMode = 1; // 0=Sleep, 1=Calm, 2=Focus

  // Add this block - Nature sound parameters
  String _selectedNatureSound = 'none'; // default nature sound
  double _natureSoundVolume = 50.0; // 0-100%

  @override
  void initState() {
    super.initState();
    _initializeAudioEngine();
  }

  @override
  void dispose() {
    _cleanupAudioEngine();
    super.dispose();
  }

  /// Initialize audio engine with default parameters
  Future<void> _initializeAudioEngine() async {
    final success = await _audioService.initialize();
    if (success) {
      print('Native audio engine initialized successfully');
      // Set initial parameters
      await _audioService.setBaseFrequency(_baseFrequency);
      await _audioService.setBeatFrequency(_beatFrequency);
      await _audioService.setAttackTime(_attackTime);
      await _audioService.setReleaseTime(_releaseTime);

      // Set Phase 2 parameters
      await _audioService.setHarmonicRichness(_harmonicRichness / 100.0);
      await _audioService.setDriftIntensity(_driftIntensity / 100.0);
      await _audioService.setBrownNoiseWarmth(_brownNoiseWarmth / 100.0);
      await _audioService.setBrownNoiseTexture(_brownNoiseTexture / 100.0);
      await _audioService.setPadLayerEnabled(_padLayerEnabled);
      await _audioService.setPsychoacousticSmoothing(_psychoacousticSmoothing);

      // Set Phase 3 parameters
      await _audioService.setRootNote(_rootNote);
      await _audioService.setTuningReference(_useTuning432);
      await _audioService.setHarmonicDensity(_harmonicDensity / 100.0);
      await _audioService.setEvolutionSpeed(_evolutionSpeed / 100.0);
      await _audioService.setStereoWidth(_stereoWidth / 100.0);
      await _audioService.setSaturationAmount(_saturationAmount / 100.0);
      await _audioService.setPadIntensity(_padIntensity / 100.0);
      await _audioService.setEmotionalMode(_emotionalMode);
    } else {
      print('Failed to initialize native audio engine');
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              'Failed to initialize audio engine',
              style: TextStyle(fontSize: 12.sp),
            ),
            backgroundColor: Colors.red,
          ),
        );
      }
    }

    // Simulate initial waveform data
    setState(() {
      _waveformData = List.generate(100, (index) {
        return (index % 10) / 10.0;
      });
    });
  }

  /// Cleanup audio engine resources
  Future<void> _cleanupAudioEngine() async {
    _performanceTimer?.cancel();
    if (_isPlaying) {
      await _stopAudio();
    }
  }

  /// Toggle play/stop state
  Future<void> _togglePlayStop() async {
    if (_isPlaying) {
      await _stopAudio();
    } else {
      await _startAudio();
    }
  }

  /// Start audio engine
  Future<void> _startAudio() async {
    final success = await _audioService.start();
    if (success) {
      setState(() {
        _isPlaying = true;
      });
      _startPerformanceMonitoring();
      _updateWaveform();
    } else {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              'Failed to start audio engine',
              style: TextStyle(fontSize: 12.sp),
            ),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  /// Stop audio engine
  Future<void> _stopAudio() async {
    await _audioService.stop();
    _performanceTimer?.cancel();
    setState(() {
      _isPlaying = false;
      _cpuUsage = 0.0;
      _latency = 0.0;
      _hasAudioFocus = false;
      _waveformData = List.generate(100, (index) => 0.0);
    });
  }

  /// Start performance monitoring timer
  void _startPerformanceMonitoring() {
    _performanceTimer?.cancel();
    _performanceTimer = Timer.periodic(const Duration(milliseconds: 500), (
      timer,
    ) async {
      if (!_isPlaying) {
        timer.cancel();
        return;
      }

      final cpuUsage = await _audioService.getCpuUsage();
      final latency = await _audioService.getLatency();
      final hasFocus = await _audioService.hasAudioFocus();

      if (mounted) {
        setState(() {
          _cpuUsage = cpuUsage;
          _latency = latency;
          _hasAudioFocus = hasFocus;
        });
      }
    });
  }

  /// Update base frequency
  Future<void> _updateBaseFrequency(double value) async {
    setState(() {
      _baseFrequency = value;
    });
    await _audioService.setBaseFrequency(value);
    if (_isPlaying) {
      _updateWaveform();
    }
  }

  /// Update beat frequency
  Future<void> _updateBeatFrequency(double value) async {
    setState(() {
      _beatFrequency = value;
    });
    await _audioService.setBeatFrequency(value);
    if (_isPlaying) {
      _updateWaveform();
    }
  }

  /// Toggle brown noise
  Future<void> _toggleBrownNoise(bool value) async {
    setState(() {
      _brownNoiseEnabled = value;
    });
    await _audioService.setBrownNoiseEnabled(value);
    if (_isPlaying) {
      _updateWaveform();
    }
  }

  /// Update brown noise level
  Future<void> _updateBrownNoiseLevel(double value) async {
    setState(() {
      _brownNoiseLevel = value;
    });
    await _audioService.setBrownNoiseLevel(value / 100.0);
    if (_isPlaying) {
      _updateWaveform();
    }
  }

  /// Update ramp duration
  void _updateRampDuration(double value) {
    setState(() {
      _rampDuration = value;
    });
  }

  /// Toggle ramp mode
  void _toggleRampMode(bool isLinear) {
    setState(() {
      _isLinearRamp = isLinear;
    });
  }

  /// Execute frequency ramp
  Future<void> _executeRamp() async {
    if (!_isPlaying) return;

    await _audioService.rampBeatFrequency(
      targetFrequency: _beatFrequency,
      duration: _rampDuration,
      linear: _isLinearRamp,
    );

    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Executing ${_isLinearRamp ? 'linear' : 'exponential'} ramp over ${_rampDuration.toInt()}s',
            style: TextStyle(fontSize: 12.sp),
          ),
          duration: const Duration(seconds: 2),
        ),
      );
    }
  }

  /// Update attack time
  Future<void> _updateAttackTime(double value) async {
    setState(() {
      _attackTime = value;
    });
    await _audioService.setAttackTime(value);
  }

  /// Update release time
  Future<void> _updateReleaseTime(double value) async {
    setState(() {
      _releaseTime = value;
    });
    await _audioService.setReleaseTime(value);
  }

  // ========== PHASE 2: Premium Sound Design Control Methods ==========

  /// Update harmonic richness
  Future<void> _updateHarmonicRichness(double value) async {
    setState(() {
      _harmonicRichness = value;
    });
    await _audioService.setHarmonicRichness(value / 100.0);
    if (_isPlaying) {
      _updateWaveform();
    }
  }

  /// Update drift intensity
  Future<void> _updateDriftIntensity(double value) async {
    setState(() {
      _driftIntensity = value;
    });
    await _audioService.setDriftIntensity(value / 100.0);
  }

  /// Update brown noise warmth
  Future<void> _updateBrownNoiseWarmth(double value) async {
    setState(() {
      _brownNoiseWarmth = value;
    });
    await _audioService.setBrownNoiseWarmth(value / 100.0);
    if (_isPlaying && _brownNoiseEnabled) {
      _updateWaveform();
    }
  }

  /// Update brown noise texture
  Future<void> _updateBrownNoiseTexture(double value) async {
    setState(() {
      _brownNoiseTexture = value;
    });
    await _audioService.setBrownNoiseTexture(value / 100.0);
    if (_isPlaying && _brownNoiseEnabled) {
      _updateWaveform();
    }
  }

  /// Toggle ambient pad layer
  Future<void> _togglePadLayer(bool value) async {
    setState(() {
      _padLayerEnabled = value;
    });
    await _audioService.setPadLayerEnabled(value);
    if (_isPlaying) {
      _updateWaveform();
    }
  }

  /// Toggle psychoacoustic smoothing
  Future<void> _togglePsychoacousticSmoothing(bool value) async {
    setState(() {
      _psychoacousticSmoothing = value;
    });
    await _audioService.setPsychoacousticSmoothing(value);
  }

  // ========== PHASE 3: Generative Harmonic Ambient Engine Control Methods ==========

  /// Update root note
  Future<void> _updateRootNote(double value) async {
    setState(() {
      _rootNote = value;
    });
    await _audioService.setRootNote(value);
  }

  /// Update tuning reference
  Future<void> _updateTuningReference(bool use432) async {
    setState(() {
      _useTuning432 = use432;
    });
    await _audioService.setTuningReference(use432);
  }

  /// Update harmonic density
  Future<void> _updateHarmonicDensity(double value) async {
    setState(() {
      _harmonicDensity = value;
    });
    await _audioService.setHarmonicDensity(value / 100.0);
  }

  /// Update evolution speed
  Future<void> _updateEvolutionSpeed(double value) async {
    setState(() {
      _evolutionSpeed = value;
    });
    await _audioService.setEvolutionSpeed(value / 100.0);
  }

  /// Update stereo width
  Future<void> _updateStereoWidth(double value) async {
    setState(() {
      _stereoWidth = value;
    });
    await _audioService.setStereoWidth(value / 100.0);
  }

  /// Update saturation amount
  Future<void> _updateSaturationAmount(double value) async {
    setState(() {
      _saturationAmount = value;
    });
    await _audioService.setSaturationAmount(value / 100.0);
  }

  /// Update pad intensity
  Future<void> _updatePadIntensity(double value) async {
    setState(() {
      _padIntensity = value;
    });
    await _audioService.setPadIntensity(value / 100.0);
  }

  /// Update emotional mode
  Future<void> _updateEmotionalMode(int mode) async {
    setState(() {
      _emotionalMode = mode;
    });
    await _audioService.setEmotionalMode(mode);
  }

  // Add this block - Nature sound control methods
  /// Update nature sound
  Future<void> _updateNatureSound(String sound) async {
    setState(() {
      _selectedNatureSound = sound;
    });
    // Add native audio service call if available
    // await _audioService.setNatureSound(sound);
  }

  /// Update nature sound volume
  Future<void> _updateNatureSoundVolume(double value) async {
    setState(() {
      _natureSoundVolume = value;
    });
    // Add native audio service call if available
    // await _audioService.setNatureSoundVolume(value / 100.0);
  }

  /// Update waveform visualization
  void _updateWaveform() {
    setState(() {
      _waveformData = List.generate(100, (index) {
        double baseWave = (index * _baseFrequency / 100.0) % 1.0;
        double beatWave = (index * _beatFrequency / 100.0) % 1.0;
        double combined = (baseWave + beatWave) / 2.0;

        if (_brownNoiseEnabled) {
          double noise = (index % 7) / 7.0 * (_brownNoiseLevel / 100.0);
          combined = (combined * (1.0 - _brownNoiseLevel / 100.0)) + noise;
        }

        return combined;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: theme.scaffoldBackgroundColor,
      body: SafeArea(
        child: Padding(
          padding: EdgeInsets.symmetric(vertical: 2.h, horizontal: 4.w),
          child: Column(
            children: [
              // Audio focus indicator
              AudioFocusIndicatorWidget(hasAudioFocus: _hasAudioFocus),
              SizedBox(height: 2.h),

              // Play/Stop controls
              _buildPlayStopControls(theme),
              SizedBox(height: 3.h),

              // Scrollable content
              Expanded(
                child: SingleChildScrollView(
                  child: Column(
                    children: [
                      // Frequency controls
                      FrequencyControlWidget(
                        baseFrequency: _baseFrequency,
                        beatFrequency: _beatFrequency,
                        onBaseFrequencyChanged: _updateBaseFrequency,
                        onBeatFrequencyChanged: _updateBeatFrequency,
                      ),
                      SizedBox(height: 3.h),

                      // Brown noise controls
                      BrownNoiseControlWidget(
                        isEnabled: _brownNoiseEnabled,
                        level: _brownNoiseLevel,
                        onToggle: _toggleBrownNoise,
                        onLevelChanged: _updateBrownNoiseLevel,
                      ),
                      SizedBox(height: 3.h),

                      // Ramp controls
                      RampControlWidget(
                        duration: _rampDuration,
                        isLinearMode: _isLinearRamp,
                        onDurationChanged: _updateRampDuration,
                        onModeToggle: _toggleRampMode,
                        onExecuteRamp: _executeRamp,
                        isPlaying: _isPlaying,
                      ),
                      SizedBox(height: 3.h),

                      // Gain envelope controls
                      GainEnvelopeControlWidget(
                        attackTime: _attackTime,
                        releaseTime: _releaseTime,
                        onAttackChanged: _updateAttackTime,
                        onReleaseChanged: _updateReleaseTime,
                      ),
                      SizedBox(height: 3.h),

                      // ========== PHASE 2: Premium Sound Design Controls ==========

                      // Harmonic richness control
                      HarmonicRichnessControlWidget(
                        harmonicRichness: _harmonicRichness / 100.0,
                        onHarmonicRichnessChanged: (value) =>
                            _updateHarmonicRichness(value * 100.0),
                      ),
                      SizedBox(height: 3.h),

                      // Drift intensity control
                      DriftIntensityControlWidget(
                        driftIntensity: _driftIntensity / 100.0,
                        onDriftIntensityChanged: (value) =>
                            _updateDriftIntensity(value * 100.0),
                      ),
                      SizedBox(height: 3.h),

                      // Warmth control (brown noise warmth + texture)
                      WarmthControlWidget(
                        warmth: _brownNoiseWarmth / 100.0,
                        texture: _brownNoiseTexture / 100.0,
                        onWarmthChanged: (value) =>
                            _updateBrownNoiseWarmth(value * 100.0),
                        onTextureChanged: (value) =>
                            _updateBrownNoiseTexture(value * 100.0),
                      ),
                      SizedBox(height: 3.h),

                      // Premium features control
                      PremiumFeaturesControlWidget(
                        padLayerEnabled: _padLayerEnabled,
                        psychoacousticSmoothing: _psychoacousticSmoothing,
                        onPadLayerChanged: _togglePadLayer,
                        onSmoothingChanged: _togglePsychoacousticSmoothing,
                      ),
                      SizedBox(height: 3.h),

                      // ========== PHASE 3: Generative Harmonic Ambient Engine Controls ==========

                      // Tonal system control
                      TonalSystemControlWidget(
                        rootNote: _rootNote,
                        useTuning432: _useTuning432,
                        onRootNoteChanged: _updateRootNote,
                        onTuningReferenceChanged: _updateTuningReference,
                      ),
                      SizedBox(height: 3.h),

                      // Emotional mode selector
                      EmotionalModeSelectorWidget(
                        selectedMode: _emotionalMode,
                        onModeChanged: _updateEmotionalMode,
                      ),
                      SizedBox(height: 3.h),

                      // Generative controls
                      GenerativeControlsWidget(
                        harmonicDensity: _harmonicDensity,
                        evolutionSpeed: _evolutionSpeed,
                        stereoWidth: _stereoWidth,
                        saturationAmount: _saturationAmount,
                        padIntensity: _padIntensity,
                        onHarmonicDensityChanged: _updateHarmonicDensity,
                        onEvolutionSpeedChanged: _updateEvolutionSpeed,
                        onStereoWidthChanged: _updateStereoWidth,
                        onSaturationAmountChanged: _updateSaturationAmount,
                        onPadIntensityChanged: _updatePadIntensity,
                      ),
                      SizedBox(height: 3.h),

                      // Nature sound overlay
                      NatureSoundControlWidget(
                        selectedSound: _selectedNatureSound,
                        volume: _natureSoundVolume,
                        onSoundChanged: _updateNatureSound,
                        onVolumeChanged: _updateNatureSoundVolume,
                      ),
                      SizedBox(height: 3.h),

                      // Performance indicators
                      PerformanceIndicatorsWidget(
                        cpuUsage: _cpuUsage,
                        latency: _latency,
                      ),
                      SizedBox(height: 3.h),

                      // Waveform visualization
                      WaveformVisualizationWidget(
                        waveformData: _waveformData,
                        isPlaying: _isPlaying,
                      ),
                      SizedBox(height: 2.h),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  /// Build play/stop controls
  Widget _buildPlayStopControls(ThemeData theme) {
    return Container(
      padding: EdgeInsets.symmetric(horizontal: 4.w, vertical: 2.h),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: theme.shadowColor,
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          ElevatedButton.icon(
            onPressed: _togglePlayStop,
            icon: CustomIconWidget(
              iconName: _isPlaying ? 'stop' : 'play_arrow',
              color: theme.colorScheme.onPrimary,
              size: 24,
            ),
            label: Text(
              _isPlaying ? 'Stop' : 'Play',
              style: theme.textTheme.titleMedium?.copyWith(
                color: theme.colorScheme.onPrimary,
              ),
            ),
            style: ElevatedButton.styleFrom(
              backgroundColor: _isPlaying
                  ? theme.colorScheme.error
                  : theme.colorScheme.tertiary,
              padding: EdgeInsets.symmetric(horizontal: 8.w, vertical: 2.h),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
              ),
            ),
          ),
        ],
      ),
    );
  }
}