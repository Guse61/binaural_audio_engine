import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';

/// Warmth Control Widget
/// Controls brown noise warmth EQ curve and texture density
class WarmthControlWidget extends StatelessWidget {
  final double warmth;
  final double texture;
  final ValueChanged<double> onWarmthChanged;
  final ValueChanged<double> onTextureChanged;

  const WarmthControlWidget({
    super.key,
    required this.warmth,
    required this.texture,
    required this.onWarmthChanged,
    required this.onTextureChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final warmColor = Colors.orange.shade400;

    return Container(
      padding: EdgeInsets.all(2.w),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        borderRadius: BorderRadius.circular(12.0),
        border: Border.all(color: warmColor.withAlpha(77)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          // Warmth control
          Row(
            children: [
              Icon(Icons.thermostat, color: warmColor, size: 16.sp),
              SizedBox(width: 2.w),
              Text(
                'Warmth',
                style: TextStyle(
                  color: warmColor,
                  fontSize: 14.sp,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const Spacer(),
              Container(
                padding: EdgeInsets.symmetric(horizontal: 2.w, vertical: 0.5.h),
                decoration: BoxDecoration(
                  color: warmColor.withAlpha(51),
                  borderRadius: BorderRadius.circular(8.0),
                ),
                child: Text(
                  '${(warmth * 100).toInt()}%',
                  style: TextStyle(
                    color: warmColor,
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
              activeTrackColor: warmColor,
              inactiveTrackColor: theme.colorScheme.outline,
              thumbColor: warmColor,
              overlayColor: warmColor.withAlpha(51),
              trackHeight: 4.0,
            ),
            child: Slider(
              value: warmth,
              min: 0.0,
              max: 1.0,
              divisions: 100,
              onChanged: onWarmthChanged,
            ),
          ),
          Text(
            'Low-pass shaping & harshness reduction (2-4kHz)',
            style: TextStyle(
              color: theme.colorScheme.onSurfaceVariant,
              fontSize: 11.sp,
            ),
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
          SizedBox(height: 1.5.h),
          // Texture control
          Row(
            children: [
              Icon(Icons.texture, color: warmColor, size: 16.sp),
              SizedBox(width: 2.w),
              Text(
                'Texture Density',
                style: TextStyle(
                  color: warmColor,
                  fontSize: 14.sp,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const Spacer(),
              Container(
                padding: EdgeInsets.symmetric(horizontal: 2.w, vertical: 0.5.h),
                decoration: BoxDecoration(
                  color: warmColor.withAlpha(51),
                  borderRadius: BorderRadius.circular(8.0),
                ),
                child: Text(
                  '${(texture * 100).toInt()}%',
                  style: TextStyle(
                    color: warmColor,
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
              activeTrackColor: warmColor,
              inactiveTrackColor: theme.colorScheme.outline,
              thumbColor: warmColor,
              overlayColor: warmColor.withAlpha(51),
              trackHeight: 4.0,
            ),
            child: Slider(
              value: texture,
              min: 0.0,
              max: 1.0,
              divisions: 100,
              onChanged: onTextureChanged,
            ),
          ),
          Text(
            'Brown noise character and density',
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
