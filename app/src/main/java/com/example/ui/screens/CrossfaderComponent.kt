package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

enum class CrossfaderCurve(val label: String, val description: String) {
    SMOOTH("Smooth", "Equal-power DJ blend"),
    LINEAR("Linear", "Linear attenuation"),
    SCRATCH("Scratch Cut", "Sharp transition")
}

/**
 * Calculates volume level (0.0 to 1.0) for Track A given crossfader value (0.0 to 1.0) and curve mode.
 */
fun calculateTrackAVolume(crossfader: Float, curve: CrossfaderCurve): Float {
    return when (curve) {
        CrossfaderCurve.SMOOTH -> if (crossfader <= 0.5f) 1.0f else ((1.0f - crossfader) * 2.0f).coerceIn(0f, 1f)
        CrossfaderCurve.LINEAR -> (1.0f - crossfader).coerceIn(0f, 1f)
        CrossfaderCurve.SCRATCH -> if (crossfader < 0.9f) 1.0f else ((1.0f - crossfader) * 10f).coerceIn(0f, 1f)
    }
}

/**
 * Calculates volume level (0.0 to 1.0) for Track B given crossfader value (0.0 to 1.0) and curve mode.
 */
fun calculateTrackBVolume(crossfader: Float, curve: CrossfaderCurve): Float {
    return when (curve) {
        CrossfaderCurve.SMOOTH -> if (crossfader >= 0.5f) 1.0f else (crossfader * 2.0f).coerceIn(0f, 1f)
        CrossfaderCurve.LINEAR -> crossfader.coerceIn(0f, 1f)
        CrossfaderCurve.SCRATCH -> if (crossfader > 0.1f) 1.0f else (crossfader * 10f).coerceIn(0f, 1f)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossfaderComponent(
    crossfaderValue: Float,
    onCrossfaderChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    trackAName: String = "Track A (Deck 1)",
    trackBName: String = "Track B (Deck 2)",
    primaryColor: Color = Color(0xFF00E5FF),
    accentColor: Color = Color(0xFFE040FB),
    surfaceColor: Color = Color(0xFF1E1E2C)
) {
    var selectedCurve by remember { mutableStateOf(CrossfaderCurve.SMOOTH) }

    val trackAVolume = calculateTrackAVolume(crossfaderValue, selectedCurve)
    val trackBVolume = calculateTrackBVolume(crossfaderValue, selectedCurve)

    val animatedTrackA by animateFloatAsState(targetValue = trackAVolume, animationSpec = tween(100), label = "volA")
    val animatedTrackB by animateFloatAsState(targetValue = trackBVolume, animationSpec = tween(100), label = "volB")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("crossfader_component_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.9f)),
        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.6f), accentColor.copy(alpha = 0.6f))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with title and curve picker selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = "Crossfader Balance",
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "AUDIO CROSSFADER",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Volume Balance: Track A vs Track B",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }

                // Curve selector chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CrossfaderCurve.values().forEach { curve ->
                        val isSelected = curve == selectedCurve
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCurve = curve },
                            label = { Text(curve.label, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF28283E),
                                labelColor = Color.LightGray
                            ),
                            border = null,
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }
            }

            // Track Volume Status Cards (Track A vs Track B)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Track A Info Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (trackAVolume > 0.05f) Color(0xFF142838) else Color(0xFF12121A)
                    ),
                    border = BorderStroke(1.dp, if (trackAVolume > 0.5f) primaryColor else Color(0xFF2A2A3D))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DECK A",
                                color = primaryColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(animatedTrackA * 100).roundToInt()}%",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = trackAName,
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // VU Volume Meter Bar for Track A
                        LinearProgressIndicator(
                            progress = { animatedTrackA },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = primaryColor,
                            trackColor = Color(0xFF1E1E2E)
                        )
                    }
                }

                // Track B Info Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (trackBVolume > 0.05f) Color(0xFF321A38) else Color(0xFF12121A)
                    ),
                    border = BorderStroke(1.dp, if (trackBVolume > 0.5f) accentColor else Color(0xFF2A2A3D))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DECK B",
                                color = accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(animatedTrackB * 100).roundToInt()}%",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = trackBName,
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // VU Volume Meter Bar for Track B
                        LinearProgressIndicator(
                            progress = { animatedTrackB },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = accentColor,
                            trackColor = Color(0xFF1E1E2E)
                        )
                    }
                }
            }

            // Crossfader Main Slider Track
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Label Markers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("◀ TRACK A", color = primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("CENTER", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("TRACK B ▶", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Styled Slider
                Slider(
                    value = crossfaderValue,
                    onValueChange = onCrossfaderChange,
                    valueRange = 0.0f..1.0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("crossfader_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = primaryColor,
                        inactiveTrackColor = accentColor.copy(alpha = 0.5f)
                    )
                )
            }

            // Preset Quick Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onCrossfaderChange(0.0f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("crossfader_preset_a"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.8f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (crossfaderValue == 0.0f) primaryColor.copy(alpha = 0.2f) else Color.Transparent,
                        contentColor = primaryColor
                    )
                ) {
                    Text("100% Deck A", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onCrossfaderChange(0.5f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("crossfader_preset_center"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (crossfaderValue in 0.48f..0.52f) Color.White else Color(0xFF2C2C3E),
                        contentColor = if (crossfaderValue in 0.48f..0.52f) Color.Black else Color.White
                    )
                ) {
                    Text("Center 50/50", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onCrossfaderChange(1.0f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("crossfader_preset_b"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.8f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (crossfaderValue == 1.0f) accentColor.copy(alpha = 0.2f) else Color.Transparent,
                        contentColor = accentColor
                    )
                ) {
                    Text("100% Deck B", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
