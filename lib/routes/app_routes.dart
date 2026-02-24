import 'package:flutter/material.dart';
import '../presentation/audio_engine_test_interface/audio_engine_test_interface.dart';

class AppRoutes {
  // TODO: Add your routes here
  static const String initial = '/';
  static const String audioEngineTestInterface = '/audio-engine-test-interface';

  static Map<String, WidgetBuilder> routes = {
    initial: (context) => const AudioEngineTestInterface(),
    audioEngineTestInterface: (context) => const AudioEngineTestInterface(),
    // TODO: Add your other routes here
  };
}
