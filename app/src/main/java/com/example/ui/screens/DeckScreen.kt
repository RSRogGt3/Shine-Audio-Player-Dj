package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DeckScreen(
    viewModel: LocalMusicViewModel,
    onNavigateToEqualizer: () -> Unit = {}
) {
    val context = LocalContext.current

    // Media & Microphone permissions for audio playback, video selection and live visualizer
    val mediaAudioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val permissionsToRequest = remember {
        val list = mutableListOf(mediaAudioPermission, Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.READ_MEDIA_VIDEO)
        }
        list
    }
    val permissionState = rememberMultiplePermissionsState(permissions = permissionsToRequest)

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            viewModel.fetchLocalMusic(context)
        }
    }
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackPosition by viewModel.playbackPosition.collectAsState()
    val playbackDuration by viewModel.playbackDuration.collectAsState()

    // Deck A states
    val trackA by viewModel.currentTrackA.collectAsState()
    val isPlayingA by viewModel.isPlayingA.collectAsState()
    val posA by viewModel.playbackPositionA.collectAsState()
    val durA by viewModel.playbackDurationA.collectAsState()
    val pitchA by viewModel.pitchA.collectAsState()
    val bpmA by viewModel.bpmA.collectAsState()
    val audioLevelA by viewModel.audioLevelA.collectAsState()
    val beatLevelA by viewModel.beatLevelA.collectAsState()
    val isLoopA by viewModel.isLoopA.collectAsState()

    // Deck B states
    val trackB by viewModel.currentTrackB.collectAsState()
    val isPlayingB by viewModel.isPlayingB.collectAsState()
    val posB by viewModel.playbackPositionB.collectAsState()
    val durB by viewModel.playbackDurationB.collectAsState()
    val pitchB by viewModel.pitchB.collectAsState()
    val bpmB by viewModel.bpmB.collectAsState()
    val audioLevelB by viewModel.audioLevelB.collectAsState()
    val beatLevelB by viewModel.beatLevelB.collectAsState()
    val isLoopB by viewModel.isLoopB.collectAsState()

    // Pioneer DJ: Sync, Master, Beatlock, Beatanzeige, Loops, Hot Cues, Video Mode
    val masterDeck by viewModel.masterDeck.collectAsState()
    val isSyncA by viewModel.isSyncA.collectAsState()
    val isSyncB by viewModel.isSyncB.collectAsState()
    val isBeatLockA by viewModel.isBeatLockA.collectAsState()
    val isBeatLockB by viewModel.isBeatLockB.collectAsState()
    val currentBeatA by viewModel.currentBeatA.collectAsState()
    val currentBeatB by viewModel.currentBeatB.collectAsState()
    val beatPhaseA by viewModel.beatPhaseA.collectAsState()
    val beatPhaseB by viewModel.beatPhaseB.collectAsState()
    val beatPhaseDiff by viewModel.beatPhaseDiff.collectAsState()
    val isLoopActiveA by viewModel.isLoopActiveA.collectAsState()
    val isLoopActiveB by viewModel.isLoopActiveB.collectAsState()
    val activeLoopBeatsA by viewModel.activeLoopBeatsA.collectAsState()
    val activeLoopBeatsB by viewModel.activeLoopBeatsB.collectAsState()
    val hotCuesA by viewModel.hotCuesA.collectAsState()
    val hotCuesB by viewModel.hotCuesB.collectAsState()
    val deckVideoModeA by viewModel.deckVideoModeA.collectAsState()
    val deckVideoModeB by viewModel.deckVideoModeB.collectAsState()

    val deckViewMode by viewModel.deckViewMode.collectAsState()

    val isShuffle by viewModel.isShuffle.collectAsState()
    val isLoop by viewModel.isLoop.collectAsState()
    val isAutoDjEnabled by viewModel.isAutoDjEnabled.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val ch1Level by viewModel.ch1Level.collectAsState()
    val ch2Level by viewModel.ch2Level.collectAsState()
    val masterLevel by viewModel.masterLevel.collectAsState()
    val crossfader by viewModel.crossfader.collectAsState()
    val pitch by viewModel.pitch.collectAsState()
    val isBassKilled by viewModel.isBassKilled.collectAsState()
    val isMidKilled by viewModel.isMidKilled.collectAsState()
    val isHighKilled by viewModel.isHighKilled.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val beatLevel by viewModel.beatLevel.collectAsState()
    val lyrics by viewModel.lyrics.collectAsState()
    val isLoadingLyrics by viewModel.isLoadingLyrics.collectAsState()
    val currentLyricsLine by viewModel.currentLyricsLine.collectAsState()

    val localTracks by viewModel.localTracks.collectAsState()
    val localVideos by viewModel.localVideos.collectAsState()

    val availableSkins by viewModel.availableSkins.collectAsState()
    val currentSkin by viewModel.currentSkin.collectAsState()
    
    var showExportDialog by remember { mutableStateOf(false) }
    var showLyricsDialog by remember { mutableStateOf(false) }
    var showTrackPickerForDeck by remember { mutableStateOf<String?>(null) } // "A" or "B"

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
                .widthIn(max = 840.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DJ Live-Mischpult",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "2 Decks parallel auflegen • MP3 & MP4 Mix",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.saveCurrentSettings(context) },
                        modifier = Modifier.background(primaryColor, CircleShape)
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = "Einstellungen speichern", tint = Color.Black)
                    }

                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.background(Color(0xFFE040FB), CircleShape)
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = "Export Mix", tint = Color.Black)
                    }
                }
            }

            // Skin Selector
            SkinSelector(
                availableSkins = availableSkins,
                currentSkin = currentSkin,
                onSkinSelected = { viewModel.selectSkin(it) }
            )

            // Permissions Check & Status Banner
            if (!permissionState.allPermissionsGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                val isPermanentlyDenied = permissionState.permissions.any { it.status is PermissionStatus.Denied && !it.status.shouldShowRationale }
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deck_permission_banner"),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF21151A),
                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Berechtigungen erforderlich",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isPermanentlyDenied) 
                                        "Dateizugriff in Einstellungen aktivieren für eigene MP3s & MP4s" 
                                    else 
                                        "Zugriff auf Audio/Video & Mikrofon (Visualizer) erlauben",
                                    fontSize = 10.sp,
                                    color = Color.LightGray,
                                    maxLines = 2
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (isPermanentlyDenied) {
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Einstellungen", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = { permissionState.launchMultiplePermissionRequest() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("deck_grant_permission_btn")
                            ) {
                                Text("Erlauben", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Deck View Switcher: [ DUAL DECKS A & B ] [ DECK A ] [ DECK B ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161520))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "DUAL" to "DUAL DECKS (A & B)",
                    "DECK_A" to "DECK A (CH 1)",
                    "DECK_B" to "DECK B (CH 2)"
                ).forEach { (mode, label) ->
                    val isSelected = deckViewMode == mode
                    Surface(
                        onClick = { viewModel.setDeckViewMode(mode) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) primaryColor else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color.LightGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DUAL DECKS VIEW OR FOCUSED SINGLE DECK VIEW
            if (deckViewMode == "DUAL") {
                // Two CDJ Decks with Central DJM Mixer Console
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // PIONEER CDJ DECK A (CH 1)
                    CdjDeckComponent(
                        deckId = "A",
                        channelNumber = 1,
                        track = trackA,
                        isPlaying = isPlayingA,
                        playbackPosition = posA,
                        playbackDuration = durA,
                        bpm = bpmA,
                        pitch = pitchA,
                        isMaster = masterDeck == "A",
                        isSync = isSyncA,
                        isBeatLock = isBeatLockA,
                        currentBeat = currentBeatA,
                        beatPhase = beatPhaseA,
                        beatPhaseDiff = beatPhaseDiff,
                        isLoopActive = isLoopActiveA,
                        activeLoopBeats = activeLoopBeatsA,
                        hotCues = hotCuesA,
                        themeColor = Color(0xFF00E5FF),
                        surfaceColor = surfaceColor,
                        currentSkin = currentSkin,
                        videoMode = deckVideoModeA,
                        onToggleVideoMode = { viewModel.toggleDeckVideoModeA() },
                        onLoadTrack = { showTrackPickerForDeck = "A" },
                        onTogglePlay = { viewModel.togglePlayPauseA() },
                        onCue = { viewModel.cueDeckA() },
                        onToggleSync = { viewModel.toggleSyncA() },
                        onSetMaster = { viewModel.setMasterDeck("A") },
                        onToggleBeatLock = { viewModel.toggleBeatLockA() },
                        onPitchChange = { viewModel.setPitchA(it) },
                        onNudgeMinus = { viewModel.nudgeA(false) },
                        onNudgePlus = { viewModel.nudgeA(true) },
                        onSeek = { viewModel.seekToA(it) },
                        onLoopIn = { viewModel.setLoopInA() },
                        onLoopOut = { viewModel.setLoopOutA() },
                        onExitLoop = { viewModel.exitLoopA() },
                        onAutoLoop = { viewModel.setAutoLoopA(it) },
                        onHalveLoop = { viewModel.halveLoopA() },
                        onDoubleLoop = { viewModel.doubleLoopA() },
                        onTriggerHotCue = { viewModel.triggerHotCueA(it) },
                        onClearHotCue = { viewModel.clearHotCueA(it) },
                        viewModel = viewModel
                    )

                    // PIONEER CENTRAL DJM MISCHPULT
                    DjMixerComponent(
                        viewModel = viewModel,
                        primaryColor = Color(0xFF00E5FF),
                        accentColor = Color(0xFFFF007F)
                    )

                    // PIONEER CDJ DECK B (CH 2)
                    CdjDeckComponent(
                        deckId = "B",
                        channelNumber = 2,
                        track = trackB,
                        isPlaying = isPlayingB,
                        playbackPosition = posB,
                        playbackDuration = durB,
                        bpm = bpmB,
                        pitch = pitchB,
                        isMaster = masterDeck == "B",
                        isSync = isSyncB,
                        isBeatLock = isBeatLockB,
                        currentBeat = currentBeatB,
                        beatPhase = beatPhaseB,
                        beatPhaseDiff = beatPhaseDiff,
                        isLoopActive = isLoopActiveB,
                        activeLoopBeats = activeLoopBeatsB,
                        hotCues = hotCuesB,
                        themeColor = Color(0xFFFF007F),
                        surfaceColor = surfaceColor,
                        currentSkin = currentSkin,
                        videoMode = deckVideoModeB,
                        onToggleVideoMode = { viewModel.toggleDeckVideoModeB() },
                        onLoadTrack = { showTrackPickerForDeck = "B" },
                        onTogglePlay = { viewModel.togglePlayPauseB() },
                        onCue = { viewModel.cueDeckB() },
                        onToggleSync = { viewModel.toggleSyncB() },
                        onSetMaster = { viewModel.setMasterDeck("B") },
                        onToggleBeatLock = { viewModel.toggleBeatLockB() },
                        onPitchChange = { viewModel.setPitchB(it) },
                        onNudgeMinus = { viewModel.nudgeB(false) },
                        onNudgePlus = { viewModel.nudgeB(true) },
                        onSeek = { viewModel.seekToB(it) },
                        onLoopIn = { viewModel.setLoopInB() },
                        onLoopOut = { viewModel.setLoopOutB() },
                        onExitLoop = { viewModel.exitLoopB() },
                        onAutoLoop = { viewModel.setAutoLoopB(it) },
                        onHalveLoop = { viewModel.halveLoopB() },
                        onDoubleLoop = { viewModel.doubleLoopB() },
                        onTriggerHotCue = { viewModel.triggerHotCueB(it) },
                        onClearHotCue = { viewModel.clearHotCueB(it) },
                        viewModel = viewModel
                    )
                }
            } else if (deckViewMode == "DECK_A") {
                // Focused Single Deck A + Mixer
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CdjDeckComponent(
                        deckId = "A",
                        channelNumber = 1,
                        track = trackA,
                        isPlaying = isPlayingA,
                        playbackPosition = posA,
                        playbackDuration = durA,
                        bpm = bpmA,
                        pitch = pitchA,
                        isMaster = masterDeck == "A",
                        isSync = isSyncA,
                        isBeatLock = isBeatLockA,
                        currentBeat = currentBeatA,
                        beatPhase = beatPhaseA,
                        beatPhaseDiff = beatPhaseDiff,
                        isLoopActive = isLoopActiveA,
                        activeLoopBeats = activeLoopBeatsA,
                        hotCues = hotCuesA,
                        themeColor = Color(0xFF00E5FF),
                        surfaceColor = surfaceColor,
                        currentSkin = currentSkin,
                        videoMode = deckVideoModeA,
                        onToggleVideoMode = { viewModel.toggleDeckVideoModeA() },
                        onLoadTrack = { showTrackPickerForDeck = "A" },
                        onTogglePlay = { viewModel.togglePlayPauseA() },
                        onCue = { viewModel.cueDeckA() },
                        onToggleSync = { viewModel.toggleSyncA() },
                        onSetMaster = { viewModel.setMasterDeck("A") },
                        onToggleBeatLock = { viewModel.toggleBeatLockA() },
                        onPitchChange = { viewModel.setPitchA(it) },
                        onNudgeMinus = { viewModel.nudgeA(false) },
                        onNudgePlus = { viewModel.nudgeA(true) },
                        onSeek = { viewModel.seekToA(it) },
                        onLoopIn = { viewModel.setLoopInA() },
                        onLoopOut = { viewModel.setLoopOutA() },
                        onExitLoop = { viewModel.exitLoopA() },
                        onAutoLoop = { viewModel.setAutoLoopA(it) },
                        onHalveLoop = { viewModel.halveLoopA() },
                        onDoubleLoop = { viewModel.doubleLoopA() },
                        onTriggerHotCue = { viewModel.triggerHotCueA(it) },
                        onClearHotCue = { viewModel.clearHotCueA(it) },
                        viewModel = viewModel
                    )

                    DjMixerComponent(
                        viewModel = viewModel,
                        primaryColor = Color(0xFF00E5FF),
                        accentColor = Color(0xFFFF007F)
                    )
                }
            } else {
                // Focused Single Deck B + Mixer
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CdjDeckComponent(
                        deckId = "B",
                        channelNumber = 2,
                        track = trackB,
                        isPlaying = isPlayingB,
                        playbackPosition = posB,
                        playbackDuration = durB,
                        bpm = bpmB,
                        pitch = pitchB,
                        isMaster = masterDeck == "B",
                        isSync = isSyncB,
                        isBeatLock = isBeatLockB,
                        currentBeat = currentBeatB,
                        beatPhase = beatPhaseB,
                        beatPhaseDiff = beatPhaseDiff,
                        isLoopActive = isLoopActiveB,
                        activeLoopBeats = activeLoopBeatsB,
                        hotCues = hotCuesB,
                        themeColor = Color(0xFFFF007F),
                        surfaceColor = surfaceColor,
                        currentSkin = currentSkin,
                        videoMode = deckVideoModeB,
                        onToggleVideoMode = { viewModel.toggleDeckVideoModeB() },
                        onLoadTrack = { showTrackPickerForDeck = "B" },
                        onTogglePlay = { viewModel.togglePlayPauseB() },
                        onCue = { viewModel.cueDeckB() },
                        onToggleSync = { viewModel.toggleSyncB() },
                        onSetMaster = { viewModel.setMasterDeck("B") },
                        onToggleBeatLock = { viewModel.toggleBeatLockB() },
                        onPitchChange = { viewModel.setPitchB(it) },
                        onNudgeMinus = { viewModel.nudgeB(false) },
                        onNudgePlus = { viewModel.nudgeB(true) },
                        onSeek = { viewModel.seekToB(it) },
                        onLoopIn = { viewModel.setLoopInB() },
                        onLoopOut = { viewModel.setLoopOutB() },
                        onExitLoop = { viewModel.exitLoopB() },
                        onAutoLoop = { viewModel.setAutoLoopB(it) },
                        onHalveLoop = { viewModel.halveLoopB() },
                        onDoubleLoop = { viewModel.doubleLoopB() },
                        onTriggerHotCue = { viewModel.triggerHotCueB(it) },
                        onClearHotCue = { viewModel.clearHotCueB(it) },
                        viewModel = viewModel
                    )

                    DjMixerComponent(
                        viewModel = viewModel,
                        primaryColor = Color(0xFF00E5FF),
                        accentColor = Color(0xFFFF007F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Real-Time Audio Visualizer (Canvas API with frequencies up to 192 kHz)
            AudioVisualizerComponent(
                viewModel = viewModel,
                primaryColor = primaryColor,
                surfaceColor = surfaceColor
            )

            Spacer(modifier = Modifier.height(20.dp))

            // AI Bass Booster Component (with 192 kHz frequency engine and presets)
            AiBassBoosterComponent(
                viewModel = viewModel,
                primaryColor = primaryColor,
                surfaceColor = surfaceColor
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Link to Dedicated Equalizer Tab
            Button(
                onClick = onNavigateToEqualizer,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_pro_equalizer_tab_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Equalizer, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Studio Equalizer Tab öffnen (5 - 12 Lanes) →", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Equalizer Component embedded on Deck Screen
            VisualEqualizerComponent(
                viewModel = viewModel,
                primaryColor = primaryColor,
                accentColor = accentColor,
                surfaceColor = surfaceColor
            )

            Spacer(modifier = Modifier.height(28.dp))
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

        // Modal Track Picker Dialog for loading MP3 or MP4 onto Deck A or Deck B
        showTrackPickerForDeck?.let { targetDeck ->
            TrackPickerDialog(
                targetDeck = targetDeck,
                tracks = localTracks,
                videos = localVideos,
                onSelectTrack = { selected ->
                    if (targetDeck == "A") {
                        viewModel.playTrackA(context, selected)
                    } else {
                        viewModel.playTrackB(context, selected)
                    }
                    showTrackPickerForDeck = null
                },
                onDismiss = { showTrackPickerForDeck = null }
            )
        }
    }
}

@Composable
fun DeckChannelCard(
    deckId: String,
    channelTitle: String,
    track: LocalTrack?,
    isPlaying: Boolean,
    playbackPosition: Int,
    playbackDuration: Int,
    pitch: Float,
    bpm: Int,
    audioLevel: Float,
    beatLevel: Float,
    isLoop: Boolean,
    themeColor: Color,
    surfaceColor: Color,
    currentSkin: DjSkin,
    largeMode: Boolean = false,
    onLoadTrack: () -> Unit,
    onTogglePlay: () -> Unit,
    onCue: () -> Unit,
    onSync: () -> Unit,
    onNudgeMinus: () -> Unit,
    onNudgePlus: () -> Unit,
    onPitchChange: (Float) -> Unit,
    onSeek: (Int) -> Unit,
    onToggleLoop: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("deck_channel_card_$deckId"),
        shape = RoundedCornerShape(18.dp),
        color = surfaceColor.copy(alpha = 0.95f),
        border = BorderStroke(1.5.dp, themeColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Channel Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(if (isPlaying) Color(0xFF00E676) else Color.Gray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = channelTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }

                Button(
                    onClick = onLoadTrack,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("load_track_button_$deckId")
                ) {
                    Icon(Icons.Filled.FolderOpen, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("TRACK LADEN (MP3/MP4)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Track info strip
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF14131E)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (track?.isVideo == true) Color(0xFFE040FB) else themeColor,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = if (track?.isVideo == true) "MP4 VIDEO" else "MP3 AUDIO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Column {
                            Text(
                                text = track?.title ?: "Kein Track geladen (Tippe auf Laden)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track?.artist ?: "Deck $deckId Bereit",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // BPM Display
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF201F2C)
                    ) {
                        Text(
                            text = "${(bpm * pitch).toInt()} BPM",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Jogwheel Turntable Display
            DeckDisplay(
                currentSkin = currentSkin,
                isPlaying = isPlaying,
                playbackPosition = playbackPosition,
                audioLevel = audioLevel,
                beatLevel = beatLevel,
                ch1Level = 0.8f,
                ch2Level = 0.8f,
                crossfader = 0.5f,
                primaryColor = themeColor,
                accentColor = Color.White,
                surfaceColor = surfaceColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Seek Bar
            SeekBarAndTimer(
                playbackPosition = playbackPosition,
                playbackDuration = playbackDuration,
                onSeek = onSeek,
                primaryColor = themeColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Deck Playback Controls & Pitch Fader
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause
                Button(
                    onClick = onTogglePlay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Color(0xFFFF5252) else themeColor
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.3f).testTag("play_pause_deck_$deckId")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPlaying) "PAUSE" else "PLAY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                // CUE
                Button(
                    onClick = onCue,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2B38)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("cue_deck_$deckId")
                ) {
                    Text("CUE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // SYNC
                Button(
                    onClick = onSync,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2B38)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("sync_deck_$deckId")
                ) {
                    Text("SYNC", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColor)
                }

                // NUDGE -
                IconButton(
                    onClick = onNudgeMinus,
                    modifier = Modifier.background(Color(0xFF201F2C), CircleShape).size(36.dp)
                ) {
                    Text("◄", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // NUDGE +
                IconButton(
                    onClick = onNudgePlus,
                    modifier = Modifier.background(Color(0xFF201F2C), CircleShape).size(36.dp)
                ) {
                    Text("►", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tempo / Pitch Fader Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pitch: ${"%.1f".format((pitch - 1f) * 100f)}%",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    modifier = Modifier.width(80.dp)
                )
                Slider(
                    value = pitch,
                    onValueChange = onPitchChange,
                    valueRange = 0.85f..1.15f,
                    colors = SliderDefaults.colors(thumbColor = themeColor, activeTrackColor = themeColor),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { onPitchChange(1.0f) }) {
                    Text("0%", fontSize = 11.sp, color = themeColor)
                }
            }
        }
    }
}

@Composable
fun TrackPickerDialog(
    targetDeck: String,
    tracks: List<LocalTrack>,
    videos: List<LocalTrack>,
    onSelectTrack: (LocalTrack) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("AUDIO") } // "AUDIO", "VIDEO", "SAMPLES"

    val sampleTracks = remember {
        listOf(
            LocalTrack(
                id = -1,
                title = "SoundHelix Synth Symphony 1",
                artist = "SoundHelix Archive",
                uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                duration = 372000
            ),
            LocalTrack(
                id = -2,
                title = "SoundHelix Deep Bass Groove 2",
                artist = "SoundHelix Archive",
                uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                duration = 423000
            ),
            LocalTrack(
                id = -3,
                title = "SoundHelix Club Electro 3",
                artist = "SoundHelix Archive",
                uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                duration = 345000
            ),
            LocalTrack(
                id = -101,
                title = "Big Buck Bunny (MP4 Video)",
                artist = "Blender Foundation",
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                duration = 596000,
                isVideo = true
            ),
            LocalTrack(
                id = -102,
                title = "Elephant's Dream (MP4 Video)",
                artist = "Blender Foundation",
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                duration = 653000,
                isVideo = true
            )
        )
    }

    val currentList = when (selectedCategory) {
        "AUDIO" -> tracks.ifEmpty { sampleTracks.filter { !it.isVideo } }
        "VIDEO" -> videos.ifEmpty { sampleTracks.filter { it.isVideo } }
        else -> sampleTracks
    }

    val filteredList = remember(currentList, searchQuery) {
        if (searchQuery.isBlank()) currentList else currentList.filter {
            it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161522)),
            border = BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Track für DECK $targetDeck wählen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Schließen", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Suche nach Titel oder Artist...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("AUDIO" to "Musik (MP3)", "VIDEO" to "Videos (MP4)", "SAMPLES" to "Samples").forEach { (cat, title) ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            onClick = { selectedCategory = cat },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF222030),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else Color.LightGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Track List
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList.size) { index ->
                        val item = filteredList[index]
                        Surface(
                            onClick = { onSelectTrack(item) },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E1D2C),
                            border = BorderStroke(1.dp, Color(0xFF2E2C42)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (item.isVideo) Icons.Filled.VideoLibrary else Icons.Filled.MusicNote,
                                        contentDescription = null,
                                        tint = if (item.isVideo) Color(0xFFE040FB) else Color(0xFF00E5FF),
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF2A283C), CircleShape)
                                            .padding(8.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = item.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (targetDeck == "A") Color(0xFF00E5FF) else Color(0xFFFF007F)
                                ) {
                                    Text(
                                        text = "AUF DECK $targetDeck",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
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

