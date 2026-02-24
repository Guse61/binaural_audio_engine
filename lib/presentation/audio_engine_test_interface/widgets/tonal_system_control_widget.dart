import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';

import '../../../core/app_export.dart';

/// Tonal System Control Widget
/// Controls root note and tuning reference for generative harmonic engine
class TonalSystemControlWidget extends StatelessWidget {
  final double rootNote;
  final bool useTuning432;
  final Function(double) onRootNoteChanged;
  final Function(bool) onTuningReferenceChanged;

  const TonalSystemControlWidget({
    super.key,
    required this.rootNote,
    required this.useTuning432,
    required this.onRootNoteChanged,
    required this.onTuningReferenceChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.all(2.w),
      decoration: BoxDecoration(
        color: Colors.grey[900],
        borderRadius: BorderRadius.circular(12.0),
        border: Border.all(color: Colors.blue[700]!, width: 1),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            'Tonal System',
            style: TextStyle(
              fontSize: 14.sp,
              fontWeight: FontWeight.bold,
              color: Colors.blue[300],
            ),
          ),
          SizedBox(height: 1.h),

          // Root Note Selector
          Row(
            children: [
              Expanded(
                child: Text(
                  'Root Note',
                  style: TextStyle(fontSize: 12.sp, color: Colors.white70),
                ),
              ),
              DropdownButton<double>(
                value: rootNote,
                dropdownColor: Colors.grey[850],
                style: TextStyle(fontSize: 11.sp, color: Colors.white),
                items: [
                  DropdownMenuItem(value: 65.41, child: Text('C2 (65.41 Hz)')),
                  DropdownMenuItem(value: 73.42, child: Text('D2 (73.42 Hz)')),
                  DropdownMenuItem(value: 82.41, child: Text('E2 (82.41 Hz)')),
                  DropdownMenuItem(value: 110.0, child: Text('A2 (110.0 Hz)')),
                ],
                onChanged: (value) {
                  if (value != null) onRootNoteChanged(value);
                },
              ),
            ],
          ),
          SizedBox(height: 1.h),

          // Tuning Reference Selector
          Row(
            children: [
              Expanded(
                child: Text(
                  'Tuning Reference',
                  style: TextStyle(fontSize: 12.sp, color: Colors.white70),
                ),
              ),
              Switch(
                value: useTuning432,
                activeColor: Colors.blue[400],
                onChanged: onTuningReferenceChanged,
              ),
              Text(
                useTuning432 ? '432 Hz' : '440 Hz',
                style: TextStyle(fontSize: 11.sp, color: Colors.white),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
