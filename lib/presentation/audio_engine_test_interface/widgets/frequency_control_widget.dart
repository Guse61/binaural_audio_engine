import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';

import '../../../core/app_export.dart';
import '../../../theme/app_theme.dart';

/// Frequency control widget for base and beat frequency adjustment
class FrequencyControlWidget extends StatelessWidget {
  final double baseFrequency;
  final double beatFrequency;
  final ValueChanged<double> onBaseFrequencyChanged;
  final ValueChanged<double> onBeatFrequencyChanged;

  const FrequencyControlWidget({
    super.key,
    required this.baseFrequency,
    required this.beatFrequency,
    required this.onBaseFrequencyChanged,
    required this.onBeatFrequencyChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: EdgeInsets.all(4.w),
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
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Frequency Controls',
            style: theme.textTheme.titleLarge?.copyWith(
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 2.h),

          // Base frequency control
          _buildFrequencySlider(
            context: context,
            label: 'Base Frequency',
            value: baseFrequency,
            min: 100.0,
            max: 400.0,
            divisions: 300,
            unit: 'Hz',
            onChanged: onBaseFrequencyChanged,
            theme: theme,
          ),
          SizedBox(height: 2.h),

          // Beat frequency control
          _buildFrequencySlider(
            context: context,
            label: 'Beat Frequency',
            value: beatFrequency,
            min: 0.5,
            max: 20.0,
            divisions: 195,
            unit: 'Hz',
            onChanged: onBeatFrequencyChanged,
            theme: theme,
          ),
        ],
      ),
    );
  }

  /// Build frequency slider with label and value display
  Widget _buildFrequencySlider({
    required BuildContext context,
    required String label,
    required double value,
    required double min,
    required double max,
    required int divisions,
    required String unit,
    required ValueChanged<double> onChanged,
    required ThemeData theme,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              label,
              style: theme.textTheme.titleMedium?.copyWith(
                color: theme.colorScheme.onSurface,
              ),
            ),
            Container(
              padding: EdgeInsets.symmetric(horizontal: 3.w, vertical: 0.5.h),
              decoration: BoxDecoration(
                color: theme.colorScheme.tertiary.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(
                '${value.toStringAsFixed(1)} $unit',
                style: AppTheme.getTechnicalTextStyle(
                  isLight: theme.brightness == Brightness.light,
                  fontSize: 14,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          ],
        ),
        SizedBox(height: 1.h),
        SliderTheme(
          data: SliderThemeData(
            activeTrackColor: theme.colorScheme.tertiary,
            inactiveTrackColor: theme.colorScheme.outline,
            thumbColor: theme.colorScheme.tertiary,
            overlayColor: theme.colorScheme.tertiary.withValues(alpha: 0.2),
            trackHeight: 4.0,
            thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 8.0),
            overlayShape: const RoundSliderOverlayShape(overlayRadius: 20.0),
          ),
          child: Slider(
            value: value,
            min: min,
            max: max,
            divisions: divisions,
            onChanged: onChanged,
          ),
        ),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              '$min $unit',
              style: theme.textTheme.bodySmall?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
            Text(
              '$max $unit',
              style: theme.textTheme.bodySmall?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ],
        ),
      ],
    );
  }
}
