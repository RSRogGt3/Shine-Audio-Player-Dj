package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.EqualizerBand
import com.example.ui.viewmodels.FreqCategory
import com.example.ui.viewmodels.LocalMusicViewModel

@Composable
fun VisualEqualizerComponent(
    viewModel: LocalMusicViewModel,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF00E5FF),
    accentColor: Color = Color(0xFFFF007F),
    surfaceColor: Color = Color(0xFF1E1E28)
) {
    val bands by viewModel.equalizerBands.collectAsState()
    val isEqEnabled by viewModel.isEqEnabled.collectAsState()
    val eqPresetName by viewModel.eqPresetName.collectAsState()
    val bassGain by viewModel.eqBassGain.collectAsState()
    val midGain by viewModel.eqMidGain.collectAsState()
    val trebleGain by viewModel.eqTrebleGain.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val beatLevel by viewModel.beatLevel.collectAsState()
    val playbackPosition by viewModel.playbackPosition.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("visual_equalizer_component"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title, EQ Power Toggle, Preset name & Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isEqEnabled) Color(0xFF00E676) else Color.Gray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Grafischer Equalizer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = primaryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = eqPresetName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { viewModel.resetEqualizer() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.LightGray),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("0 dB", fontSize = 12.sp)
                    }
                    Switch(
                        checked = isEqEnabled,
                        onCheckedChange = { viewModel.toggleEqEnabled() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = primaryColor,
                            checkedTrackColor = primaryColor.copy(alpha = 0.3f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF2A2A34)
                        ),
                        modifier = Modifier.testTag("eq_power_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real-Time Visual Frequency Response Curve Canvas
            EqualizerFrequencyCurveCanvas(
                bands = bands,
                isEnabled = isEqEnabled,
                isPlaying = isPlaying,
                audioLevel = audioLevel,
                beatLevel = beatLevel,
                playbackPosition = playbackPosition,
                primaryColor = primaryColor,
                accentColor = accentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A0A10))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Master 3-Band Quick Sliders (Bass, Mids, Treble)
            Text(
                text = "Schnellregelung: Bass • Mitten • Höhen",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bass
                QuickTonalSlider(
                    label = "BASS",
                    freqLabel = "< 250 Hz",
                    value = bassGain,
                    color = Color(0xFFFF5252),
                    icon = Icons.Filled.Speaker,
                    onValueChange = { viewModel.setEqBassGain(it) },
                    modifier = Modifier.weight(1f)
                )
                // Mids
                QuickTonalSlider(
                    label = "MITTEN",
                    freqLabel = "250 - 4k Hz",
                    value = midGain,
                    color = Color(0xFFFFD740),
                    icon = Icons.Filled.Mic,
                    onValueChange = { viewModel.setEqMidGain(it) },
                    modifier = Modifier.weight(1f)
                )
                // Treble
                QuickTonalSlider(
                    label = "HÖHEN",
                    freqLabel = "> 4k Hz",
                    value = trebleGain,
                    color = Color(0xFF00E5FF),
                    icon = Icons.Filled.AutoAwesome,
                    onValueChange = { viewModel.setEqTrebleGain(it) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Individual Channel Faders (Scrollable row)
            Text(
                text = "Frequenz-Bänder (${bands.size} Lanes)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                bands.forEach { band ->
                    ChannelBandFader(
                        band = band,
                        isPlaying = isPlaying,
                        audioLevel = audioLevel,
                        playbackPosition = playbackPosition,
                        onLevelChange = { newLevel ->
                            viewModel.setEqualizerBandLevel(band.band, newLevel.toInt().toShort())
                        },
                        modifier = Modifier.width(62.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Preset Chips
            Text(
                text = "Presets",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf("Flat", "Bass Boost", "Vocal Clarity", "Club / EDM", "Rock / Metal", "Acoustic / Warm", "Treble Sparkle")
                presets.forEach { preset ->
                    val isSelected = eqPresetName == preset
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.applyEqPreset(preset) },
                        label = { Text(preset, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF262630),
                            labelColor = Color.LightGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) primaryColor else Color.DarkGray
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun EqualizerFrequencyCurveCanvas(
    bands: List<EqualizerBand>,
    isEnabled: Boolean,
    isPlaying: Boolean,
    audioLevel: Float,
    beatLevel: Float,
    playbackPosition: Int,
    primaryColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        // Draw frequency category background shading
        val bassWidth = width * 0.30f
        val midWidth = width * 0.40f
        val trebleWidth = width * 0.30f

        drawRect(
            color = Color(0xFFFF1744).copy(alpha = 0.08f),
            topLeft = Offset(0f, 0f),
            size = Size(bassWidth, height)
        )
        drawRect(
            color = Color(0xFFFFC400).copy(alpha = 0.08f),
            topLeft = Offset(bassWidth, 0f),
            size = Size(midWidth, height)
        )
        drawRect(
            color = Color(0xFF00E5FF).copy(alpha = 0.08f),
            topLeft = Offset(bassWidth + midWidth, 0f),
            size = Size(trebleWidth, height)
        )

        // Draw horizontal grid lines (+12 dB, +6 dB, 0 dB, -6 dB, -12 dB)
        val gridLevels = listOf(-1200, -600, 0, 600, 1200)
        gridLevels.forEach { lvl ->
            val y = centerY - (lvl / 1500f) * (height * 0.42f)
            val isZero = lvl == 0
            drawLine(
                color = if (isZero) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = if (isZero) 1.5.dp.toPx() else 1.dp.toPx()
            )
        }

        // Draw dynamic audio visualizer FFT bars dancing in the background
        if (isPlaying && isEnabled) {
            val numBars = 32
            val barW = width / numBars
            for (i in 0 until numBars) {
                val wave = kotlin.math.sin((i * 12f) + (playbackPosition / 140f)).toFloat() * 0.5f + 0.5f
                val activity = (audioLevel * 0.4f + beatLevel * 0.6f) * wave
                val barH = (activity * height * 0.5f).coerceIn(4f, height * 0.75f)
                val barColor = when {
                    i < 10 -> Color(0xFFFF5252).copy(alpha = 0.3f)
                    i < 22 -> Color(0xFFFFD740).copy(alpha = 0.3f)
                    else -> Color(0xFF00E5FF).copy(alpha = 0.3f)
                }
                drawRect(
                    color = barColor,
                    topLeft = Offset(i * barW + 2f, height - barH),
                    size = Size((barW - 4f).coerceAtLeast(1f), barH)
                )
            }
        }

        if (bands.isEmpty()) return@Canvas

        // Calculate control points for each frequency band
        val points = mutableListOf<Offset>()
        bands.forEachIndexed { index, band ->
            val x = if (bands.size > 1) {
                val ratio = index.toFloat() / (bands.size - 1)
                24.dp.toPx() + ratio * (width - 48.dp.toPx())
            } else {
                width / 2f
            }
            val gainRatio = if (isEnabled) (band.currentLevel / 1500f).coerceIn(-1f, 1f) else 0f
            val y = centerY - gainRatio * (height * 0.42f)
            points.add(Offset(x, y))
        }

        // Build smooth Cubic Bézier Spline through control points
        val curvePath = Path()
        val fillPath = Path()

        curvePath.moveTo(0f, points.first().y)
        fillPath.moveTo(0f, height)
        fillPath.lineTo(0f, points.first().y)

        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val cx1 = (p0.x + p1.x) / 2f
            val cy1 = p0.y
            val cx2 = (p0.x + p1.x) / 2f
            val cy2 = p1.y

            curvePath.cubicTo(cx1, cy1, cx2, cy2, p1.x, p1.y)
            fillPath.cubicTo(cx1, cy1, cx2, cy2, p1.x, p1.y)
        }

        val lastPoint = points.last()
        curvePath.lineTo(width, lastPoint.y)
        fillPath.lineTo(width, lastPoint.y)
        fillPath.lineTo(width, height)
        fillPath.close()

        // Draw gradient area underneath curve
        val gradientBrush = Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = if (isEnabled) 0.35f else 0.1f),
                accentColor.copy(alpha = if (isEnabled) 0.15f else 0.05f),
                Color.Transparent
            ),
            startY = 0f,
            endY = height
        )
        drawPath(path = fillPath, brush = gradientBrush)

        // Draw glowing active curve line
        val lineBrush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFFF5252),
                Color(0xFFFFD740),
                primaryColor,
                Color(0xFFE040FB)
            )
        )
        drawPath(
            path = curvePath,
            brush = if (isEnabled) lineBrush else Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray)),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw node circles at each band
        points.forEachIndexed { idx, point ->
            val band = bands[idx]
            val nodeColor = when (band.category) {
                FreqCategory.BASS -> Color(0xFFFF5252)
                FreqCategory.MID -> Color(0xFFFFD740)
                FreqCategory.TREBLE -> Color(0xFF00E5FF)
            }
            drawCircle(
                color = Color.Black,
                radius = 6.dp.toPx(),
                center = point
            )
            drawCircle(
                color = if (isEnabled) nodeColor else Color.Gray,
                radius = 4.5.dp.toPx(),
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
fun QuickTonalSlider(
    label: String,
    freqLabel: String,
    value: Float, // -12 .. +12 dB
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF14141E),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                }
                val sign = if (value > 0) "+" else ""
                Text(
                    text = "$sign${value.toInt()} dB",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(freqLabel, fontSize = 9.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = -12f..12f,
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = Color(0xFF2A2A38)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tonal_slider_${label.lowercase()}")
            )
        }
    }
}

@Composable
fun ChannelBandFader(
    band: EqualizerBand,
    isPlaying: Boolean,
    audioLevel: Float,
    playbackPosition: Int,
    onLevelChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = when (band.category) {
        FreqCategory.BASS -> Color(0xFFFF5252)
        FreqCategory.MID -> Color(0xFFFFD740)
        FreqCategory.TREBLE -> Color(0xFF00E5FF)
    }

    val dbValue = (band.currentLevel / 100f)
    val formattedFreq = if (band.centerFreq >= 1000) {
        "${(band.centerFreq / 1000f).let { if (it % 1 == 0f) it.toInt().toString() else "%.1f".format(it) }}k"
    } else {
        "${band.centerFreq}"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF14141E),
        border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Category Badge
            Text(
                text = band.category.name,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = categoryColor
            )

            // dB Readout
            val sign = if (dbValue > 0) "+" else ""
            Text(
                text = "$sign${"%.1f".format(dbValue)}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (dbValue != 0f) Color.White else Color.Gray,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Slider & Dynamic VU Bar side by side in a Box/Row
            Box(
                modifier = Modifier
                    .height(130.dp)
                    .width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Center (0 dB) Line
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerY = size.height / 2f
                    drawLine(
                        color = Color.White.copy(alpha = 0.2f),
                        start = Offset(4.dp.toPx(), centerY),
                        end = Offset(size.width - 4.dp.toPx(), centerY),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Vertical Slider
                Slider(
                    value = band.currentLevel.toFloat(),
                    onValueChange = onLevelChange,
                    valueRange = band.minLevel.toFloat()..band.maxLevel.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = categoryColor,
                        activeTrackColor = categoryColor,
                        inactiveTrackColor = Color(0xFF262635)
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(130.dp)
                        .graphicsLayer {
                            rotationZ = 270f
                        }
                        .testTag("eq_fader_${band.centerFreq}hz")
                )
            }

            // Frequency label (e.g. 60, 1k, 14k)
            Text(
                text = "$formattedFreq Hz",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.LightGray
            )

            // 0 dB Reset button
            TextButton(
                onClick = { onLevelChange(0f) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(20.dp)
            ) {
                Text("0", fontSize = 9.sp, color = Color.Gray)
            }
        }
    }
}
