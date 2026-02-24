import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';

import '../../../core/app_export.dart';
import '../../../theme/app_theme.dart';

/// Gain envelope control widget for attack and release time adjustment
class GainEnvelopeControlWidget extends StatelessWidget {
  final double attackTime;
  final double releaseTime;
  final ValueChanged<double> onAttackChanged;
  final ValueChanged<double> onReleaseChanged;

  const GainEnvelopeControlWidget({
    super.key,
    required this.attackTime,
    required this.releaseTime,
    required this.onAttackChanged,
    required this.onReleaseChanged,
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
            'Gain Envelope',
            style: theme.textTheme.titleLarge?.copyWith(
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 2.h),

          Row(
            children: [
              // Attack time control
              Expanded(
                child: _buildVerticalSlider(
                  context: context,
                  label: 'Attack',
                  value: attackTime,
                  min: 0.1,
                  max: 10.0,
                  onChanged: onAttackChanged,
                  theme: theme,
                ),
              ),
              SizedBox(width: 4.w),

              // Release time control
              Expanded(
                child: _buildVerticalSlider(
                  context: context,
                  label: 'Release',
                  value: releaseTime,
                  min: 0.1,
                  max: 10.0,
                  onChanged: onReleaseChanged,
                  theme: theme,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  /// Build vertical slider for envelope control
  Widget _buildVerticalSlider({
    required BuildContext context,
    required String label,
    required double value,
    required double min,
    required double max,
    required ValueChanged<double> onChanged,
    required ThemeData theme,
  }) {
    return Column(
      children: [
        Text(
          label,
          style: theme.textTheme.titleMedium?.copyWith(
            color: theme.colorScheme.onSurface,
          ),
        ),
        SizedBox(height: 1.h),
        Container(
          padding: EdgeInsets.symmetric(horizontal: 3.w, vertical: 0.5.h),
          decoration: BoxDecoration(
            color: theme.colorScheme.tertiary.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Text(
            '${value.toStringAsFixed(1)}s',
            style: AppTheme.getTechnicalTextStyle(
              isLight: theme.brightness == Brightness.light,
              fontSize: 14,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
        SizedBox(height: 1.h),
        SizedBox(
          height: 15.h,
          child: RotatedBox(
            quarterTurns: 3,
            child: SliderTheme(
              data: SliderThemeData(
                activeTrackColor: theme.colorScheme.tertiary,
                inactiveTrackColor: theme.colorScheme.outline,
                thumbColor: theme.colorScheme.tertiary,
                overlayColor: theme.colorScheme.tertiary.withValues(alpha: 0.2),
                trackHeight: 4.0,
                thumbShape: const RoundSliderThumbShape(
                  enabledThumbRadius: 8.0,
                ),
                overlayShape: const RoundSliderOverlayShape(
                  overlayRadius: 20.0,
                ),
              ),
              child: Slider(
                value: value,
                min: min,
                max: max,
                divisions: 99,
                onChanged: onChanged,
              ),
            ),
          ),
        ),
      ],
    );
  }
}
