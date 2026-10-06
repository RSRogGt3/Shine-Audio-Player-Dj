package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.viewmodels.LocalMusicViewModel

@Composable
fun AiBassBoosterComponent(
    viewModel: LocalMusicViewModel,
    primaryColor: Color,
    surfaceColor: Color,
    modifier: Modifier = Modifier
) {
    val isEnabled by viewModel.isAiBassBoostEnabled.collectAsState()
    val strength by viewModel.aiBassBoostStrength.collectAsState()
    val currentPreset by viewModel.aiBassBoostPreset.collectAsState()
    val subCutoffHz by viewModel.aiSubCutoffHz.collectAsState()
    val liveBassLevel by viewModel.aiBassBoostLevel.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isHiRes192kHzEnabled by viewModel.isHiRes192kHzEnabled.collectAsState()
    val sampleRateLabel by viewModel.sampleRateLabel.collectAsState()

    val bassGlowColor by animateColorAsState(
        targetValue = if (isEnabled) Color(0xFFFF1744) else Color.DarkGray,
        animationSpec = tween(300),
        label = "bass_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_bass_booster_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor.copy(alpha = 0.85f)
        ),
        border = BorderStroke(
            width = if (isEnabled) 1.5.dp else 1.dp,
            color = if (isEnabled) Color(0xFFFF1744).copy(alpha = 0.7f) else Color.DarkGray.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Title, Live Bass Pulse, and Master Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (isEnabled) Color(0xFFFF1744).copy(alpha = 0.25f) else Color.DarkGray.copy(alpha = 0.3f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SurroundSound,
                            contentDescription = "AI Bass Booster",
                            tint = bassGlowColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "AI BASS BOOSTER",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (isEnabled) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFF1744).copy(alpha = 0.25f),
                                    border = BorderStroke(0.5.dp, Color(0xFFFF1744))
                                ) {
                                    Text(
                                        text = "AKTIV",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF5252),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Tiefton-Verstärkung bis 192 kHz Hi-Res",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                // Switch
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { viewModel.toggleAiBassBoost() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFFF1744),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF2B2B38)
                    ),
                    modifier = Modifier.testTag("ai_bass_boost_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-time Bass Energy Dynamic VU Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sub-Bass Pegel (20-120 Hz)",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = if (isEnabled && isPlaying) "${(liveBassLevel * 100).toInt()}% Impuls" else "0% (Standby)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) Color(0xFFFF5252) else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Live dynamic VU indicator bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF161622))
                ) {
                    val animatedWidth = if (isEnabled && isPlaying) liveBassLevel.coerceIn(0.05f, 1f) else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedWidth)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFF9100),
                                        Color(0xFFFF1744),
                                        Color(0xFFD50000)
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Boost Strength Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bass-Stärke / Drive",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${(strength * 100).toInt()}% (+${String.format("%.1f", strength * 18)} dB)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) Color(0xFFFF5252) else Color.Gray
                    )
                }

                Slider(
                    value = strength,
                    onValueChange = { viewModel.setAiBassBoostStrength(it) },
                    enabled = isEnabled,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFF1744),
                        activeTrackColor = Color(0xFFFF1744),
                        inactiveTrackColor = Color(0xFF2C2C3A)
                    ),
                    modifier = Modifier.testTag("ai_bass_strength_slider")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI Preset Selector Chips
            Text(
                text = "AI Bass Profile:",
                fontSize = 11.sp,
                color = Color.LightGray,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(
                    Triple("DYNAMIC_AI", "Dynamic AI", Icons.Filled.AutoAwesome),
                    Triple("SUB_BASS_20HZ", "Sub-Bass 20Hz", Icons.Filled.GraphicEq),
                    Triple("CLUB_808", "Club 808 Punch", Icons.Filled.MusicNote),
                    Triple("DEEP_WARMTH", "Deep Warmth", Icons.Filled.Album),
                    Triple("HI_RES_192KHZ", "192 kHz Audiophile", Icons.Filled.AllInclusive)
                )

                presets.forEach { (id, title, icon) ->
                    val isSelected = currentPreset == id
                    Surface(
                        onClick = { viewModel.setAiBassBoostPreset(id) },
                        enabled = isEnabled,
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected && isEnabled) Color(0xFFFF1744).copy(alpha = 0.25f) else Color(0xFF1B1B26),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected && isEnabled) Color(0xFFFF1744) else Color(0xFF2E2E3E)
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected && isEnabled) Color(0xFFFF5252) else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected && isEnabled) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-Cutoff Hz Frequency Target & 192 kHz Hi-Res Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sub-Cutoff Focus:",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(35, 60, 90, 120).forEach { hz ->
                            val isSelected = subCutoffHz == hz
                            Surface(
                                onClick = { viewModel.setAiSubCutoff(hz) },
                                enabled = isEnabled,
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected && isEnabled) Color(0xFFFF1744).copy(alpha = 0.3f) else Color(0xFF1B1B26),
                                border = BorderStroke(
                                    0.5.dp,
                                    if (isSelected && isEnabled) Color(0xFFFF1744) else Color.DarkGray
                                )
                            ) {
                                Text(
                                    text = "${hz}Hz",
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected && isEnabled) Color(0xFFFF5252) else Color.Gray,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // 192 kHz Hi-Res Master Switch
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isHiRes192kHzEnabled) Color(0xFF7C4DFF).copy(alpha = 0.2f) else Color(0xFF1A1A24),
                    border = BorderStroke(
                        1.dp,
                        if (isHiRes192kHzEnabled) Color(0xFFB388FF) else Color.DarkGray
                    ),
                    modifier = Modifier.clickable { viewModel.toggleHiRes192kHz() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GraphicEq,
                            contentDescription = null,
                            tint = if (isHiRes192kHzEnabled) Color(0xFFE040FB) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "192 kHz Engine",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHiRes192kHzEnabled) Color(0xFFE040FB) else Color.Gray
                            )
                            Text(
                                text = if (isHiRes192kHzEnabled) "Hi-Res Master" else "Standard 48k",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
