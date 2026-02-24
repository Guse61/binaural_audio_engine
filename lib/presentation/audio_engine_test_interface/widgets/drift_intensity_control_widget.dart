import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';

/// Drift Intensity Control Widget
/// Controls the amount of organic frequency drift for analog feel
class DriftIntensityControlWidget extends StatelessWidget {
  final double driftIntensity;
  final ValueChanged<double> onDriftIntensityChanged;

  const DriftIntensityControlWidget({
    super.key,
    required this.driftIntensity,
    required this.onDriftIntensityChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final accentColor = theme.colorScheme.secondary;

    return Container(
      padding: EdgeInsets.all(2.w),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        borderRadius: BorderRadius.circular(12.0),
        border: Border.all(color: accentColor.withAlpha(77)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            children: [
              Icon(Icons.waves, color: accentColor, size: 16.sp),
              SizedBox(width: 2.w),
              Text(
                'Micro Drift',
                style: TextStyle(
                  color: accentColor,
                  fontSize: 14.sp,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const Spacer(),
              Container(
                padding: EdgeInsets.symmetric(horizontal: 2.w, vertical: 0.5.h),
                decoration: BoxDecoration(
                  color: accentColor.withAlpha(51),
                  borderRadius: BorderRadius.circular(8.0),
                ),
                child: Text(
                  '${(driftIntensity * 100).toInt()}%',
                  style: TextStyle(
                    color: accentColor,
                    fontSize: 13.sp,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ),
          SizedBox(height: 1.h),
          SliderTheme(
            data: SliderThemeData(
              activeTrackColor: accentColor,
              inactiveTrackColor: theme.colorScheme.outline,
              thumbColor: accentColor,
              overlayColor: accentColor.withAlpha(51),
              trackHeight: 4.0,
            ),
            child: Slider(
              value: driftIntensity,
              min: 0.0,
              max: 1.0,
              divisions: 100,
              onChanged: onDriftIntensityChanged,
            ),
          ),
          Text(
            'Organic analog imperfection (±0.05-0.15 Hz drift)',
            style: TextStyle(
              color: theme.colorScheme.onSurfaceVariant,
              fontSize: 11.sp,
            ),
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    );
  }
}
