import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';

import '../../../core/app_export.dart';

/// Generative Controls Widget
/// Developer tuning controls for harmonic density, evolution speed, stereo width, saturation, and pad intensity
class GenerativeControlsWidget extends StatelessWidget {
  final double harmonicDensity;
  final double evolutionSpeed;
  final double stereoWidth;
  final double saturationAmount;
  final double padIntensity;
  final Function(double) onHarmonicDensityChanged;
  final Function(double) onEvolutionSpeedChanged;
  final Function(double) onStereoWidthChanged;
  final Function(double) onSaturationAmountChanged;
  final Function(double) onPadIntensityChanged;

  const GenerativeControlsWidget({
    super.key,
    required this.harmonicDensity,
    required this.evolutionSpeed,
    required this.stereoWidth,
    required this.saturationAmount,
    required this.padIntensity,
    required this.onHarmonicDensityChanged,
    required this.onEvolutionSpeedChanged,
    required this.onStereoWidthChanged,
    required this.onSaturationAmountChanged,
    required this.onPadIntensityChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.all(2.w),
      decoration: BoxDecoration(
        color: Colors.grey[900],
        borderRadius: BorderRadius.circular(12.0),
        border: Border.all(color: Colors.purple[700]!, width: 1),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            'Generative Controls',
            style: TextStyle(
              fontSize: 14.sp,
              fontWeight: FontWeight.bold,
              color: Colors.purple[300],
            ),
          ),
          SizedBox(height: 1.h),

          // Harmonic Density
          _buildSlider(
            label: 'Harmonic Density',
            value: harmonicDensity,
            onChanged: onHarmonicDensityChanged,
            color: Colors.purple[400]!,
          ),

          // Evolution Speed
          _buildSlider(
            label: 'Evolution Speed',
            value: evolutionSpeed,
            onChanged: onEvolutionSpeedChanged,
            color: Colors.purple[400]!,
          ),

          // Stereo Width
          _buildSlider(
            label: 'Stereo Width',
            value: stereoWidth,
            onChanged: onStereoWidthChanged,
            color: Colors.purple[400]!,
          ),

          // Saturation Amount
          _buildSlider(
            label: 'Saturation',
            value: saturationAmount,
            onChanged: onSaturationAmountChanged,
            color: Colors.purple[400]!,
          ),

          // Pad Intensity
          _buildSlider(
            label: 'Pad Intensity',
            value: padIntensity,
            onChanged: onPadIntensityChanged,
            color: Colors.purple[400]!,
          ),
        ],
      ),
    );
  }

  Widget _buildSlider({
    required String label,
    required double value,
    required Function(double) onChanged,
    required Color color,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              label,
              style: TextStyle(fontSize: 11.sp, color: Colors.white70),
            ),
            Text(
              '${value.toStringAsFixed(0)}%',
              style: TextStyle(
                fontSize: 11.sp,
                color: color,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
        SliderTheme(
          data: SliderThemeData(
            activeTrackColor: color,
            inactiveTrackColor: Colors.grey[800],
            thumbColor: color,
            overlayColor: color.withAlpha(51),
            trackHeight: 2.0,
          ),
          child: Slider(value: value, min: 0, max: 100, onChanged: onChanged),
        ),
        SizedBox(height: 0.5.h),
      ],
    );
  }
}
