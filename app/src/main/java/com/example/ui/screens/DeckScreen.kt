package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.viewmodels.LocalMusicViewModel
import com.example.ui.viewmodels.LocalTrack
import com.example.ui.viewmodels.DjSkin
import com.example.ui.viewmodels.DeskType
import com.example.ui.viewmodels.EqualizerBand

@Composable
fun DeckScreen(viewModel: LocalMusicViewModel) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackPosition by viewModel.playbackPosition.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val isLoop by viewModel.isLoop.collectAsState()
    val isAutoDjEnabled by viewModel.isAutoDjEnabled.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val ch1Level by viewModel.ch1Level.collectAsState()
    val ch2Level by viewModel.ch2Level.collectAsState()
    val masterLevel by viewModel.masterLevel.collectAsState()
    val crossfader by viewModel.crossfader.collectAsState()
    val pitch by viewModel.pitch.collectAsState()
    val equalizerBands by viewModel.equalizerBands.collectAsState()
    val isBassKilled by viewModel.isBassKilled.collectAsState()
    val isMidKilled by viewModel.isMidKilled.collectAsState()
    val isHighKilled by viewModel.isHighKilled.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val beatLevel by viewModel.beatLevel.collectAsState()
    val playbackDuration by viewModel.playbackDuration.collectAsState()
    val lyrics by viewModel.lyrics.collectAsState()
    val isLoadingLyrics by viewModel.isLoadingLyrics.collectAsState()
    val currentLyricsLine by viewModel.currentLyricsLine.collectAsState()

    val availableSkins by viewModel.availableSkins.collectAsState()
    val currentSkin by viewModel.currentSkin.collectAsState()
    
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var showLyricsDialog by remember { mutableStateOf(false) }

    val backgrounds = remember {
        listOf(
            com.example.R.drawable.img_dj_bg_1,
            com.example.R.drawable.img_dj_bg_2,
            com.example.R.drawable.img_dj_bg_3
        )
    }
    var currentBgIndex by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000L)
            var nextIndex = backgrounds.indices.random()
            while (nextIndex == currentBgIndex && backgrounds.size > 1) {
                nextIndex = backgrounds.indices.random()
            }
            currentBgIndex = nextIndex
        }
    }

    val primaryColor = Color(currentSkin.primaryColorHex)
    val accentColor = Color(currentSkin.accentColorHex)
    val backgroundColor = Color(currentSkin.backgroundColorHex)
    val surfaceColor = Color(currentSkin.surfaceColorHex)

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = backgrounds[currentBgIndex],
            animationSpec = tween(1500),
            label = "bg_crossfade"
        ) { bgRes ->
            AsyncImage(
                model = bgRes,
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor.copy(alpha = 0.85f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .displayCutoutPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .widthIn(max = 760.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hauptdeck",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.background(Color(0xFFE040FB), CircleShape)
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = "Export Mix", tint = Color.Black)
                }
            }

            // Skin Selector
            SkinSelector(
                availableSkins = availableSkins,
                currentSkin = currentSkin,
                onSkinSelected = { viewModel.selectSkin(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Visual Deck Display
            DeckDisplay(
                currentSkin = currentSkin,
                isPlaying = isPlaying,
                playbackPosition = playbackPosition,
                audioLevel = audioLevel,
                beatLevel = beatLevel,
                ch1Level = ch1Level,
                ch2Level = ch2Level,
                crossfader = crossfader,
                primaryColor = primaryColor,
                accentColor = accentColor,
                surfaceColor = surfaceColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Track Info
            TrackInfo(currentTrack = currentTrack)

            Spacer(modifier = Modifier.height(8.dp))

            // Current Lyric Preview
            CurrentLyricPreview(lyrics = lyrics, currentLine = currentLyricsLine)

            Spacer(modifier = Modifier.height(16.dp))

            // Seek Bar and Time
            SeekBarAndTimer(
                playbackPosition = playbackPosition,
                playbackDuration = playbackDuration,
                onSeek = { viewModel.seekTo(it) },
                primaryColor = primaryColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Playback Controls
            PlaybackControls(
                isPlaying = isPlaying,
                isShuffle = isShuffle,
                isLoop = isLoop,
                isAutoDjEnabled = isAutoDjEnabled,
                onTogglePlay = { viewModel.togglePlayPause() },
                onSkipNext = { viewModel.playNext(context) },
                onSkipPrev = { viewModel.playPrevious(context) },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleLoop = { viewModel.toggleLoop() },
                onToggleAutoDj = { viewModel.toggleAutoDj() },
                onShowLyrics = { 
                    viewModel.fetchLyrics()
                    showLyricsDialog = true
                },
                primaryColor = primaryColor,
                surfaceColor = surfaceColor
            )



            // Mixer Section
            MixerSection(
                ch1Level = ch1Level,
                ch2Level = ch2Level,
                masterLevel = masterLevel,
                volume = volume,
                crossfader = crossfader,
                pitch = pitch,
                onCh1Change = { viewModel.setCh1Level(it) },
                onCh2Change = { viewModel.setCh2Level(it) },
                onMasterChange = { viewModel.setMasterLevel(it) },
                onVolumeChange = { viewModel.setVolume(it) },
                onCrossfaderChange = { viewModel.setCrossfader(it) },
                onPitchChange = { viewModel.setPitch(it) },
                primaryColor = primaryColor,
                surfaceColor = surfaceColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Equalizer Section
            if (equalizerBands.isNotEmpty()) {
                EqualizerSection(
                    bands = equalizerBands,
                    isPlaying = isPlaying,
                    playbackPosition = playbackPosition,
                    audioLevel = audioLevel,
                    isBassKilled = isBassKilled,
                    isMidKilled = isMidKilled,
                    isHighKilled = isHighKilled,
                    onToggleBassKill = { viewModel.toggleBassKill() },
                    onToggleMidKill = { viewModel.toggleMidKill() },
                    onToggleHighKill = { viewModel.toggleHighKill() },
                    onResetKills = { viewModel.resetAllKills() },
                    primaryColor = primaryColor,
                    surfaceColor = surfaceColor,
                    onBandChange = { band, level -> viewModel.setEqualizerBandLevel(band, level.toShort()) }
                )
            }
        }

        if (showLyricsDialog) {
            LyricsDialog(
                title = currentTrack?.title ?: "Unbekannt",
                lyrics = lyrics,
                currentLine = currentLyricsLine,
                isLoading = isLoadingLyrics,
                onDismiss = { showLyricsDialog = false },
                backgroundColor = backgroundColor
            )
        }

        if (showExportDialog) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showExportDialog = false }) {
                AudioRecorderComponent(
                    onRecordingFinished = {
                        showExportDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun SkinSelector(
    availableSkins: List<DjSkin>,
    currentSkin: DjSkin,
    onSkinSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        availableSkins.forEach { skin ->
            val isSelected = skin.id == currentSkin.id
            val skinBgColor = if (isSelected) Color(skin.primaryColorHex).copy(alpha = 0.25f) else Color(0xFF1E1E1E)
            val skinBorderColor = if (isSelected) Color(skin.primaryColorHex) else Color.DarkGray
            
            Surface(
                onClick = { onSkinSelected(skin.id) },
                shape = RoundedCornerShape(16.dp),
                color = skinBgColor,
                border = BorderStroke(1.5.dp, skinBorderColor),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(skin.primaryColorHex), CircleShape)
                    )
                    Text(
                        text = skin.name,
                        color = if (isSelected) Color.White else Color.Gray,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun DeckDisplay(
    currentSkin: DjSkin,
    isPlaying: Boolean,
    playbackPosition: Int,
    audioLevel: Float,
    beatLevel: Float,
    ch1Level: Float,
    ch2Level: Float,
    crossfader: Float,
    primaryColor: Color,
    accentColor: Color,
    surfaceColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "deck_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )
    val rotation = if (isPlaying) angle else 0f

    Box(
        modifier = Modifier.size(320.dp),
        contentAlignment = Alignment.Center
    ) {
        when (currentSkin.deskType) {
            DeskType.VINYL_TURNTABLE -> {
            VinylTurntable(rotation, isPlaying, audioLevel, beatLevel, playbackPosition, primaryColor, accentColor)
            }
            DeskType.DIGITAL_CDJ -> {
            DigitalCdj(rotation, isPlaying, audioLevel, beatLevel, primaryColor, accentColor)
            }
            DeskType.MIDI_LAUNCHPAD -> {
            MidiLaunchpad(isPlaying, audioLevel, beatLevel, playbackPosition, primaryColor, accentColor, surfaceColor)
            }
            DeskType.STUDIO_MIXER -> {
                StudioMixerView(isPlaying, audioLevel, beatLevel, ch1Level, ch2Level, crossfader, primaryColor)
            }
        }
    }
}

@Composable
fun VinylTurntable(
    rotation: Float,
    isPlaying: Boolean,
    audioLevel: Float,
    beatLevel: Float,
    playbackPosition: Int,
    primaryColor: Color,
    accentColor: Color
) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(320.dp)) {
            val radius = size.width / 2f
            val barWidth = 4.dp.toPx()
            val innerRadius = radius - 30.dp.toPx()
            
            for (i in 0 until 48) {
                val angleRad = (i * 360f / 48) * (Math.PI / 180f).toFloat()
                val randomFactor = kotlin.math.sin((i * 10f) + (playbackPosition / 100f)) * 0.5f + 0.5f
                // Mix audioLevel and beatLevel for visualizer bars
                val activity = (audioLevel * 0.5f + beatLevel * 1.0f).coerceIn(0f, 1.5f)
                val heightValue = if (isPlaying) (activity * randomFactor).coerceIn(0.1f, 1f) else 0.1f
                val barHeight = heightValue * 40.dp.toPx()
                
                val startX = center.x + innerRadius * kotlin.math.cos(angleRad)
                val startY = center.y + innerRadius * kotlin.math.sin(angleRad)
                val endX = center.x + (innerRadius + barHeight) * kotlin.math.cos(angleRad)
                val endY = center.y + (innerRadius + barHeight) * kotlin.math.sin(angleRad)
                
                drawLine(
                    color = primaryColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        Box(
            modifier = Modifier
                .size(220.dp)
                .rotate(rotation)
                .background(Color.Black, CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.width / 2f
                for (r in 10..90 step 10) {
                    drawCircle(
                        color = Color.DarkGray.copy(alpha = 0.3f),
                        radius = radius * (r / 100f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                drawCircle(color = accentColor, radius = radius * 0.25f)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.DarkGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.LibraryMusic, contentDescription = null, tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun DigitalCdj(
    rotation: Float,
    isPlaying: Boolean,
    audioLevel: Float,
    beatLevel: Float,
    primaryColor: Color,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .size(280.dp)
            .background(Color(0xFF1E1E24), shape = CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color.Black, radius = size.width / 2f)
            val ticksCount = 40
            for (i in 0 until ticksCount) {
                val angleRad = (i * 360f / ticksCount) * (Math.PI / 180f).toFloat()
                val innerR = (size.width / 2f) - 10.dp.toPx()
                val outerR = (size.width / 2f) - 2.dp.toPx()
                drawLine(
                    color = if (isPlaying && (i == (rotation / (360f / ticksCount)).toInt() % ticksCount)) primaryColor else Color.DarkGray,
                    start = Offset(center.x + innerR * kotlin.math.cos(angleRad), center.y + innerR * kotlin.math.sin(angleRad)),
                    end = Offset(center.x + outerR * kotlin.math.cos(angleRad), center.y + outerR * kotlin.math.sin(angleRad)),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        
        Box(
            modifier = Modifier
                .size(230.dp)
                .background(Color(0xFF121214), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (isPlaying) {
                    val activity = (audioLevel * 0.3f + beatLevel * 0.7f).coerceIn(0f, 1f)
                    drawCircle(
                        color = accentColor.copy(alpha = activity * 0.4f),
                        radius = (size.width / 2f) * activity
                    )
                }
            }
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(-rotation * 1.5f)
            ) {
                drawCircle(
                    color = accentColor.copy(alpha = 0.15f),
                    radius = size.width / 2.5f,
                    style = Stroke(width = 6.dp.toPx())
                )
                drawCircle(
                    color = accentColor,
                    radius = 8.dp.toPx(),
                    center = center.copy(y = center.y - size.width / 2.5f)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isPlaying) "PLAYING" else "CUE",
                    color = if (isPlaying) primaryColor else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun MidiLaunchpad(
    isPlaying: Boolean,
    audioLevel: Float,
    beatLevel: Float,
    playbackPosition: Int,
    primaryColor: Color,
    accentColor: Color,
    surfaceColor: Color
) {
    Column(
        modifier = Modifier
            .size(280.dp)
            .background(Color(0xFF101012), shape = RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in 0 until 4) {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until 4) {
                    val padIndex = row * 4 + col
                    val isPadGlowing = isPlaying && (
                        (playbackPosition / 250) % 16 == padIndex || 
                        (padIndex % 3 == (playbackPosition / 400) % 3) ||
                        (beatLevel > 0.5f && padIndex % 4 == (playbackPosition / 100) % 4) ||
                        (audioLevel > 0.6f && padIndex % 2 == 0)
                    )
                    val activity = (audioLevel * 0.4f + beatLevel * 0.6f).coerceIn(0f, 1f)
                    val padAlpha = if (isPadGlowing) (0.6f + activity * 0.4f).coerceIn(0f, 1f) else 0.5f
                    val padColor = if (isPadGlowing) {
                        if (padIndex % 2 == 0) primaryColor.copy(alpha = padAlpha) else accentColor.copy(alpha = padAlpha)
                    } else {
                        surfaceColor.copy(alpha = padAlpha)
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(padColor, shape = RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    color = if (isPadGlowing) Color.White else Color.DarkGray,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudioMixerView(
    isPlaying: Boolean,
    audioLevel: Float,
    beatLevel: Float,
    ch1Level: Float,
    ch2Level: Float,
    crossfader: Float,
    primaryColor: Color
) {
    Column(
        modifier = Modifier
            .size(width = 300.dp, height = 280.dp)
            .background(Color(0xFF18181A), shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            for (chan in 1..3) {
                if (chan < 3) {
                    val chanLevel = if (chan == 1) ch1Level else ch2Level
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CH $chan",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Black, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val baseAngle = chan * 60f
                            // React to beat for dials too
                            val bounce = if (isPlaying) (beatLevel * 45f) else 0f
                            val dialAngle = 30f + baseAngle + bounce
                            Canvas(modifier = Modifier.fillMaxSize().rotate(dialAngle)) {
                                drawLine(
                                    color = primaryColor,
                                    start = center,
                                    end = center.copy(y = center.y - 14.dp.toPx()),
                                    strokeWidth = 3.dp.toPx()
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .weight(1f)
                                .background(Color.Black, RoundedCornerShape(6.dp))
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Fader level is a combination of set level and audio/beat activity
                            val activity = if (isPlaying) (audioLevel * 0.2f + beatLevel * 0.3f) else 0f
                            val faderLevel = (chanLevel * 0.7f + activity).coerceIn(0.1f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(faderLevel)
                                    .background(primaryColor, RoundedCornerShape(6.dp))
                            )
                        }
                    }
                } else {
                    // VU Meter - Highly reactive to beat
                    Column(
                        modifier = Modifier.width(40.dp).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("VU", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        for (i in 0 until 12) {
                            val ledLevel = (12 - i) / 12f
                            // VU meter reacts more to beat (bass) for that "punchy" look
                            val isLit = isPlaying && (beatLevel * 1.25f >= ledLevel || audioLevel * 0.8f >= ledLevel)
                            val ledColor = when {
                                i < 3 -> if (isLit) Color.Red else Color.Red.copy(alpha = 0.2f)
                                i < 6 -> if (isLit) Color.Yellow else Color.Yellow.copy(alpha = 0.2f)
                                else -> if (isLit) Color.Green else Color.Green.copy(alpha = 0.2f)
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(ledColor, RoundedCornerShape(1.dp)))
                        }
                    }
                }
            }
        }
        
        // Horizontal Crossfader Visual
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.Black, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.1f)
                        .graphicsLayer { 
                            translationX = (crossfader * 0.9f) * size.width 
                        }
                        .background(Color.White, RoundedCornerShape(2.dp))
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("L", color = Color.Gray, fontSize = 8.sp)
                Text("R", color = Color.Gray, fontSize = 8.sp)
            }
        }
    }
}

@Composable
fun SeekBarAndTimer(
    playbackPosition: Int,
    playbackDuration: Int,
    onSeek: (Int) -> Unit,
    primaryColor: Color
) {
    val currentSeconds = (playbackPosition / 1000) % 60
    val currentMinutes = (playbackPosition / 1000) / 60
    val totalSeconds = (playbackDuration / 1000) % 60
    val totalMinutes = (playbackDuration / 1000) / 60

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = playbackPosition.toFloat(),
            onValueChange = { onSeek(it.toInt()) },
            valueRange = 0f..(if (playbackDuration > 0) playbackDuration.toFloat() else 1f),
            modifier = Modifier.fillMaxWidth().height(24.dp),
            colors = SliderDefaults.colors(
                thumbColor = primaryColor,
                activeTrackColor = primaryColor,
                inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "%02d:%02d".format(currentMinutes, currentSeconds),
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "%02d:%02d".format(totalMinutes, totalSeconds),
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun CurrentLyricPreview(lyrics: String?, currentLine: Int) {
    val lines = lyrics?.lines()?.filter { it.isNotBlank() } ?: emptyList()
    val text = if (lines.isNotEmpty() && currentLine >= 0 && currentLine < lines.size) {
        lines[currentLine]
    } else {
        ""
    }
    
    if (text.isNotEmpty()) {
        Text(
            text = text,
            color = Color.Cyan,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TrackInfo(currentTrack: LocalTrack?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.DarkGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.LibraryMusic, contentDescription = null, tint = Color.LightGray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = currentTrack?.title ?: "Kein Titel ausgewählt",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentTrack?.artist ?: "Wähle einen Track in der Library",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PlaybackControls(
    isPlaying: Boolean,
    isShuffle: Boolean,
    isLoop: Boolean,
    isAutoDjEnabled: Boolean,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrev: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleLoop: () -> Unit,
    onToggleAutoDj: () -> Unit,
    onShowLyrics: () -> Unit,
    primaryColor: Color,
    surfaceColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleShuffle) {
                Icon(Icons.Filled.Shuffle, contentDescription = "Shuffle", tint = if (isShuffle) primaryColor else Color.Gray)
            }
            IconButton(onClick = onSkipPrev, modifier = Modifier.testTag("prev_button")) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Prev", tint = Color.White)
            }
            FloatingActionButton(
                onClick = onTogglePlay,
                containerColor = primaryColor,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.testTag("play_pause_button")
            ) {
                Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = "Play/Pause")
            }
            IconButton(onClick = onSkipNext, modifier = Modifier.testTag("next_button")) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = Color.White)
            }
            IconButton(onClick = onToggleLoop) {
                Icon(Icons.Filled.Repeat, contentDescription = "Loop", tint = if (isLoop) primaryColor else Color.Gray)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onToggleAutoDj,
                border = BorderStroke(1.dp, if (isAutoDjEnabled) primaryColor else Color.Gray),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isAutoDjEnabled) primaryColor else Color.Gray
                ),
                modifier = Modifier.testTag("auto_dj_button")
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isAutoDjEnabled) "Auto-DJ: AN" else "Auto-DJ: AUS", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedButton(
                onClick = onShowLyrics,
                border = BorderStroke(1.dp, Color.Gray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.testTag("lyrics_button")
            ) {
                Text("Songtext", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MixerSection(
    ch1Level: Float,
    ch2Level: Float,
    masterLevel: Float,
    volume: Float,
    crossfader: Float,
    pitch: Float,
    onCh1Change: (Float) -> Unit,
    onCh2Change: (Float) -> Unit,
    onMasterChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onCrossfaderChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    primaryColor: Color,
    surfaceColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Mixer & Master", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.height(180.dp)) {
                MixerFader("CH 1", ch1Level, onCh1Change, primaryColor, Modifier.weight(1f))
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("PITCH", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Slider(
                        value = pitch,
                        onValueChange = onPitchChange,
                        valueRange = 0.5f..1.5f,
                        modifier = Modifier.weight(1f).graphicsLayer { 
                            rotationZ = 270f
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                        },
                        colors = SliderDefaults.colors(thumbColor = Color.Cyan, activeTrackColor = Color.Cyan)
                    )
                }
                MixerFader("CH 2", ch2Level, onCh2Change, primaryColor, Modifier.weight(1f))
                MixerFader("MASTER", masterLevel, onMasterChange, Color.Red, Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Crossfader Component
            CrossfaderComponent(
                crossfaderValue = crossfader,
                onCrossfaderChange = onCrossfaderChange,
                trackAName = "Deck 1 (Channel 1)",
                trackBName = "Deck 2 (Channel 2)",
                primaryColor = primaryColor,
                surfaceColor = surfaceColor
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = primaryColor, activeTrackColor = primaryColor)
                )
            }
        }
    }
}

@Composable
fun MixerFader(label: String, value: Float, onValueChange: (Float) -> Unit, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f).graphicsLayer { 
                rotationZ = 270f
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            },
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
        )
    }
}

@Composable
fun EqualizerSection(
    bands: List<EqualizerBand>,
    isPlaying: Boolean,
    playbackPosition: Int,
    audioLevel: Float,
    isBassKilled: Boolean,
    isMidKilled: Boolean,
    isHighKilled: Boolean,
    onToggleBassKill: () -> Unit,
    onToggleMidKill: () -> Unit,
    onToggleHighKill: () -> Unit,
    onResetKills: () -> Unit,
    primaryColor: Color,
    surfaceColor: Color,
    onBandChange: (Short, Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Equalizer & Kill-Knöpfe", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            if (isBassKilled || isMidKilled || isHighKilled) {
                TextButton(
                    onClick = onResetKills,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("RESET ALL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Prominent DJ Frequency Band Kill Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KillButton(
                label = "BASS KILL",
                freqLabel = "20 - 300 Hz",
                isKilled = isBassKilled,
                activeColor = Color(0xFFFF1744),
                onClick = onToggleBassKill,
                modifier = Modifier.weight(1f)
            )
            KillButton(
                label = "MID KILL",
                freqLabel = "300 - 2.5k Hz",
                isKilled = isMidKilled,
                activeColor = Color(0xFFFFC400),
                onClick = onToggleMidKill,
                modifier = Modifier.weight(1f)
            )
            KillButton(
                label = "HI KILL",
                freqLabel = "2.5k - 20k Hz",
                isKilled = isHighKilled,
                activeColor = Color(0xFF00E5FF),
                onClick = onToggleHighKill,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            bands.forEach { band ->
                val staticProgress = (band.currentLevel - band.minLevel).toFloat() / (band.maxLevel - band.minLevel).toFloat()
                val randomFactor = kotlin.math.sin((band.band * 10f) + (playbackPosition / 100f)) * 0.5f + 0.5f
                val spectrumOffset = if (isPlaying) (audioLevel * randomFactor) else 0f
                val progress = (staticProgress * 0.5f + spectrumOffset * 0.5f).coerceIn(0f, 1f)
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(surfaceColor),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(progress)
                            .background(primaryColor)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column {
            bands.forEach { band ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${band.centerFreq} Hz", color = Color.Gray, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(50.dp))
                    Slider(
                        value = band.currentLevel.toFloat(),
                        onValueChange = { onBandChange(band.band, it.toInt()) },
                        valueRange = band.minLevel.toFloat()..band.maxLevel.toFloat(),
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = primaryColor, activeTrackColor = primaryColor)
                    )
                }
            }
        }
    }
}

@Composable
fun KillButton(
    label: String,
    freqLabel: String,
    isKilled: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isKilled) activeColor else Color(0xFF222228),
            contentColor = if (isKilled) Color.Black else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isKilled) 2.dp else 1.dp,
            color = if (isKilled) Color.White else Color.DarkGray
        ),
        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
        modifier = modifier.testTag("kill_button_${label.lowercase().replace(" ", "_")}")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (isKilled) Color.Black else Color.Gray,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = if (isKilled) "[OFF/KILLED]" else freqLabel,
                fontSize = 9.sp,
                color = if (isKilled) Color.Black.copy(alpha = 0.8f) else Color.Gray
            )
        }
    }
}

@Composable
fun LyricsDialog(
    title: String,
    lyrics: String?,
    currentLine: Int,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    backgroundColor: Color
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Songtext: $title") },
        text = {
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    val lines = lyrics?.lines()?.filter { it.isNotBlank() } ?: emptyList()
                    if (lines.isEmpty()) {
                        Text(text = "Kein Songtext verfügbar.", color = Color.Gray)
                    } else {
                        val listState = rememberLazyListState()
                        
                        // Auto-scroll to current line
                        LaunchedEffect(currentLine) {
                            if (lines.isNotEmpty()) {
                                listState.animateScrollToItem(currentLine.coerceIn(0, lines.size - 1))
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(lines) { index, line ->
                                val isCurrent = index == currentLine
                                Text(
                                    text = line,
                                    style = if (isCurrent) 
                                        MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        ) 
                                    else 
                                        MaterialTheme.typography.bodyMedium,
                                    color = if (isCurrent) Color.Cyan else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Schließen") }
        },
        containerColor = backgroundColor,
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

