import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';

import '../../../core/app_export.dart';
import '../../../theme/app_theme.dart';

/// Performance indicators widget for CPU usage and latency monitoring
class PerformanceIndicatorsWidget extends StatelessWidget {
  final double cpuUsage;
  final double latency;

  const PerformanceIndicatorsWidget({
    super.key,
    required this.cpuUsage,
    required this.latency,
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
            'Performance Metrics',
            style: theme.textTheme.titleLarge?.copyWith(
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 2.h),

          // CPU usage indicator
          _buildMetricIndicator(
            context: context,
            label: 'CPU Usage',
            value: cpuUsage,
            maxValue: 100.0,
            unit: '%',
            theme: theme,
          ),
          SizedBox(height: 2.h),

          // Latency indicator
          _buildMetricIndicator(
            context: context,
            label: 'Latency',
            value: latency,
            maxValue: 50.0,
            unit: 'ms',
            theme: theme,
          ),
        ],
      ),
    );
  }

  /// Build metric indicator with progress bar
  Widget _buildMetricIndicator({
    required BuildContext context,
    required String label,
    required double value,
    required double maxValue,
    required String unit,
    required ThemeData theme,
  }) {
    final percentage = (value / maxValue).clamp(0.0, 1.0);
    final Color indicatorColor = _getIndicatorColor(percentage, theme);

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
                color: indicatorColor.withValues(alpha: 0.1),
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
        ClipRRect(
          borderRadius: BorderRadius.circular(4),
          child: LinearProgressIndicator(
            value: percentage,
            backgroundColor: theme.colorScheme.outline,
            valueColor: AlwaysStoppedAnimation<Color>(indicatorColor),
            minHeight: 8,
          ),
        ),
      ],
    );
  }

  /// Get indicator color based on performance level
  Color _getIndicatorColor(double percentage, ThemeData theme) {
    if (percentage < 0.5) {
      return AppTheme.successLight;
    } else if (percentage < 0.8) {
      return AppTheme.warningLight;
    } else {
      return theme.colorScheme.error;
    }
  }
}
