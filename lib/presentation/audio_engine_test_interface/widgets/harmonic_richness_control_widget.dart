import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';

/// Harmonic Richness Control Widget
/// Controls the amplitude of harmonic overtones for warmer, more musical sound
class HarmonicRichnessControlWidget extends StatelessWidget {
  final double harmonicRichness;
  final ValueChanged<double> onHarmonicRichnessChanged;

  const HarmonicRichnessControlWidget({
    super.key,
    required this.harmonicRichness,
    required this.onHarmonicRichnessChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: EdgeInsets.all(2.w),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        borderRadius: BorderRadius.circular(12.0),
        border: Border.all(color: theme.colorScheme.tertiary.withAlpha(77)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            children: [
              Icon(
                Icons.graphic_eq,
                color: theme.colorScheme.tertiary,
                size: 16.sp,
              ),
              SizedBox(width: 2.w),
              Text(
                'Harmonic Richness',
                style: TextStyle(
                  color: theme.colorScheme.tertiary,
                  fontSize: 14.sp,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const Spacer(),
              Container(
                padding: EdgeInsets.symmetric(horizontal: 2.w, vertical: 0.5.h),
                decoration: BoxDecoration(
                  color: theme.colorScheme.tertiary.withAlpha(51),
                  borderRadius: BorderRadius.circular(8.0),
                ),
                child: Text(
                  '${(harmonicRichness * 100).toInt()}%',
                  style: TextStyle(
                    color: theme.colorScheme.tertiary,
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
              activeTrackColor: theme.colorScheme.tertiary,
              inactiveTrackColor: theme.colorScheme.outline,
              thumbColor: theme.colorScheme.tertiary,
              overlayColor: theme.colorScheme.tertiary.withAlpha(51),
              trackHeight: 4.0,
            ),
            child: Slider(
              value: harmonicRichness,
              min: 0.0,
              max: 1.0,
              divisions: 100,
              onChanged: onHarmonicRichnessChanged,
            ),
          ),
          Text(
            'Adds warm harmonic overtones (2nd, 3rd, 5th harmonics)',
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
