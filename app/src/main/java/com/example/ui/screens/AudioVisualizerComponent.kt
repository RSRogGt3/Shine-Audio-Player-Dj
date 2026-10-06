package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.LocalMusicViewModel
import kotlin.math.*

@Composable
fun AudioVisualizerComponent(
    viewModel: LocalMusicViewModel,
    primaryColor: Color,
    surfaceColor: Color,
    modifier: Modifier = Modifier
) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val frequencyData by viewModel.frequencyData192kHz.collectAsState()
    val waveform by viewModel.timeDomainWaveform.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val beatLevel by viewModel.beatLevel.collectAsState()
    val isAiBassBoostEnabled by viewModel.isAiBassBoostEnabled.collectAsState()
    val aiBassBoostLevel by viewModel.aiBassBoostLevel.collectAsState()
    val currentPeakFreq by viewModel.currentPeakFreq.collectAsState()
    val visualizerMode by viewModel.visualizerMode.collectAsState()
    val visualizerSensitivity by viewModel.visualizerSensitivity.collectAsState()
    val isHiRes192kHzEnabled by viewModel.isHiRes192kHzEnabled.collectAsState()
    val sampleRateLabel by viewModel.sampleRateLabel.collectAsState()

    var showControls by remember { mutableStateOf(false) }

    // Memory for peak-hold gravity falloff across frames
    var peakLevels by remember { mutableStateOf(FloatArray(48)) }
    LaunchedEffect(frequencyData) {
        val newPeaks = peakLevels.copyOf(48)
        for (i in 0 until 48) {
            val cur = if (i < frequencyData.size) frequencyData[i] * visualizerSensitivity else 0f
            if (cur > newPeaks[i]) {
                newPeaks[i] = cur.coerceIn(0f, 1f)
            } else {
                newPeaks[i] = (newPeaks[i] - 0.035f).coerceAtLeast(0f)
            }
        }
        peakLevels = newPeaks
    }

    val glowAlpha by animateFloatAsState(
        targetValue = if (isPlaying && isAiBassBoostEnabled) (0.4f + aiBassBoostLevel * 0.5f).coerceIn(0.2f, 0.95f) else 0.15f,
        animationSpec = tween(120),
        label = "bass_glow"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("audio_visualizer_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor.copy(alpha = 0.85f)
        ),
        border = BorderStroke(
            width = if (isAiBassBoostEnabled && isPlaying) 1.5.dp else 1.dp,
            color = if (isAiBassBoostEnabled && isPlaying) {
                Color(0xFFFF1744).copy(alpha = glowAlpha)
            } else {
                primaryColor.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Bar: Visualizer Title, Hi-Res Badge, Peak Hz, Controls Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (isPlaying) Color(0xFF00E676) else Color.Gray,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "ECHTZEIT-AUDIO-VISUALIZER",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 192 kHz Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isHiRes192kHzEnabled) Color(0xFF7C4DFF).copy(alpha = 0.25f) else Color.DarkGray.copy(alpha = 0.4f),
                        border = BorderStroke(
                            1.dp,
                            if (isHiRes192kHzEnabled) Color(0xFFB388FF) else Color.Gray
                        ),
                        modifier = Modifier.clickable { viewModel.toggleHiRes192kHz() }
                    ) {
                        Text(
                            text = if (isHiRes192kHzEnabled) "192 kHz Hi-Res" else "48 kHz",
                            color = if (isHiRes192kHzEnabled) Color(0xFFE040FB) else Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Settings toggle button
                    IconButton(
                        onClick = { showControls = !showControls },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (showControls) Icons.Filled.Close else Icons.Filled.Tune,
                            contentDescription = "Visualizer Einstellungen",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Real-time Status Badges (Peak Hz, AI Bass Impact, Sample Mode)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Peak frequency tag
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1E1E28),
                        border = BorderStroke(0.5.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Peak: $currentPeakFreq Hz ${if (currentPeakFreq < 120) "• Sub-Bass" else if (currentPeakFreq < 2000) "• Mids" else "• Treble"}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (isAiBassBoostEnabled) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFF1744).copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, Color(0xFFFF1744))
                        ) {
                            Text(
                                text = "AI BASS +${(aiBassBoostLevel * 100).toInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = when (visualizerMode) {
                        "WAVE_CURVE" -> "Oszilloskop-Kurve"
                        "STUDIO_192KHZ" -> "192 kHz Spektrum"
                        "RADIAL" -> "Radial-Kreis"
                        else -> "Frequenzbänder"
                    },
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }

            // Collapsible Controls Panel
            AnimatedVisibility(visible = showControls) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF14131C), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Visualizer-Modus:",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VisualizerModeChip(
                            label = "Bänder",
                            icon = Icons.Filled.Equalizer,
                            isSelected = visualizerMode == "SPECTRUM_BARS",
                            onClick = { viewModel.setVisualizerMode("SPECTRUM_BARS") },
                            modifier = Modifier.weight(1f)
                        )
                        VisualizerModeChip(
                            label = "Kurve",
                            icon = Icons.Filled.GraphicEq,
                            isSelected = visualizerMode == "WAVE_CURVE",
                            onClick = { viewModel.setVisualizerMode("WAVE_CURVE") },
                            modifier = Modifier.weight(1f)
                        )
                        VisualizerModeChip(
                            label = "192 kHz",
                            icon = Icons.Filled.AllInclusive,
                            isSelected = visualizerMode == "STUDIO_192KHZ",
                            onClick = { viewModel.setVisualizerMode("STUDIO_192KHZ") },
                            modifier = Modifier.weight(1f)
                        )
                        VisualizerModeChip(
                            label = "Radial",
                            icon = Icons.Filled.DonutLarge,
                            isSelected = visualizerMode == "RADIAL",
                            onClick = { viewModel.setVisualizerMode("RADIAL") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Empfindlichkeit: ${String.format("%.1fx", visualizerSensitivity)}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Slider(
                            value = visualizerSensitivity,
                            onValueChange = { viewModel.setVisualizerSensitivity(it) },
                            valueRange = 0.5f..2.5f,
                            modifier = Modifier.width(180.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = primaryColor,
                                activeTrackColor = primaryColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // THE REAL-TIME CANVAS VISUALIZER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF09090F))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("visualizer_canvas")
                ) {
                    val w = size.width
                    val h = size.height

                    // Subtle background grid
                    drawVisualizerGrid(w, h)

                    when (visualizerMode) {
                        "WAVE_CURVE" -> {
                            drawWaveformCurve(
                                waveform = waveform,
                                w = w,
                                h = h,
                                primaryColor = primaryColor,
                                isAiBassBoostEnabled = isAiBassBoostEnabled,
                                aiBassBoostLevel = aiBassBoostLevel,
                                isPlaying = isPlaying
                            )
                        }
                        "STUDIO_192KHZ" -> {
                            drawStudio192kHzSpectrum(
                                frequencyData = frequencyData,
                                peaks = peakLevels,
                                w = w,
                                h = h,
                                sensitivity = visualizerSensitivity,
                                isAiBassBoostEnabled = isAiBassBoostEnabled,
                                aiBassBoostLevel = aiBassBoostLevel,
                                isPlaying = isPlaying,
                                primaryColor = primaryColor
                            )
                        }
                        "RADIAL" -> {
                            drawRadialVisualizer(
                                frequencyData = frequencyData,
                                w = w,
                                h = h,
                                primaryColor = primaryColor,
                                isAiBassBoostEnabled = isAiBassBoostEnabled,
                                aiBassBoostLevel = aiBassBoostLevel,
                                isPlaying = isPlaying,
                                beatLevel = beatLevel
                            )
                        }
                        else -> {
                            // Default: SPECTRUM_BARS
                            drawSpectrumBars(
                                frequencyData = frequencyData,
                                peaks = peakLevels,
                                w = w,
                                h = h,
                                sensitivity = visualizerSensitivity,
                                isAiBassBoostEnabled = isAiBassBoostEnabled,
                                aiBassBoostLevel = aiBassBoostLevel,
                                isPlaying = isPlaying,
                                primaryColor = primaryColor
                            )
                        }
                    }
                }
            }

            // Frequency Scale Footer Markers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val scaleLabels = if (isHiRes192kHzEnabled) {
                    listOf("20Hz", "60Hz", "250Hz", "1kHz", "4kHz", "16kHz", "48kHz", "96kHz", "192kHz")
                } else {
                    listOf("20Hz", "100Hz", "300Hz", "1kHz", "3kHz", "7kHz", "12kHz", "16kHz", "20kHz")
                }
                scaleLabels.forEach { label ->
                    val isSubBass = label == "20Hz" || label == "60Hz"
                    val isUltraHiRes = label == "96kHz" || label == "192kHz"
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = when {
                            isSubBass && isAiBassBoostEnabled -> Color(0xFFFF5252)
                            isUltraHiRes -> Color(0xFFB388FF)
                            else -> Color.DarkGray
                        },
                        fontWeight = if (isSubBass && isAiBassBoostEnabled) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun VisualizerModeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1B1B26),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF00E5FF) else Color(0xFF2E2E3E)
        ),
        modifier = modifier.height(34.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color(0xFF00E5FF) else Color.Gray,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.Gray
            )
        }
    }
}

// -------------------------------------------------------------
// Canvas API Drawing Helper Functions
// -------------------------------------------------------------

private fun DrawScope.drawVisualizerGrid(w: Float, h: Float) {
    val gridColor = Color(0xFF1E1E2C).copy(alpha = 0.45f)
    val stroke = Stroke(width = 0.8f)
    // Horizontal dB lines
    for (i in 1..4) {
        val y = h * (i / 5f)
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 0.8f
        )
    }
    // Vertical frequency band division lines
    val dividers = 8
    for (i in 1 until dividers) {
        val x = w * (i.toFloat() / dividers)
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 0.8f
        )
    }
}

/**
 * Mode 1: Multi-band Neon Spectrum Bars with Peak-Hold caps & AI Bass glow
 */
private fun DrawScope.drawSpectrumBars(
    frequencyData: FloatArray,
    peaks: FloatArray,
    w: Float,
    h: Float,
    sensitivity: Float,
    isAiBassBoostEnabled: Boolean,
    aiBassBoostLevel: Float,
    isPlaying: Boolean,
    primaryColor: Color
) {
    val barCount = 36
    val totalSpacing = w * 0.15f
    val barWidth = (w - totalSpacing) / barCount
    val spacing = totalSpacing / (barCount + 1)

    // Sub-bass glow aura on background
    if (isPlaying && isAiBassBoostEnabled) {
        val bassAreaWidth = (barWidth + spacing) * 8
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFFF1744).copy(alpha = 0.25f + aiBassBoostLevel * 0.3f),
                    Color(0xFFFF5252).copy(alpha = 0.10f),
                    Color.Transparent
                ),
                startX = 0f,
                endX = bassAreaWidth
            ),
            topLeft = Offset(0f, 0f),
            size = Size(bassAreaWidth, h)
        )
    }

    for (i in 0 until barCount) {
        val x = spacing + i * (barWidth + spacing)
        val dataIdx = (i * frequencyData.size / barCount).coerceIn(0, frequencyData.size - 1)
        val rawAmp = if (isPlaying) (frequencyData[dataIdx] * sensitivity).coerceIn(0.04f, 1f) else 0.04f
        val barHeight = (rawAmp * (h - 8.dp.toPx())).coerceAtLeast(3.dp.toPx())
        val peakAmp = if (isPlaying) peaks.getOrElse(dataIdx) { rawAmp }.coerceIn(rawAmp, 1f) else 0.04f
        val peakY = h - (peakAmp * (h - 8.dp.toPx())).coerceAtLeast(3.dp.toPx())

        // Color grading according to frequency position (Sub-Bass -> Mids -> Treble -> 192k Ultra)
        val isSubBass = i < 6
        val isLowMid = i in 6..14
        val isPresence = i in 15..26
        val isUltraHiRes = i >= 27

        val barBrush = Brush.verticalGradient(
            colors = when {
                isSubBass && isAiBassBoostEnabled -> listOf(
                    Color(0xFFFF1744),
                    Color(0xFFFF5252),
                    Color(0xFFD50000)
                )
                isSubBass -> listOf(
                    Color(0xFFFF6D00),
                    Color(0xFFFF9100),
                    Color(0xFFFFAB40)
                )
                isLowMid -> listOf(
                    Color(0xFF00E5FF),
                    Color(0xFF00B0FF),
                    Color(0xFF0091EA)
                )
                isPresence -> listOf(
                    Color(0xFF00E676),
                    Color(0xFF76FF03),
                    Color(0xFF64DD17)
                )
                else -> listOf(
                    Color(0xFFE040FB),
                    Color(0xFFD500F9),
                    Color(0xFFAA00FF)
                )
            },
            startY = h - barHeight,
            endY = h
        )

        // Draw the bar
        drawRoundRect(
            brush = barBrush,
            topLeft = Offset(x, h - barHeight),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barWidth / 3f, barWidth / 3f)
        )

        // Draw peak-hold cap
        if (isPlaying && peakAmp > 0.08f) {
            val capColor = if (isSubBass && isAiBassBoostEnabled) Color(0xFFFF8A80) else Color.White
            drawRoundRect(
                color = capColor,
                topLeft = Offset(x, peakY - 2.dp.toPx()),
                size = Size(barWidth, 2.dp.toPx()),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
        }
    }
}

/**
 * Mode 2: Oscilloscope Waveform & Spline Glow Curve
 */
private fun DrawScope.drawWaveformCurve(
    waveform: FloatArray,
    w: Float,
    h: Float,
    primaryColor: Color,
    isAiBassBoostEnabled: Boolean,
    aiBassBoostLevel: Float,
    isPlaying: Boolean
) {
    if (waveform.isEmpty()) return

    val centerY = h / 2f
    val amplitudeScale = (h / 2f) * 0.85f

    val path = Path()
    val fillPath = Path()
    fillPath.moveTo(0f, h)

    val stepX = w / (waveform.size - 1).coerceAtLeast(1)

    for (i in waveform.indices) {
        val rawVal = if (isPlaying) waveform[i] else 0f
        val boostedVal = if (isAiBassBoostEnabled && isPlaying) {
            rawVal * (1f + aiBassBoostLevel * 0.5f)
        } else rawVal
        val x = i * stepX
        val y = (centerY - (boostedVal * amplitudeScale)).coerceIn(4f, h - 4f)

        if (i == 0) {
            path.moveTo(x, y)
            fillPath.lineTo(x, y)
        } else {
            val prevX = (i - 1) * stepX
            val prevRaw = if (isPlaying) waveform[i - 1] else 0f
            val prevBoosted = if (isAiBassBoostEnabled && isPlaying) {
                prevRaw * (1f + aiBassBoostLevel * 0.5f)
            } else prevRaw
            val prevY = (centerY - (prevBoosted * amplitudeScale)).coerceIn(4f, h - 4f)

            val cpX1 = prevX + (x - prevX) / 2f
            val cpY1 = prevY
            val cpX2 = prevX + (x - prevX) / 2f
            val cpY2 = y
            path.cubicTo(cpX1, cpY1, cpX2, cpY2, x, y)
            fillPath.cubicTo(cpX1, cpY1, cpX2, cpY2, x, y)
        }
    }

    fillPath.lineTo(w, h)
    fillPath.close()

    // Gradient fill under curve
    val fillBrush = Brush.verticalGradient(
        colors = listOf(
            (if (isAiBassBoostEnabled) Color(0xFFFF1744) else primaryColor).copy(alpha = 0.35f),
            Color(0xFF00E5FF).copy(alpha = 0.15f),
            Color.Transparent
        ),
        startY = 0f,
        endY = h
    )
    drawPath(path = fillPath, brush = fillBrush, style = Fill)

    // Glowing main curve stroke
    val strokeBrush = Brush.horizontalGradient(
        colors = listOf(
            if (isAiBassBoostEnabled) Color(0xFFFF1744) else Color(0xFFFF6D00),
            Color(0xFF00E5FF),
            Color(0xFFE040FB),
            Color(0xFFB388FF)
        )
    )

    // Outer glow
    drawPath(
        path = path,
        brush = strokeBrush,
        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        alpha = 0.35f
    )
    // Sharp core line
    drawPath(
        path = path,
        brush = strokeBrush,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        alpha = 1.0f
    )
}

/**
 * Mode 3: High-Resolution 192 kHz Studio Spectrogram
 */
private fun DrawScope.drawStudio192kHzSpectrum(
    frequencyData: FloatArray,
    peaks: FloatArray,
    w: Float,
    h: Float,
    sensitivity: Float,
    isAiBassBoostEnabled: Boolean,
    aiBassBoostLevel: Float,
    isPlaying: Boolean,
    primaryColor: Color
) {
    // 192 kHz Studio Frequency Zone Dividers
    // Zones: Sub-Bass (0..0.12), Bass (0.12..0.25), Mids (0.25..0.55), Highs (0.55..0.75), 192k Studio (0.75..1.0)
    val subBassEnd = w * 0.15f
    val bassEnd = w * 0.30f
    val midsEnd = w * 0.60f
    val highsEnd = w * 0.80f

    // Draw Sub-Bass Boost Highlight
    if (isPlaying && isAiBassBoostEnabled) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF1744).copy(alpha = 0.35f * (0.5f + aiBassBoostLevel * 0.5f)),
                    Color.Transparent
                )
            ),
            topLeft = Offset(0f, 0f),
            size = Size(subBassEnd, h)
        )
    }

    // Draw continuous frequency spline
    val path = Path()
    val fillPath = Path()
    fillPath.moveTo(0f, h)

    val count = frequencyData.size.coerceAtLeast(2)
    val stepX = w / (count - 1)

    for (i in 0 until count) {
        val rawAmp = if (isPlaying) (frequencyData[i] * sensitivity).coerceIn(0.04f, 1f) else 0.04f
        val x = i * stepX
        val y = h - (rawAmp * (h - 10.dp.toPx())).coerceAtLeast(4.dp.toPx())

        if (i == 0) {
            path.moveTo(x, y)
            fillPath.lineTo(x, y)
        } else {
            val prevX = (i - 1) * stepX
            val prevAmp = if (isPlaying) (frequencyData[i - 1] * sensitivity).coerceIn(0.04f, 1f) else 0.04f
            val prevY = h - (prevAmp * (h - 10.dp.toPx())).coerceAtLeast(4.dp.toPx())

            val cpX1 = prevX + (x - prevX) / 2f
            val cpY1 = prevY
            val cpX2 = prevX + (x - prevX) / 2f
            val cpY2 = y
            path.cubicTo(cpX1, cpY1, cpX2, cpY2, x, y)
            fillPath.cubicTo(cpX1, cpY1, cpX2, cpY2, x, y)
        }
    }

    fillPath.lineTo(w, h)
    fillPath.close()

    // Gradient fill
    val fillBrush = Brush.horizontalGradient(
        colors = listOf(
            if (isAiBassBoostEnabled) Color(0xFFFF1744).copy(alpha = 0.45f) else Color(0xFFFF6D00).copy(alpha = 0.3f),
            Color(0xFF00E5FF).copy(alpha = 0.3f),
            Color(0xFF76FF03).copy(alpha = 0.25f),
            Color(0xFFE040FB).copy(alpha = 0.35f),
            Color(0xFFB388FF).copy(alpha = 0.4f)
        )
    )
    drawPath(fillPath, fillBrush, style = Fill)

    // Neon Contour
    val lineBrush = Brush.horizontalGradient(
        colors = listOf(
            if (isAiBassBoostEnabled) Color(0xFFFF1744) else Color(0xFFFF9100),
            Color(0xFF00E5FF),
            Color(0xFF00E676),
            Color(0xFFE040FB),
            Color(0xFFB388FF)
        )
    )
    drawPath(path, lineBrush, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

    // Zone labels markers
    val zoneMarkers = listOf(
        Pair(0f, "SUB 20-60Hz"),
        Pair(subBassEnd, "BASS 60-250Hz"),
        Pair(bassEnd, "MID 250-4k"),
        Pair(midsEnd, "PRESENCE 4k-20k"),
        Pair(highsEnd, "192k STUDIO")
    )
    for (marker in zoneMarkers) {
        drawLine(
            color = Color(0xFF33334A),
            start = Offset(marker.first, 0f),
            end = Offset(marker.first, h),
            strokeWidth = 1f
        )
    }
}

/**
 * Mode 4: Radial Cosmic Visualizer
 */
private fun DrawScope.drawRadialVisualizer(
    frequencyData: FloatArray,
    w: Float,
    h: Float,
    primaryColor: Color,
    isAiBassBoostEnabled: Boolean,
    aiBassBoostLevel: Float,
    isPlaying: Boolean,
    beatLevel: Float
) {
    val center = Offset(w / 2f, h / 2f)
    val baseRadius = (min(w, h) / 4f) * (1f + (if (isAiBassBoostEnabled && isPlaying) aiBassBoostLevel * 0.25f else 0f))
    val numSpikes = 40

    // Center disc with AI Bass pulse
    val centerColor = if (isAiBassBoostEnabled && isPlaying) {
        Color(0xFFFF1744).copy(alpha = 0.35f + aiBassBoostLevel * 0.4f)
    } else {
        primaryColor.copy(alpha = 0.25f)
    }
    drawCircle(color = centerColor, radius = baseRadius, center = center)
    drawCircle(
        color = if (isAiBassBoostEnabled && isPlaying) Color(0xFFFF1744) else primaryColor,
        radius = baseRadius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )

    for (i in 0 until numSpikes) {
        val angleRad = (i * 360f / numSpikes) * (PI / 180f).toFloat()
        val dataIdx = (i * frequencyData.size / numSpikes).coerceIn(0, frequencyData.size - 1)
        val amp = if (isPlaying) frequencyData[dataIdx].coerceIn(0.05f, 1f) else 0.05f
        val spikeLength = amp * 35.dp.toPx()

        val startX = center.x + baseRadius * cos(angleRad)
        val startY = center.y + baseRadius * sin(angleRad)
        val endX = center.x + (baseRadius + spikeLength) * cos(angleRad)
        val endY = center.y + (baseRadius + spikeLength) * sin(angleRad)

        val spikeColor = if (i < 8 && isAiBassBoostEnabled) {
            Color(0xFFFF1744)
        } else if (i < 20) {
            Color(0xFF00E5FF)
        } else if (i < 30) {
            Color(0xFF00E676)
        } else {
            Color(0xFFE040FB)
        }

        drawLine(
            color = spikeColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
