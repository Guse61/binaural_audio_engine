import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';

/// Premium Features Control Widget
/// Toggle controls for pad layer and psychoacoustic smoothing
class PremiumFeaturesControlWidget extends StatelessWidget {
  final bool padLayerEnabled;
  final bool psychoacousticSmoothing;
  final ValueChanged<bool> onPadLayerChanged;
  final ValueChanged<bool> onSmoothingChanged;

  const PremiumFeaturesControlWidget({
    super.key,
    required this.padLayerEnabled,
    required this.psychoacousticSmoothing,
    required this.onPadLayerChanged,
    required this.onSmoothingChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final accentColor = Colors.cyan.shade400;

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
          Text(
            'Premium Features',
            style: TextStyle(
              color: accentColor,
              fontSize: 14.sp,
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 1.5.h),
          // Ambient Pad Layer toggle
          Row(
            children: [
              Icon(
                Icons.layers,
                color: padLayerEnabled
                    ? accentColor
                    : theme.colorScheme.onSurfaceVariant,
                size: 16.sp,
              ),
              SizedBox(width: 2.w),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Ambient Pad Layer',
                      style: TextStyle(
                        color: padLayerEnabled
                            ? accentColor
                            : theme.colorScheme.onSurface,
                        fontSize: 13.sp,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    Text(
                      'Subtle evolving ambient texture',
                      style: TextStyle(
                        color: theme.colorScheme.onSurfaceVariant,
                        fontSize: 10.sp,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
              Switch(
                value: padLayerEnabled,
                onChanged: onPadLayerChanged,
                activeThumbColor: accentColor,
                activeTrackColor: accentColor.withAlpha(128),
              ),
            ],
          ),
          SizedBox(height: 1.5.h),
          Divider(color: theme.colorScheme.outline, height: 1),
          SizedBox(height: 1.5.h),
          // Psychoacoustic Smoothing toggle
          Row(
            children: [
              Icon(
                Icons.auto_fix_high,
                color: psychoacousticSmoothing
                    ? accentColor
                    : theme.colorScheme.onSurfaceVariant,
                size: 16.sp,
              ),
              SizedBox(width: 2.w),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Psychoacoustic Smoothing',
                      style: TextStyle(
                        color: psychoacousticSmoothing
                            ? accentColor
                            : theme.colorScheme.onSurface,
                        fontSize: 13.sp,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    Text(
                      'Equal-power curves, no zipper noise',
                      style: TextStyle(
                        color: theme.colorScheme.onSurfaceVariant,
                        fontSize: 10.sp,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
              Switch(
                value: psychoacousticSmoothing,
                onChanged: onSmoothingChanged,
                activeThumbColor: accentColor,
                activeTrackColor: accentColor.withAlpha(128),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
