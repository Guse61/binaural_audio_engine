import 'package:flutter/material.dart';
import 'package:sizer/sizer.dart';

/// Waveform visualization widget for real-time audio output display
class WaveformVisualizationWidget extends StatelessWidget {
  final List<double> waveformData;
  final bool isPlaying;

  const WaveformVisualizationWidget({
    super.key,
    required this.waveformData,
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
            'Waveform Visualization',
            style: theme.textTheme.titleLarge?.copyWith(
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 2.h),

          // Waveform display
          Container(
            height: 20.h,
            width: double.infinity,
            decoration: BoxDecoration(
              color: theme.brightness == Brightness.light
                  ? Colors.black.withValues(alpha: 0.05)
                  : Colors.white.withValues(alpha: 0.05),
              borderRadius: BorderRadius.circular(8),
            ),
            child: CustomPaint(
              painter: WaveformPainter(
                waveformData: waveformData,
                color: theme.colorScheme.tertiary,
                isPlaying: isPlaying,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Custom painter for waveform visualization
class WaveformPainter extends CustomPainter {
  final List<double> waveformData;
  final Color color;
  final bool isPlaying;

  WaveformPainter({
    required this.waveformData,
    required this.color,
    required this.isPlaying,
  });

  @override
  void paint(Canvas canvas, Size size) {
    if (!isPlaying || waveformData.isEmpty) {
      // Draw flat line when not playing
      final paint = Paint()
        ..color = color.withValues(alpha: 0.3)
        ..strokeWidth = 2.0
        ..style = PaintingStyle.stroke;

      canvas.drawLine(
        Offset(0, size.height / 2),
        Offset(size.width, size.height / 2),
        paint,
      );
      return;
    }

    final paint = Paint()
      ..color = color
      ..strokeWidth = 2.0
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;

    final path = Path();
    final stepWidth = size.width / (waveformData.length - 1);

    for (int i = 0; i < waveformData.length; i++) {
      final x = i * stepWidth;
      final y = size.height / 2 + (waveformData[i] - 0.5) * size.height * 0.8;

      if (i == 0) {
        path.moveTo(x, y);
      } else {
        path.lineTo(x, y);
      }
    }

    canvas.drawPath(path, paint);
  }

  @override
  bool shouldRepaint(WaveformPainter oldDelegate) {
    return oldDelegate.waveformData != waveformData ||
        oldDelegate.isPlaying != isPlaying;
  }
}
