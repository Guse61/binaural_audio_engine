import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';

import '../../../core/app_export.dart';

/// Emotional Mode Selector Widget
/// Allows selection between Sleep, Calm, and Focus modes
class EmotionalModeSelectorWidget extends StatelessWidget {
  final int selectedMode;
  final Function(int) onModeChanged;

  const EmotionalModeSelectorWidget({
    super.key,
    required this.selectedMode,
    required this.onModeChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.all(2.w),
      decoration: BoxDecoration(
        color: Colors.grey[900],
        borderRadius: BorderRadius.circular(12.0),
        border: Border.all(color: Colors.teal[700]!, width: 1),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            'Emotional Mode',
            style: TextStyle(
              fontSize: 14.sp,
              fontWeight: FontWeight.bold,
              color: Colors.teal[300],
            ),
          ),
          SizedBox(height: 1.h),
          Row(
            children: [
              Expanded(
                child: _buildModeButton(
                  label: 'Sleep',
                  icon: Icons.nightlight_round,
                  mode: 0,
                  color: Colors.indigo[400]!,
                ),
              ),
              SizedBox(width: 2.w),
              Expanded(
                child: _buildModeButton(
                  label: 'Calm',
                  icon: Icons.spa,
                  mode: 1,
                  color: Colors.teal[400]!,
                ),
              ),
              SizedBox(width: 2.w),
              Expanded(
                child: _buildModeButton(
                  label: 'Focus',
                  icon: Icons.center_focus_strong,
                  mode: 2,
                  color: Colors.amber[400]!,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildModeButton({
    required String label,
    required IconData icon,
    required int mode,
    required Color color,
  }) {
    final isSelected = selectedMode == mode;

    return GestureDetector(
      onTap: () => onModeChanged(mode),
      child: Container(
        padding: EdgeInsets.symmetric(vertical: 1.h, horizontal: 1.w),
        decoration: BoxDecoration(
          color: isSelected ? color.withAlpha(77) : Colors.grey[850],
          borderRadius: BorderRadius.circular(8.0),
          border: Border.all(
            color: isSelected ? color : Colors.grey[700]!,
            width: isSelected ? 2 : 1,
          ),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, color: isSelected ? color : Colors.white54, size: 20.sp),
            SizedBox(height: 0.5.h),
            Text(
              label,
              style: TextStyle(
                fontSize: 10.sp,
                color: isSelected ? color : Colors.white54,
                fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
              ),
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ),
      ),
    );
  }
}
