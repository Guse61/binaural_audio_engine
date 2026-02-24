import 'package:flutter/services.dart';
import 'dart:async';

/// NativeAudioService - Flutter service layer for native audio engine communication
///
/// Provides a clean Dart API to communicate with the native Kotlin BinauralAudioEngine
/// via platform channels. Handles all method channel calls and error handling.
class NativeAudioService {
  static const MethodChannel _channel = MethodChannel(
    'com.example.binaural_audio_engine/audio',
  );

  /// Singleton instance
  static final NativeAudioService _instance = NativeAudioService._internal();
  factory NativeAudioService() => _instance;
  NativeAudioService._internal();

  /// Initialize the native audio engine
  /// Returns true if initialization was successful
  Future<bool> initialize() async {
    try {
      final bool result = await _channel.invokeMethod('initialize');
      return result;
    } catch (e) {
      print('Error initializing audio engine: $e');
      return false;
    }
  }

  /// Start audio playback with fade-in
  /// Returns true if start was successful
  Future<bool> start() async {
    try {
      final bool result = await _channel.invokeMethod('start');
      return result;
    } catch (e) {
      print('Error starting audio engine: $e');
      return false;
    }
  }

  /// Stop audio playback with fade-out
  Future<void> stop() async {
    try {
      await _channel.invokeMethod('stop');
    } catch (e) {
      print('Error stopping audio engine: $e');
    }
  }

  /// Set base frequency (100-400 Hz)
  /// @param frequency Base frequency in Hz
  Future<void> setBaseFrequency(double frequency) async {
    try {
      await _channel.invokeMethod('setBaseFrequency', {'frequency': frequency});
    } catch (e) {
      print('Error setting base frequency: $e');
    }
  }

  /// Set beat frequency (0.5-20 Hz)
  /// @param frequency Beat frequency in Hz
  Future<void> setBeatFrequency(double frequency) async {
    try {
      await _channel.invokeMethod('setBeatFrequency', {'frequency': frequency});
    } catch (e) {
      print('Error setting beat frequency: $e');
    }
  }

  /// Ramp beat frequency smoothly over duration
  /// @param targetFrequency Target beat frequency (0.5-20 Hz)
  /// @param duration Ramp duration in seconds (1-120s)
  /// @param linear True for linear ramp, false for exponential
  Future<void> rampBeatFrequency({
    required double targetFrequency,
    required double duration,
    required bool linear,
  }) async {
    try {
      await _channel.invokeMethod('rampBeatFrequency', {
        'targetFrequency': targetFrequency,
        'duration': duration,
        'linear': linear,
      });
    } catch (e) {
      print('Error ramping beat frequency: $e');
    }
  }

  /// Enable or disable brown noise layer
  /// @param enabled True to enable, false to disable
  Future<void> setBrownNoiseEnabled(bool enabled) async {
    try {
      await _channel.invokeMethod('setBrownNoiseEnabled', {'enabled': enabled});
    } catch (e) {
      print('Error setting brown noise enabled: $e');
    }
  }

  /// Set brown noise mix level
  /// @param level Mix level from 0.0 (0%) to 1.0 (100%)
  Future<void> setBrownNoiseLevel(double level) async {
    try {
      await _channel.invokeMethod('setBrownNoiseLevel', {'level': level});
    } catch (e) {
      print('Error setting brown noise level: $e');
    }
  }

  /// Set envelope attack time
  /// @param time Attack time in seconds
  Future<void> setAttackTime(double time) async {
    try {
      await _channel.invokeMethod('setAttackTime', {'time': time});
    } catch (e) {
      print('Error setting attack time: $e');
    }
  }

  /// Set envelope release time
  /// @param time Release time in seconds
  Future<void> setReleaseTime(double time) async {
    try {
      await _channel.invokeMethod('setReleaseTime', {'time': time});
    } catch (e) {
      print('Error setting release time: $e');
    }
  }

  /// Fade in audio (start attack envelope)
  Future<void> fadeIn() async {
    try {
      await _channel.invokeMethod('fadeIn');
    } catch (e) {
      print('Error fading in: $e');
    }
  }

  /// Fade out audio (start release envelope)
  Future<void> fadeOut() async {
    try {
      await _channel.invokeMethod('fadeOut');
    } catch (e) {
      print('Error fading out: $e');
    }
  }

  /// Get current CPU usage percentage
  /// Returns CPU usage as a percentage (0.0-100.0)
  Future<double> getCpuUsage() async {
    try {
      final double result = await _channel.invokeMethod('getCpuUsage');
      return result;
    } catch (e) {
      print('Error getting CPU usage: $e');
      return 0.0;
    }
  }

  /// Get current audio latency in milliseconds
  /// Returns latency in milliseconds
  Future<double> getLatency() async {
    try {
      final double result = await _channel.invokeMethod('getLatency');
      return result;
    } catch (e) {
      print('Error getting latency: $e');
      return 0.0;
    }
  }

  /// Get audio underrun count
  /// Returns number of audio underruns detected
  Future<int> getUnderrunCount() async {
    try {
      final int result = await _channel.invokeMethod('getUnderrunCount');
      return result;
    } catch (e) {
      print('Error getting underrun count: $e');
      return 0;
    }
  }

  /// Check if audio focus is held
  /// Returns true if audio focus is held
  Future<bool> hasAudioFocus() async {
    try {
      final bool result = await _channel.invokeMethod('hasAudioFocus');
      return result;
    } catch (e) {
      print('Error checking audio focus: $e');
      return false;
    }
  }

  // ========== PHASE 2: Premium Sound Design Controls ==========

  /// Set harmonic richness (0.0-1.0 = 0-100%)
  /// Controls the amplitude of harmonic overtones for warmer sound
  Future<void> setHarmonicRichness(double richness) async {
    try {
      await _channel.invokeMethod('setHarmonicRichness', {
        'richness': richness,
      });
    } catch (e) {
      print('Error setting harmonic richness: $e');
    }
  }

  /// Set drift intensity (0.0-1.0)
  /// Controls the amount of organic frequency drift for analog feel
  Future<void> setDriftIntensity(double intensity) async {
    try {
      await _channel.invokeMethod('setDriftIntensity', {
        'intensity': intensity,
      });
    } catch (e) {
      print('Error setting drift intensity: $e');
    }
  }

  /// Set brown noise warmth (0.0-1.0)
  /// Controls the warmth EQ curve and harshness reduction
  Future<void> setBrownNoiseWarmth(double warmth) async {
    try {
      await _channel.invokeMethod('setBrownNoiseWarmth', {'warmth': warmth});
    } catch (e) {
      print('Error setting brown noise warmth: $e');
    }
  }

  /// Set brown noise texture density (0.0-1.0)
  /// Controls the density and character of brown noise
  Future<void> setBrownNoiseTexture(double texture) async {
    try {
      await _channel.invokeMethod('setBrownNoiseTexture', {'texture': texture});
    } catch (e) {
      print('Error setting brown noise texture: $e');
    }
  }

  /// Enable or disable ambient pad layer
  /// Adds a subtle evolving ambient pad for immersive quality
  Future<void> setPadLayerEnabled(bool enabled) async {
    try {
      await _channel.invokeMethod('setPadLayerEnabled', {'enabled': enabled});
    } catch (e) {
      print('Error setting pad layer enabled: $e');
    }
  }

  /// Enable or disable psychoacoustic smoothing
  /// Applies equal-power curves and micro-smoothing to eliminate zipper noise
  Future<void> setPsychoacousticSmoothing(bool enabled) async {
    try {
      await _channel.invokeMethod('setPsychoacousticSmoothing', {
        'enabled': enabled,
      });
    } catch (e) {
      print('Error setting psychoacoustic smoothing: $e');
    }
  }

  // ========== PHASE 3: Generative Harmonic Ambient Engine Controls ==========

  /// Set root note frequency (default D2 = 73.42 Hz or A2 = 110 Hz)
  /// All harmonic layers derive from this tonal center
  Future<void> setRootNote(double noteFrequency) async {
    try {
      await _channel.invokeMethod('setRootNote', {
        'noteFrequency': noteFrequency,
      });
    } catch (e) {
      print('Error setting root note: $e');
    }
  }

  /// Set tuning reference (432Hz or 440Hz)
  /// @param use432 True for 432Hz, false for 440Hz (concert pitch)
  Future<void> setTuningReference(bool use432) async {
    try {
      await _channel.invokeMethod('setTuningReference', {'use432': use432});
    } catch (e) {
      print('Error setting tuning reference: $e');
    }
  }

  /// Set harmonic density (0.0-1.0)
  /// Controls the overall richness of the harmonic field
  Future<void> setHarmonicDensity(double density) async {
    try {
      await _channel.invokeMethod('setHarmonicDensity', {'density': density});
    } catch (e) {
      print('Error setting harmonic density: $e');
    }
  }

  /// Set evolution speed (0.0-1.0)
  /// Controls the speed of harmonic progression and LFO rates
  Future<void> setEvolutionSpeed(double speed) async {
    try {
      await _channel.invokeMethod('setEvolutionSpeed', {'speed': speed});
    } catch (e) {
      print('Error setting evolution speed: $e');
    }
  }

  /// Set stereo width (0.0-1.0)
  /// Controls the spatial spread of harmonic voices
  Future<void> setStereoWidth(double width) async {
    try {
      await _channel.invokeMethod('setStereoWidth', {'width': width});
    } catch (e) {
      print('Error setting stereo width: $e');
    }
  }

  /// Set saturation amount (0.0-1.0)
  /// Controls tape-style soft saturation for warmth
  Future<void> setSaturationAmount(double amount) async {
    try {
      await _channel.invokeMethod('setSaturationAmount', {'amount': amount});
    } catch (e) {
      print('Error setting saturation amount: $e');
    }
  }

  /// Set pad intensity (0.0-1.0)
  /// Controls the level of the evolving pad texture
  Future<void> setPadIntensity(double intensity) async {
    try {
      await _channel.invokeMethod('setPadIntensity', {'intensity': intensity});
    } catch (e) {
      print('Error setting pad intensity: $e');
    }
  }

  /// Set emotional mode (Sleep/Calm/Focus)
  /// @param mode 0=Sleep, 1=Calm, 2=Focus
  Future<void> setEmotionalMode(int mode) async {
    try {
      await _channel.invokeMethod('setEmotionalMode', {'mode': mode});
    } catch (e) {
      print('Error setting emotional mode: $e');
    }
  }
}
