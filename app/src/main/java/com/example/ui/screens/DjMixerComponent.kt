package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.LocalMusicViewModel

@Composable
fun DjMixerComponent(
    viewModel: LocalMusicViewModel,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF00E5FF),
    accentColor: Color = Color(0xFFFF007F)
) {
    // Mixer State Flows
    val ch1Level by viewModel.ch1Level.collectAsState()
    val ch2Level by viewModel.ch2Level.collectAsState()
    val masterLevel by viewModel.masterLevel.collectAsState()
    val crossfader by viewModel.crossfader.collectAsState()
    val trimA by viewModel.trimA.collectAsState()
    val trimB by viewModel.trimB.collectAsState()
    val eqHighA by viewModel.eqHighA.collectAsState()
    val eqMidA by viewModel.eqMidA.collectAsState()
    val eqLowA by viewModel.eqLowA.collectAsState()
    val eqHighB by viewModel.eqHighB.collectAsState()
    val eqMidB by viewModel.eqMidB.collectAsState()
    val eqLowB by viewModel.eqLowB.collectAsState()
    val colorFxCh1 by viewModel.colorFxCh1.collectAsState()
    val colorFxCh2 by viewModel.colorFxCh2.collectAsState()
    val cueA by viewModel.cueHeadphoneA.collectAsState()
    val cueB by viewModel.cueHeadphoneB.collectAsState()
    val audioLevelA by viewModel.audioLevelA.collectAsState()
    val audioLevelB by viewModel.audioLevelB.collectAsState()
    val isBassKilled by viewModel.isBassKilled.collectAsState()
    val isMidKilled by viewModel.isMidKilled.collectAsState()
    val isHighKilled by viewModel.isHighKilled.collectAsState()

    // Beat FX States
    val activeFxType by viewModel.activeFxType.collectAsState()
    val isFxActive by viewModel.isFxActive.collectAsState()
    val fxTargetChannel by viewModel.fxTargetChannel.collectAsState()
    val fxDryWet by viewModel.fxDryWet.collectAsState()
    val fxBeatFraction by viewModel.fxBeatFraction.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dj_mixer_console"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF100F17),
        border = BorderStroke(1.5.dp, Color(0xFF262536))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // MIXER TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DJM PRO MISCHPULT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E1D2D)
                ) {
                    Text(
                        text = "3-BAND ISOLATOR • COLOR FX • BEAT FX",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // CH 1 & CH 2 CHANNEL STRIPS SIDE-BY-SIDE
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // CHANNEL 1 STRIP
                ChannelStrip(
                    channelNumber = 1,
                    deckName = "DECK A",
                    channelColor = primaryColor,
                    trim = trimA,
                    onTrimChange = { viewModel.setTrimA(it) },
                    high = eqHighA,
                    onHighChange = { viewModel.setEqHighA(it) },
                    mid = eqMidA,
                    onMidChange = { viewModel.setEqMidA(it) },
                    low = eqLowA,
                    onLowChange = { viewModel.setEqLowA(it) },
                    colorFx = colorFxCh1,
                    onColorFxChange = { viewModel.setColorFxCh1(it) },
                    cue = cueA,
                    onToggleCue = { viewModel.toggleCueHeadphoneA() },
                    faderLevel = ch1Level,
                    onFaderChange = { viewModel.setCh1Level(it) },
                    vuLevel = audioLevelA,
                    isKillLow = isBassKilled,
                    onToggleKillLow = { viewModel.toggleBassKill() },
                    modifier = Modifier.weight(1f)
                )

                // CHANNEL 2 STRIP
                ChannelStrip(
                    channelNumber = 2,
                    deckName = "DECK B",
                    channelColor = accentColor,
                    trim = trimB,
                    onTrimChange = { viewModel.setTrimB(it) },
                    high = eqHighB,
                    onHighChange = { viewModel.setEqHighB(it) },
                    mid = eqMidB,
                    onMidChange = { viewModel.setEqMidB(it) },
                    low = eqLowB,
                    onLowChange = { viewModel.setEqLowB(it) },
                    colorFx = colorFxCh2,
                    onColorFxChange = { viewModel.setColorFxCh2(it) },
                    cue = cueB,
                    onToggleCue = { viewModel.toggleCueHeadphoneB() },
                    faderLevel = ch2Level,
                    onFaderChange = { viewModel.setCh2Level(it) },
                    vuLevel = audioLevelB,
                    isKillLow = isBassKilled,
                    onToggleKillLow = { viewModel.toggleBassKill() },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // CROSSFADER SECTION
            // ==========================================
            CrossfaderConsole(
                crossfader = crossfader,
                onCrossfaderChange = { viewModel.setCrossfader(it) },
                primaryColor = primaryColor,
                accentColor = accentColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // PIONEER BEAT FX SECTION
            // ==========================================
            PioneerBeatFxConsole(
                activeFxType = activeFxType,
                isFxActive = isFxActive,
                fxTargetChannel = fxTargetChannel,
                fxDryWet = fxDryWet,
                fxBeatFraction = fxBeatFraction,
                onSelectFxType = { viewModel.setFxType(it) },
                onToggleFx = { viewModel.toggleFxActive() },
                onSelectTarget = { viewModel.setFxTarget(it) },
                onDepthChange = { viewModel.setFxDryWet(it) },
                onSelectBeatFraction = { viewModel.setFxBeatFraction(it) },
                primaryColor = primaryColor
            )
        }
    }
}

/**
 * Single DJ Mixer Channel Strip (Trim, HI, MID, LOW, Color FX, CUE, VU meter, and Volume Fader).
 */
@Composable
fun ChannelStrip(
    channelNumber: Int,
    deckName: String,
    channelColor: Color,
    trim: Float,
    onTrimChange: (Float) -> Unit,
    high: Float,
    onHighChange: (Float) -> Unit,
    mid: Float,
    onMidChange: (Float) -> Unit,
    low: Float,
    onLowChange: (Float) -> Unit,
    colorFx: Float,
    onColorFxChange: (Float) -> Unit,
    cue: Boolean,
    onToggleCue: () -> Unit,
    faderLevel: Float,
    onFaderChange: (Float) -> Unit,
    vuLevel: Float,
    isKillLow: Boolean,
    onToggleKillLow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF151420),
        border = BorderStroke(1.dp, channelColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Strip Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CH $channelNumber",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = channelColor,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = deckName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // TRIM Knob Slider
            RotaryKnobRow(
                label = "TRIM",
                value = trim,
                onValueChange = onTrimChange,
                range = 0.2f..1.5f,
                accentColor = channelColor,
                displayValue = String.format("%.1fx", trim)
            )

            Divider(color = Color(0xFF222131), modifier = Modifier.padding(vertical = 6.dp))

            // 3-BAND EQ: HI, MID, LOW
            RotaryKnobRow(
                label = "HI EQ",
                value = high,
                onValueChange = onHighChange,
                range = -1f..1f,
                accentColor = Color(0xFF00E5FF),
                displayValue = formatDb(high)
            )

            RotaryKnobRow(
                label = "MID EQ",
                value = mid,
                onValueChange = onMidChange,
                range = -1f..1f,
                accentColor = Color(0xFFFFB300),
                displayValue = formatDb(mid)
            )

            RotaryKnobRow(
                label = "LOW EQ",
                value = low,
                onValueChange = onLowChange,
                range = -1f..1f,
                accentColor = Color(0xFFFF1744),
                displayValue = formatDb(low)
            )

            // Low Kill Button
            Surface(
                onClick = onToggleKillLow,
                shape = RoundedCornerShape(4.dp),
                color = if (isKillLow) Color(0xFFFF1744) else Color(0xFF252336),
                modifier = Modifier.fillMaxWidth().height(22.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isKillLow) "BASS KILL ON" else "BASS KILL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isKillLow) Color.White else Color.Gray
                    )
                }
            }

            Divider(color = Color(0xFF222131), modifier = Modifier.padding(vertical = 6.dp))

            // COLOR FX FILTER KNOB (LPF / HPF)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("COLOR FX", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    val filterMode = when {
                        colorFx < 0.45f -> "LPF"
                        colorFx > 0.55f -> "HPF"
                        else -> "FLAT"
                    }
                    Text(filterMode, fontSize = 9.sp, fontWeight = FontWeight.Black, color = channelColor)
                }
                Slider(
                    value = colorFx,
                    onValueChange = onColorFxChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = channelColor, activeTrackColor = channelColor),
                    modifier = Modifier.fillMaxWidth().height(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // CUE HEADPHONE MONITOR BUTTON
            Surface(
                onClick = onToggleCue,
                shape = RoundedCornerShape(6.dp),
                color = if (cue) Color(0xFFFF9100) else Color(0xFF252336),
                border = BorderStroke(1.dp, if (cue) Color.White else Color.Transparent),
                modifier = Modifier.fillMaxWidth().height(26.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Headphones,
                        contentDescription = null,
                        tint = if (cue) Color.Black else Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CUE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (cue) Color.Black else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CHANNEL VU METER + VERTICAL FADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vertical LED VU Meter
                LedVuMeterBar(
                    audioLevel = vuLevel,
                    modifier = Modifier
                        .width(16.dp)
                        .fillMaxHeight()
                )

                // Channel Fader
                Column(
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${(faderLevel * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = channelColor
                    )
                    Slider(
                        value = faderLevel,
                        onValueChange = onFaderChange,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = channelColor, activeTrackColor = channelColor),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "CH $channelNumber FADER",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Compact Row with label, mini slider, and readout.
 */
@Composable
fun RotaryKnobRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    accentColor: Color,
    displayValue: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(displayValue, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor),
            modifier = Modifier.fillMaxWidth().height(26.dp)
        )
    }
}

/**
 * Vertical LED Peak Meter with Green, Amber, Red segments.
 */
@Composable
fun LedVuMeterBar(
    audioLevel: Float,
    modifier: Modifier = Modifier
) {
    val segments = 10
    val activeSegments = (audioLevel.coerceIn(0f, 1f) * segments).toInt()

    Column(
        modifier = modifier
            .background(Color(0xFF09080E), RoundedCornerShape(4.dp))
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (i in (segments - 1) downTo 0) {
            val isActive = i < activeSegments
            val ledColor = when {
                i >= 8 -> if (isActive) Color(0xFFFF1744) else Color(0xFF3B0810) // Red Peak
                i >= 6 -> if (isActive) Color(0xFFFFD600) else Color(0xFF3B3200) // Amber Warning
                else -> if (isActive) Color(0xFF00E676) else Color(0xFF063314)   // Green Normal
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(ledColor, RoundedCornerShape(1.dp))
            )
        }
    }
}

/**
 * Crossfader Console with CH1 and CH2 indicators.
 */
@Composable
fun CrossfaderConsole(
    crossfader: Float,
    onCrossfaderChange: (Float) -> Unit,
    primaryColor: Color,
    accentColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF151420),
        border = BorderStroke(1.dp, Color(0xFF262538))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CH 1 (DECK A)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(
                    text = "DJ CROSSFADER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "CH 2 (DECK B)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Slider(
                value = crossfader,
                onValueChange = onCrossfaderChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = primaryColor,
                    inactiveTrackColor = accentColor
                ),
                modifier = Modifier.fillMaxWidth().testTag("crossfader_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { onCrossfaderChange(0f) }, contentPadding = PaddingValues(0.dp)) {
                    Text("CH 1", fontSize = 10.sp, color = primaryColor)
                }
                TextButton(onClick = { onCrossfaderChange(0.5f) }, contentPadding = PaddingValues(0.dp)) {
                    Text("CENTER (50/50)", fontSize = 10.sp, color = Color.White)
                }
                TextButton(onClick = { onCrossfaderChange(1f) }, contentPadding = PaddingValues(0.dp)) {
                    Text("CH 2", fontSize = 10.sp, color = accentColor)
                }
            }
        }
    }
}

/**
 * Pioneer DJM Beat FX Section: FX Type, Target Assign, Beat fractions, Depth, and ON/OFF.
 */
@Composable
fun PioneerBeatFxConsole(
    activeFxType: String,
    isFxActive: Boolean,
    fxTargetChannel: String,
    fxDryWet: Float,
    fxBeatFraction: String,
    onSelectFxType: (String) -> Unit,
    onToggleFx: () -> Unit,
    onSelectTarget: (String) -> Unit,
    onDepthChange: (Float) -> Unit,
    onSelectBeatFraction: (String) -> Unit,
    primaryColor: Color
) {
    val fxTypes = listOf("FILTER", "ECHO", "FLANGER", "REVERB", "ROLL", "SPIRAL", "CRUSH")
    val beatFractions = listOf("¼", "½", "¾", "1", "2", "4")

    // Flashing LED when Beat FX is active
    val fxFlashAlpha by rememberInfiniteTransition(label = "fx_flash").animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fx_flash_alpha"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF161524),
        border = BorderStroke(1.5.dp, if (isFxActive) Color(0xFFFF9100) else Color(0xFF28273A))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (isFxActive) Color(0xFFFF9100).copy(alpha = fxFlashAlpha) else Color.DarkGray,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BEAT FX PANEL",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                // Channel Assignment Chips: CH1, CH2, MASTER
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("CH1", "CH2", "MASTER").forEach { target ->
                        val isSelected = fxTargetChannel == target
                        Surface(
                            onClick = { onSelectTarget(target) },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) Color(0xFFFF9100) else Color(0xFF242336)
                        ) {
                            Text(
                                text = target,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color.LightGray,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // FX Type Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                fxTypes.forEach { fxName ->
                    val isSelected = activeFxType == fxName
                    Surface(
                        onClick = { onSelectFxType(fxName) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) primaryColor else Color(0xFF222132),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fxName,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color.Black else Color.LightGray,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Beat Fraction Selector: 1/4, 1/2, 3/4, 1, 2, 4
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("BEAT:", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                beatFractions.forEach { frac ->
                    val isSelected = fxBeatFraction == frac
                    Surface(
                        onClick = { onSelectBeatFraction(frac) },
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) Color(0xFFFF9100) else Color(0xFF222132),
                        modifier = Modifier.weight(1f).height(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = frac,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color.LightGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // LEVEL / DEPTH & BIG BEAT FX ON/OFF BUTTON
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dry/Wet Slider
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("LEVEL / DEPTH", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("${(fxDryWet * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9100))
                    }
                    Slider(
                        value = fxDryWet,
                        onValueChange = onDepthChange,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFFF9100), activeTrackColor = Color(0xFFFF9100)),
                        modifier = Modifier.fillMaxWidth().height(32.dp)
                    )
                }

                // Big Illuminated Beat FX ON/OFF Button
                Surface(
                    onClick = onToggleFx,
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFxActive) Color(0xFFFF9100) else Color(0xFF2A2838),
                    border = BorderStroke(2.dp, if (isFxActive) Color.White else Color(0xFFFF9100)),
                    modifier = Modifier.height(46.dp).width(110.dp).testTag("beat_fx_toggle_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isFxActive) "FX ON" else "BEAT FX",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isFxActive) Color.Black else Color(0xFFFF9100),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatDb(v: Float): String {
    val db = (v * 12).toInt()
    val sign = if (db >= 0) "+" else ""
    return "$sign${db}dB"
}
