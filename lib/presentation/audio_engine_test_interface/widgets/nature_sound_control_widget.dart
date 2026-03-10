import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';
import 'package:google_fonts/google_fonts.dart';

/// Nature Sound Control Widget
/// Provides ambient nature sound overlay selection and volume control
class NatureSoundControlWidget extends StatelessWidget {
  final String selectedSound;
  final double volume;
  final ValueChanged<String> onSoundChanged;
  final ValueChanged<double> onVolumeChanged;

  const NatureSoundControlWidget({
    super.key,
    required this.selectedSound,
    required this.volume,
    required this.onSoundChanged,
    required this.onVolumeChanged,
  });

  static const List<Map<String, String>> _soundOptions = [
    {'value': 'NONE', 'label': 'None'},
    {'value': 'WIND_CHIMES', 'label': 'Wind Chimes'},
    {'value': 'FOREST_BIRDSONG', 'label': 'Forest Birdsong'},
    {'value': 'RIVER_STREAM', 'label': 'River Stream'},
    {'value': 'BEACH_WAVES', 'label': 'Beach Waves'},
    {'value': 'LIGHT_RAIN', 'label': 'Light Rain'},
  ];

  static const Map<String, String> _soundIcons = {
    'NONE': '🔇',
    'WIND_CHIMES': '🎐',
    'FOREST_BIRDSONG': '🌿',
    'RIVER_STREAM': '💧',
    'BEACH_WAVES': '🌊',
    'LIGHT_RAIN': '🌧️',
  };

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final bool isActive = selectedSound != 'NONE';

    return Container(
      padding: EdgeInsets.all(3.w),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        borderRadius: BorderRadius.circular(12.0),
        border: Border.all(
          color: isActive
              ? theme.colorScheme.tertiary.withAlpha(153)
              : theme.colorScheme.outline.withAlpha(51),
          width: isActive ? 1.5 : 1.0,
        ),
        boxShadow: [
          BoxShadow(
            color: theme.shadowColor.withAlpha(20),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Row(
            children: [
              Container(
                padding: EdgeInsets.all(1.5.w),
                decoration: BoxDecoration(
                  color: isActive
                      ? theme.colorScheme.tertiary.withAlpha(38)
                      : theme.colorScheme.surfaceVariant,
                  borderRadius: BorderRadius.circular(8.0),
                ),
                child: Text(
                  _soundIcons[selectedSound] ?? '🔇',
                  style: TextStyle(fontSize: 14.sp),
                ),
              ),
              SizedBox(width: 2.w),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Nature Sound Overlay',
                      style: GoogleFonts.inter(
                        fontSize: 13.sp,
                        fontWeight: FontWeight.w600,
                        color: theme.colorScheme.onSurface,
                      ),
                    ),
                    Text(
                      'Ambient background layer',
                      style: GoogleFonts.inter(
                        fontSize: 10.sp,
                        color: theme.colorScheme.onSurface.withAlpha(128),
                      ),
                    ),
                  ],
                ),
              ),
              if (isActive)
                Container(
                  padding: EdgeInsets.symmetric(
                    horizontal: 2.w,
                    vertical: 0.5.h,
                  ),
                  decoration: BoxDecoration(
                    color: theme.colorScheme.tertiary.withAlpha(38),
                    borderRadius: BorderRadius.circular(20.0),
                  ),
                  child: Text(
                    'ACTIVE',
                    style: GoogleFonts.inter(
                      fontSize: 9.sp,
                      fontWeight: FontWeight.w700,
                      color: theme.colorScheme.tertiary,
                      letterSpacing: 0.5,
                    ),
                  ),
                ),
            ],
          ),
          SizedBox(height: 2.h),

          // Sound selector label
          Text(
            'Sound',
            style: GoogleFonts.inter(
              fontSize: 11.sp,
              fontWeight: FontWeight.w500,
              color: theme.colorScheme.onSurface.withAlpha(179),
            ),
          ),
          SizedBox(height: 0.8.h),

          // Dropdown
          Container(
            width: double.infinity,
            padding: EdgeInsets.symmetric(horizontal: 3.w, vertical: 0.5.h),
            decoration: BoxDecoration(
              color: theme.colorScheme.surfaceVariant.withAlpha(128),
              borderRadius: BorderRadius.circular(8.0),
              border: Border.all(
                color: theme.colorScheme.outline.withAlpha(77),
              ),
            ),
            child: DropdownButtonHideUnderline(
              child: DropdownButton<String>(
                value: selectedSound,
                isExpanded: true,
                dropdownColor: theme.colorScheme.surface,
                style: GoogleFonts.inter(
                  fontSize: 12.sp,
                  color: theme.colorScheme.onSurface,
                ),
                icon: Icon(
                  Icons.keyboard_arrow_down_rounded,
                  color: theme.colorScheme.onSurface.withAlpha(153),
                  size: 20,
                ),
                items: _soundOptions.map((option) {
                  return DropdownMenuItem<String>(
                    value: option['value'],
                    child: Row(
                      children: [
                        Text(
                          _soundIcons[option['value']] ?? '🔇',
                          style: TextStyle(fontSize: 13.sp),
                        ),
                        SizedBox(width: 2.w),
                        Text(
                          option['label']!,
                          style: GoogleFonts.inter(
                            fontSize: 12.sp,
                            color: theme.colorScheme.onSurface,
                          ),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                    ),
                  );
                }).toList(),
                onChanged: (value) {
                  if (value != null) onSoundChanged(value);
                },
              ),
            ),
          ),

          // Volume slider (only shown when a sound is active)
          if (isActive) ..._buildVolumeSection(context, theme),
        ],
      ),
    );
  }

  List<Widget> _buildVolumeSection(BuildContext context, ThemeData theme) {
    return [
      SizedBox(height: 2.h),
      Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            'Volume',
            style: GoogleFonts.inter(
              fontSize: 11.sp,
              fontWeight: FontWeight.w500,
              color: theme.colorScheme.onSurface.withAlpha(179),
            ),
          ),
          Text(
            '${(volume * 100).toInt()}%',
            style: GoogleFonts.inter(
              fontSize: 11.sp,
              fontWeight: FontWeight.w600,
              color: theme.colorScheme.tertiary,
            ),
          ),
        ],
      ),
      SizedBox(height: 0.5.h),
      SliderTheme(
        data: SliderTheme.of(context).copyWith(
          activeTrackColor: theme.colorScheme.tertiary,
          inactiveTrackColor: theme.colorScheme.tertiary.withAlpha(51),
          thumbColor: theme.colorScheme.tertiary,
          overlayColor: theme.colorScheme.tertiary.withAlpha(26),
          trackHeight: 3.0,
          thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 7.0),
        ),
        child: Slider(
          value: volume,
          min: 0.0,
          max: 1.0,
          onChanged: onVolumeChanged,
        ),
      ),
    ];
  }
}
