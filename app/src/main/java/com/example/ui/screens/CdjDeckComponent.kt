package com.example.ui.screens

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.ui.viewmodels.DjSkin
import com.example.ui.viewmodels.LocalMusicViewModel
import com.example.ui.viewmodels.LocalTrack

@OptIn(UnstableApi::class)
@Composable
fun CdjDeckComponent(
    deckId: String, // "A" or "B"
    channelNumber: Int, // 1 or 2
    track: LocalTrack?,
    isPlaying: Boolean,
    playbackPosition: Int,
    playbackDuration: Int,
    bpm: Int,
    pitch: Float,
    isMaster: Boolean,
    isSync: Boolean,
    isBeatLock: Boolean,
    currentBeat: Int, // 1..4
    beatPhase: Float, // 0..1
    beatPhaseDiff: Float,
    isLoopActive: Boolean,
    activeLoopBeats: Float?,
    hotCues: Map<Int, Int>,
    themeColor: Color,
    surfaceColor: Color,
    currentSkin: DjSkin,
    videoMode: Boolean,
    onToggleVideoMode: () -> Unit,
    onLoadTrack: () -> Unit,
    onTogglePlay: () -> Unit,
    onCue: () -> Unit,
    onToggleSync: () -> Unit,
    onSetMaster: () -> Unit,
    onToggleBeatLock: () -> Unit,
    onPitchChange: (Float) -> Unit,
    onNudgeMinus: () -> Unit,
    onNudgePlus: () -> Unit,
    onSeek: (Int) -> Unit,
    onLoopIn: () -> Unit,
    onLoopOut: () -> Unit,
    onExitLoop: () -> Unit,
    onAutoLoop: (Float) -> Unit,
    onHalveLoop: () -> Unit,
    onDoubleLoop: () -> Unit,
    onTriggerHotCue: (Int) -> Unit,
    onClearHotCue: (Int) -> Unit,
    viewModel: LocalMusicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val effectiveBpm = (bpm * pitch).toInt()
    val pitchPercent = ((pitch - 1.0f) * 100f)
    val pitchSign = if (pitchPercent >= 0) "+" else ""
    val formattedPitch = String.format("%s%.1f%%", pitchSign, pitchPercent)

    // Pioneer CDJ Chassis
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cdj_deck_$deckId"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF111018),
        border = BorderStroke(1.5.dp, Brush.verticalGradient(listOf(themeColor.copy(alpha = 0.8f), Color(0xFF232230))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // DECK CHASSIS TOP HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isPlaying) Color(0xFF00E676) else Color(0xFFFF5252), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DECK $deckId",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = themeColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CH $channelNumber",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Video View Toggle (MP4 live player vs Turntable)
                    if (track?.isVideo == true) {
                        Surface(
                            onClick = onToggleVideoMode,
                            shape = RoundedCornerShape(8.dp),
                            color = if (videoMode) Color(0xFFE040FB) else Color(0xFF262536),
                            border = BorderStroke(1.dp, if (videoMode) Color.White else Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (videoMode) Icons.Filled.Videocam else Icons.Filled.Album,
                                    contentDescription = null,
                                    tint = if (videoMode) Color.Black else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (videoMode) "VIDEO ON" else "JOGWHEEL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (videoMode) Color.Black else Color.White
                                )
                            }
                        }
                    }

                    // Load Track Button
                    Button(
                        onClick = onLoadTrack,
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("load_track_button_$deckId")
                    ) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LOAD (MP3/MP4)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // PIONEER TOP LCD DISPLAY WITH BEATANZEIGE
            // ==========================================
            CdjLcdDisplay(
                deckId = deckId,
                track = track,
                isPlaying = isPlaying,
                playbackPosition = playbackPosition,
                playbackDuration = playbackDuration,
                effectiveBpm = effectiveBpm,
                formattedPitch = formattedPitch,
                isMaster = isMaster,
                isSync = isSync,
                isBeatLock = isBeatLock,
                currentBeat = currentBeat,
                beatPhase = beatPhase,
                beatPhaseDiff = beatPhaseDiff,
                isLoopActive = isLoopActive,
                activeLoopBeats = activeLoopBeats,
                themeColor = themeColor,
                onSeek = onSeek
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // CENTER: JOGWHEEL OR LIVE MP4 VIDEO PLAYER
            // ==========================================
            if (track?.isVideo == true && videoMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(1.5.dp, themeColor.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val player = remember(deckId) {
                        if (deckId == "A") viewModel.fetchExoPlayerA(context) else viewModel.fetchExoPlayerB(context)
                    }
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                this.player = player
                                useController = false
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        update = { view ->
                            view.player = player
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Video Overlay Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.75f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isPlaying) Color(0xFF00E676) else Color.Gray, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DECK $deckId • LIVE MP4 VIDEO MIX",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColor
                            )
                        }
                    }
                }
            } else {
                // Digital CDJ Turntable Jogwheel
                Box(modifier = Modifier.size(240.dp), contentAlignment = Alignment.Center) {
                    val rotationAnim by rememberInfiniteTransition(label = "rot_$deckId").animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = (1800f / pitch).toInt(), easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "cdj_rotation_$deckId"
                    )

                    DigitalCdj(
                        rotation = if (isPlaying) rotationAnim else 0f,
                        isPlaying = isPlaying,
                        audioLevel = if (isPlaying) 0.85f else 0f,
                        beatLevel = if (isPlaying && beatPhase < 0.25f) 1.0f else 0.2f,
                        primaryColor = themeColor,
                        accentColor = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // PIONEER LOOPS SECTION
            // ==========================================
            PioneerLoopSection(
                isLoopActive = isLoopActive,
                activeLoopBeats = activeLoopBeats,
                onLoopIn = onLoopIn,
                onLoopOut = onLoopOut,
                onExitLoop = onExitLoop,
                onAutoLoop = onAutoLoop,
                onHalveLoop = onHalveLoop,
                onDoubleLoop = onDoubleLoop,
                themeColor = themeColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // SYNC, MASTER, BEATLOCK & TEMPO SECTION
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // MASTER button
                Surface(
                    onClick = onSetMaster,
                    shape = RoundedCornerShape(8.dp),
                    color = if (isMaster) Color(0xFFFF9100) else Color(0xFF262534),
                    border = BorderStroke(1.dp, if (isMaster) Color.White else Color.Transparent),
                    modifier = Modifier.weight(1f).testTag("master_btn_$deckId")
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "MASTER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isMaster) Color.Black else Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // SYNC button
                Surface(
                    onClick = onToggleSync,
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSync) Color(0xFF00E5FF) else Color(0xFF262534),
                    border = BorderStroke(1.dp, if (isSync) Color.White else Color.Transparent),
                    modifier = Modifier.weight(1f).testTag("sync_btn_$deckId")
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "BEAT SYNC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSync) Color.Black else Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // BEATLOCK / MASTER TEMPO (Key Lock)
                Surface(
                    onClick = onToggleBeatLock,
                    shape = RoundedCornerShape(8.dp),
                    color = if (isBeatLock) Color(0xFFFF1744) else Color(0xFF262534),
                    border = BorderStroke(1.dp, if (isBeatLock) Color.White else Color.Transparent),
                    modifier = Modifier.weight(1.3f).testTag("beatlock_btn_$deckId")
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "BEATLOCK (KEY)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isBeatLock) Color.White else Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // PITCH / TEMPO FADER WITH NUDGE
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF171622), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNudgeMinus,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF2A2938), CircleShape)
                ) {
                    Text("◄", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TEMPO PITCH", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(formattedPitch, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColor)
                    }
                    Slider(
                        value = pitch,
                        onValueChange = onPitchChange,
                        valueRange = 0.84f..1.16f,
                        colors = SliderDefaults.colors(thumbColor = themeColor, activeTrackColor = themeColor),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                IconButton(
                    onClick = onNudgePlus,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF2A2938), CircleShape)
                ) {
                    Text("►", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = { onPitchChange(1.0f) },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.width(34.dp)
                ) {
                    Text("0%", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // PERFORMANCE HOT CUE PADS (1..4)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val padColors = listOf(
                    Color(0xFF00E676), // Green
                    Color(0xFFFF9100), // Orange
                    Color(0xFFFF007F), // Pink
                    Color(0xFF00E5FF)  // Cyan
                )
                (1..4).forEach { padIndex ->
                    val cuePos = hotCues[padIndex]
                    val isSet = cuePos != null
                    val padColor = padColors[padIndex - 1]

                    Surface(
                        onClick = { onTriggerHotCue(padIndex) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSet) padColor else Color(0xFF22212F),
                        border = BorderStroke(1.dp, if (isSet) Color.White else Color(0xFF353448)),
                        modifier = Modifier.weight(1f).testTag("hot_cue_${deckId}_$padIndex")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "HOT CUE $padIndex",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSet) Color.Black else Color.LightGray
                            )
                            Text(
                                text = if (isSet) formatMmSs(cuePos) else "EMPTY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSet) Color.Black else Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // PIONEER BIG TRANSPORT BUTTONS: PLAY & CUE
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // BIG CUE BUTTON (Amber LED)
                Surface(
                    onClick = onCue,
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF2A281E),
                    border = BorderStroke(2.dp, Color(0xFFFFB300)),
                    modifier = Modifier.weight(1f).height(54.dp).testTag("cdj_cue_btn_$deckId")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFFFFB300), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CUE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFB300),
                            letterSpacing = 1.sp
                        )
                    }
                }

                // BIG PLAY / PAUSE BUTTON (Green LED)
                Surface(
                    onClick = onTogglePlay,
                    shape = RoundedCornerShape(14.dp),
                    color = if (isPlaying) Color(0xFF003816) else Color(0xFF1E2822),
                    border = BorderStroke(2.dp, if (isPlaying) Color(0xFF00E676) else Color(0xFF4CAF50)),
                    modifier = Modifier.weight(1.3f).height(54.dp).testTag("cdj_play_btn_$deckId")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlaying) "PAUSE" else "PLAY",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00E676),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Pioneer CDJ Top Screen with Live Beatanzeige [1 2 3 4], Phase Meter, Waveform & Time.
 */
@Composable
fun CdjLcdDisplay(
    deckId: String,
    track: LocalTrack?,
    isPlaying: Boolean,
    playbackPosition: Int,
    playbackDuration: Int,
    effectiveBpm: Int,
    formattedPitch: String,
    isMaster: Boolean,
    isSync: Boolean,
    isBeatLock: Boolean,
    currentBeat: Int,
    beatPhase: Float,
    beatPhaseDiff: Float,
    isLoopActive: Boolean,
    activeLoopBeats: Float?,
    themeColor: Color,
    onSeek: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0B0A12),
        border = BorderStroke(1.dp, Color(0xFF252336))
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // Track Info Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track?.title ?: "KEIN TRACK GELADEN (LOAD DRÜCKEN)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track?.artist ?: "Deck $deckId Bereit",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }

                // Deck Status Badges
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isMaster) {
                        BadgePill("MASTER", Color(0xFFFF9100))
                    }
                    if (isSync) {
                        BadgePill("SYNC", Color(0xFF00E5FF))
                    }
                    if (isBeatLock) {
                        BadgePill("MT", Color(0xFFFF1744)) // Master Tempo
                    }
                    if (isLoopActive) {
                        val beatsStr = activeLoopBeats?.let { "${it}B" } ?: "LOOP"
                        BadgePill(beatsStr, Color(0xFFFFD600))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // BEATANZEIGE: 4-BEAT COUNTER [ 1 | 2 | 3 | 4 ]
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF151422), RoundedCornerShape(6.dp))
                    .padding(vertical = 4.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BEAT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Gray
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..4).forEach { beatNum ->
                        val isCurrent = isPlaying && currentBeat == beatNum
                        Box(
                            modifier = Modifier
                                .size(width = 34.dp, height = 20.dp)
                                .background(
                                    if (isCurrent) Color(0xFF00E5FF) else Color(0xFF262538),
                                    RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$beatNum",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isCurrent) Color.Black else Color.LightGray
                            )
                        }
                    }
                }

                // Phase Meter indicator
                PhaseMeterBar(
                    phase = beatPhase,
                    phaseDiff = beatPhaseDiff,
                    isPlaying = isPlaying,
                    modifier = Modifier.width(70.dp).height(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // MULTI-COLOR CDJ WAVEFORM & PLAYHEAD
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF06060A))
                    .clickable {
                        // Tap to jump on waveform
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val centerY = h / 2f
                    val progress = if (playbackDuration > 0) playbackPosition.toFloat() / playbackDuration else 0f
                    val bars = 60

                    for (i in 0 until bars) {
                        val barX = (i.toFloat() / bars) * w
                        val amp = (kotlin.math.sin(i * 0.45) * 0.5 + 0.5).toFloat() * 0.8f + 0.15f
                        val barHeight = h * amp

                        val isPlayed = (i.toFloat() / bars) <= progress
                        val barColor = when {
                            isPlayed -> Color(0xFF00E5FF)
                            i % 4 == 0 -> Color(0xFFFF5252) // Beat marker
                            else -> Color(0xFF3949AB)
                        }

                        drawLine(
                            color = barColor,
                            start = Offset(barX, centerY - barHeight / 2f),
                            end = Offset(barX, centerY + barHeight / 2f),
                            strokeWidth = 3.dp.toPx()
                        )
                    }

                    // Playhead Line
                    val playheadX = progress * w
                    drawLine(
                        color = Color.White,
                        start = Offset(playheadX, 0f),
                        end = Offset(playheadX, h),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ==========================================
            // TIME, BPM & PITCH READOUT
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // BPM & Pitch
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$effectiveBpm",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = " BPM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedPitch,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Time Elapsed & Remaining
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatMmSs(playbackPosition),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    val remaining = (playbackDuration - playbackPosition).coerceAtLeast(0)
                    Text(
                        text = " / -${formatMmSs(remaining)}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun PhaseMeterBar(
    phase: Float,
    phaseDiff: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val midX = w / 2f

        // Draw background channel
        drawRoundRect(
            color = Color(0xFF1E1D2D),
            size = size
        )

        // Center target line
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = Offset(midX, 0f),
            end = Offset(midX, h),
            strokeWidth = 1.5.dp.toPx()
        )

        if (isPlaying) {
            // Indicator of phase diff
            val markerX = (midX + (phaseDiff * midX * 0.9f)).coerceIn(2f, w - 2f)
            val isInSync = kotlin.math.abs(phaseDiff) < 0.15f

            drawCircle(
                color = if (isInSync) Color(0xFF00E676) else Color(0xFFFF9100),
                radius = 4.dp.toPx(),
                center = Offset(markerX, h / 2f)
            )
        }
    }
}

/**
 * Pioneer Loop Control Section: IN, OUT, EXIT, Auto Loop chips (1/2, 1, 2, 4, 8, 16), 1/2X and 2X.
 */
@Composable
fun PioneerLoopSection(
    isLoopActive: Boolean,
    activeLoopBeats: Float?,
    onLoopIn: () -> Unit,
    onLoopOut: () -> Unit,
    onExitLoop: () -> Unit,
    onAutoLoop: (Float) -> Unit,
    onHalveLoop: () -> Unit,
    onDoubleLoop: () -> Unit,
    themeColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF151420), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LOOP IN Button (Yellow)
            Surface(
                onClick = onLoopIn,
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF4A3E00),
                border = BorderStroke(1.dp, Color(0xFFFFD600)),
                modifier = Modifier.weight(1f)
            ) {
                Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                    Text("LOOP IN", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD600))
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // LOOP OUT Button (Yellow)
            Surface(
                onClick = onLoopOut,
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF4A3E00),
                border = BorderStroke(1.dp, Color(0xFFFFD600)),
                modifier = Modifier.weight(1f)
            ) {
                Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                    Text("LOOP OUT", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD600))
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // RELOOP / EXIT Button
            Surface(
                onClick = onExitLoop,
                shape = RoundedCornerShape(6.dp),
                color = if (isLoopActive) Color(0xFFFFD600) else Color(0xFF262534),
                border = BorderStroke(1.dp, if (isLoopActive) Color.White else Color.Transparent),
                modifier = Modifier.weight(1.2f)
            ) {
                Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isLoopActive) "EXIT LOOP" else "RELOOP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isLoopActive) Color.Black else Color.LightGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Auto Beat Loop Selector: 1/2, 1, 2, 4, 8, 16 beats + 1/2X, 2X
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Halve Loop
            TextButton(
                onClick = onHalveLoop,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.weight(0.9f).height(28.dp)
            ) {
                Text("½X", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            listOf(0.5f to "½", 1f to "1", 2f to "2", 4f to "4", 8f to "8", 16f to "16").forEach { (beats, label) ->
                val isSelected = isLoopActive && activeLoopBeats == beats
                Surface(
                    onClick = { onAutoLoop(beats) },
                    shape = RoundedCornerShape(4.dp),
                    color = if (isSelected) Color(0xFFFFD600) else Color(0xFF232232),
                    modifier = Modifier.weight(1f).height(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color.LightGray
                        )
                    }
                }
            }

            // Double Loop
            TextButton(
                onClick = onDoubleLoop,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.weight(0.9f).height(28.dp)
            ) {
                Text("2X", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun BadgePill(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

private fun formatMmSs(ms: Int): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
