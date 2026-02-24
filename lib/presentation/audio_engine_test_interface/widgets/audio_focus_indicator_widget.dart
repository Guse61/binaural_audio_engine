import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';

import '../../../core/app_export.dart';
import '../../../theme/app_theme.dart';

/// Audio focus indicator widget for displaying audio session status
class AudioFocusIndicatorWidget extends StatelessWidget {
  final bool hasAudioFocus;

  const AudioFocusIndicatorWidget({super.key, required this.hasAudioFocus});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: EdgeInsets.symmetric(horizontal: 4.w, vertical: 1.h),
      decoration: BoxDecoration(
        color: hasAudioFocus
            ? AppTheme.successLight.withValues(alpha: 0.1)
            : AppTheme.warningLight.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(
          color: hasAudioFocus ? AppTheme.successLight : AppTheme.warningLight,
          width: 1,
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 8,
            height: 8,
            decoration: BoxDecoration(
              color: hasAudioFocus
                  ? AppTheme.successLight
                  : AppTheme.warningLight,
              shape: BoxShape.circle,
            ),
          ),
          SizedBox(width: 2.w),
          Text(
            hasAudioFocus ? 'Audio Focus Active' : 'Audio Focus Lost',
            style: theme.textTheme.bodyMedium?.copyWith(
              color: hasAudioFocus
                  ? AppTheme.successLight
                  : AppTheme.warningLight,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}
