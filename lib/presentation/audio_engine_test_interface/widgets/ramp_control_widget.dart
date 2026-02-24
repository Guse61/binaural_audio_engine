import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';

import '../../../core/app_export.dart';
import '../../../theme/app_theme.dart';

/// Ramp control widget for frequency transition configuration
class RampControlWidget extends StatelessWidget {
  final double duration;
  final bool isLinearMode;
  final ValueChanged<double> onDurationChanged;
  final ValueChanged<bool> onModeToggle;
  final VoidCallback onExecuteRamp;
  final bool isPlaying;

  const RampControlWidget({
    super.key,
    required this.duration,
    required this.isLinearMode,
    required this.onDurationChanged,
    required this.onModeToggle,
    required this.onExecuteRamp,
    required this.isPlaying,
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
            'Frequency Ramp',
            style: theme.textTheme.titleLarge?.copyWith(
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 2.h),

          // Duration control
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Duration',
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
                  '${duration.toInt()}s',
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
              value: duration,
              min: 1.0,
              max: 120.0,
              divisions: 119,
              onChanged: onDurationChanged,
            ),
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                '1s',
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              Text(
                '120s',
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
            ],
          ),
          SizedBox(height: 2.h),

          // Ramp mode selector
          Text(
            'Ramp Mode',
            style: theme.textTheme.titleMedium?.copyWith(
              color: theme.colorScheme.onSurface,
            ),
          ),
          SizedBox(height: 1.h),
          Row(
            children: [
              Expanded(
                child: _buildModeButton(
                  context: context,
                  label: 'Linear',
                  isSelected: isLinearMode,
                  onTap: () => onModeToggle(true),
                  theme: theme,
                ),
              ),
              SizedBox(width: 2.w),
              Expanded(
                child: _buildModeButton(
                  context: context,
                  label: 'Exponential',
                  isSelected: !isLinearMode,
                  onTap: () => onModeToggle(false),
                  theme: theme,
                ),
              ),
            ],
          ),
          SizedBox(height: 2.h),

          // Execute ramp button
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: isPlaying ? onExecuteRamp : null,
              style: ElevatedButton.styleFrom(
                backgroundColor: theme.colorScheme.tertiary,
                padding: EdgeInsets.symmetric(vertical: 2.h),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
              child: Text(
                'Execute Ramp',
                style: theme.textTheme.titleMedium?.copyWith(
                  color: theme.colorScheme.onPrimary,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  /// Build mode selection button
  Widget _buildModeButton({
    required BuildContext context,
    required String label,
    required bool isSelected,
    required VoidCallback onTap,
    required ThemeData theme,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: EdgeInsets.symmetric(vertical: 1.5.h),
        decoration: BoxDecoration(
          color: isSelected
              ? theme.colorScheme.tertiary
              : theme.colorScheme.surface,
          border: Border.all(
            color: isSelected
                ? theme.colorScheme.tertiary
                : theme.colorScheme.outline,
            width: 2,
          ),
          borderRadius: BorderRadius.circular(8),
        ),
        child: Center(
          child: Text(
            label,
            style: theme.textTheme.titleSmall?.copyWith(
              color: isSelected
                  ? theme.colorScheme.onPrimary
                  : theme.colorScheme.onSurface,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
      ),
    );
  }
}
