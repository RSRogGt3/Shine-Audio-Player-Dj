package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.InstrumentType
import com.example.data.SequencerProjectEntity
import com.example.ui.viewmodels.SequencerTrack
import com.example.ui.viewmodels.SequencerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SequencerScreen(
    viewModel: SequencerViewModel = viewModel()
) {
    val tracks by viewModel.tracks.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val currentBar by viewModel.currentBar.collectAsState()
    val isSynced by viewModel.isSynced.collectAsState()
    val bpm by viewModel.bpm.collectAsState()
    val masterVolume by viewModel.masterVolume.collectAsState()
    val swing by viewModel.swing.collectAsState()
    val activePresetName by viewModel.activePresetName.collectAsState()
    val savedProjects by viewModel.savedProjects.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val currentProjectName by viewModel.currentProjectName.collectAsState()

    var showAddTrackDialog by remember { mutableStateOf(false) }
    var showSaveModal by remember { mutableStateOf(false) }
    var showLoadModal by remember { mutableStateOf(false) }
    var expandedTrackId by remember { mutableStateOf<String?>(null) }

    // Clear status message after 3.5 seconds
    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            kotlinx.coroutines.delay(3500)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F0F14),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16161E))
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MULTI-TRACK SEQUENCER",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isPlaying) Color(0xFF00E676) else Color.Gray)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isPlaying) "LIVE" else "PAUSED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Project: $currentProjectName",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showAddTrackDialog = true },
                            modifier = Modifier
                                .testTag("add_track_button")
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Track",
                                tint = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action & Preset Selector Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Save Button
                    Button(
                        onClick = { showSaveModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28283C)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Speichern", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }

                    // Saved Arrangements Button
                    Button(
                        onClick = { showLoadModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28283C)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = "Projects",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Arrangements (${savedProjects.size})", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }

                    val presets = listOf("4-on-the-Floor House", "Trap Beat", "Funk Groove", "Cyberpunk Synthwave")
                    presets.forEach { preset ->
                        FilterChip(
                            selected = activePresetName == preset,
                            onClick = { viewModel.loadPreset(preset) },
                            label = { Text(preset, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF242432),
                                labelColor = Color.White
                            ),
                            border = null
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearAllSteps() },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF5252))
                    ) {
                        Text("Clear All", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF5252))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .widthIn(max = 900.dp)
        ) {
            // Toast / Status Message Banner
            AnimatedVisibility(
                visible = statusMessage != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                statusMessage?.let { msg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF28283F)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearStatusMessage() },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
            // Master Controls Card
            MasterControlPanel(
                isPlaying = isPlaying,
                bpm = bpm,
                masterVolume = masterVolume,
                swing = swing,
                currentStep = currentStep,
                onTogglePlay = { viewModel.togglePlayPause() },
                onBpmChange = { viewModel.setBpm(it) },
                onVolumeChange = { viewModel.setMasterVolume(it) },
                onSwingChange = { viewModel.setSwing(it) }
            )

            // Beat & Takt Display with Track Sync Button
            BeatAndSyncDisplay(
                isPlaying = isPlaying,
                currentStep = currentStep,
                currentBar = currentBar,
                isSynced = isSynced,
                trackCount = tracks.size,
                onSyncTracks = { viewModel.syncTracks() }
            )

            // Step Header Bar
            StepHeaderRow(currentStep = currentStep)

            // Track Layer List
            if (tracks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No instrument tracks active",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { showAddTrackDialog = true }) {
                            Text("Add Instrument Track")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tracks, key = { it.id }) { track ->
                        TrackRowItem(
                            track = track,
                            currentStep = currentStep,
                            isExpanded = expandedTrackId == track.id,
                            onToggleExpand = {
                                expandedTrackId = if (expandedTrackId == track.id) null else track.id
                            },
                            onToggleStep = { stepIdx -> viewModel.toggleStep(track.id, stepIdx) },
                            onVolumeChange = { vol -> viewModel.setTrackVolume(track.id, vol) },
                            onPanChange = { pan -> viewModel.setTrackPan(track.id, pan) },
                            onPitchChange = { pitch -> viewModel.setTrackPitch(track.id, pitch) },
                            onDistortionChange = { dist -> viewModel.setTrackDistortion(track.id, dist) },
                            onDelayChange = { delay -> viewModel.setTrackDelay(track.id, delay) },
                            onReverbChange = { rev -> viewModel.setTrackReverb(track.id, rev) },
                            onFilterChange = { cutoff -> viewModel.setTrackFilter(track.id, cutoff) },
                            onResetFx = { viewModel.resetTrackFx(track.id) },
                            onToggleMute = { viewModel.toggleTrackMute(track.id) },
                            onToggleSolo = { viewModel.toggleTrackSolo(track.id) },
                            onDeleteTrack = { viewModel.removeTrack(track.id) },
                            onApplyStylePreset = { preset -> viewModel.applyTrackStylePreset(track.id, preset) }
                        )
                    }
                }
            }
        }
    }

    if (showAddTrackDialog) {
        AddTrackModal(
            onDismiss = { showAddTrackDialog = false },
            onSelectInstrument = { instrument ->
                viewModel.addTrack(instrument)
                showAddTrackDialog = false
            }
        )
    }

    if (showSaveModal) {
        SaveProjectModal(
            initialName = currentProjectName,
            onDismiss = { showSaveModal = false },
            onSave = { name, saveToCloud ->
                viewModel.saveCurrentProject(name, saveToCloud)
                showSaveModal = false
            }
        )
    }

    if (showLoadModal) {
        LoadProjectsModal(
            projects = savedProjects,
            onDismiss = { showLoadModal = false },
            onLoadProject = { project ->
                viewModel.loadProject(project)
                showLoadModal = false
            },
            onDeleteProject = { id ->
                viewModel.deleteProject(id)
            },
            onSyncCloud = {
                viewModel.syncCloudProjects()
            }
        )
    }
}

@Composable
fun MasterControlPanel(
    isPlaying: Boolean,
    bpm: Int,
    masterVolume: Float,
    swing: Float,
    currentStep: Int,
    onTogglePlay: () -> Unit,
    onBpmChange: (Int) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSwingChange: (Float) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var bpmText by remember(bpm) { mutableStateOf(bpm.toString()) }
    var tapTimes by remember { mutableStateOf(listOf<Long>()) }

    val onTapTempo = {
        val now = System.currentTimeMillis()
        val recentTaps = tapTimes.filter { now - it < 2500 } + now
        tapTimes = recentTaps
        if (recentTaps.size >= 2) {
            val intervals = recentTaps.zipWithNext { a, b -> b - a }
            val avgIntervalMs = intervals.average()
            if (avgIntervalMs > 0) {
                val calculatedBpm = (60_000 / avgIntervalMs).toInt().coerceIn(40, 240)
                onBpmChange(calculatedBpm)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2A)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Play/Stop Button + Master Volume
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Play / Pause Button
                Button(
                    onClick = onTogglePlay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Color(0xFFFF3366) else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .height(46.dp)
                        .testTag("sequencer_play_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Stop" else "Play",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlaying) "STOP" else "PLAY",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Tap Tempo Button
                Button(
                    onClick = onTapTempo,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A3E)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.TouchApp,
                        contentDescription = "Tap Tempo",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("TAP TEMPO", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Master Volume Section
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MASTER ${(masterVolume * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Slider(
                        value = masterVolume,
                        onValueChange = onVolumeChange,
                        modifier = Modifier.width(90.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF2A2A3A), thickness = 1.dp)

            // Section 2: Global Tempo Control (Numerical BPM Input + Decrement/Increment + Tempo Slider)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161622))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GLOBAL TEMPO",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Numerical BPM Input Field with - / + Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val newBpm = (bpm - 1).coerceIn(40, 240)
                                onBpmChange(newBpm)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF26263A))
                        ) {
                            Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }

                        OutlinedTextField(
                            value = bpmText,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() }.take(3)
                                bpmText = filtered
                                val parsed = filtered.toIntOrNull()
                                if (parsed != null && parsed in 40..240) {
                                    onBpmChange(parsed)
                                }
                            },
                            modifier = Modifier
                                .width(82.dp)
                                .height(46.dp)
                                .testTag("bpm_input_field"),
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    val parsed = bpmText.toIntOrNull()
                                    if (parsed != null) {
                                        val clamped = parsed.coerceIn(40, 240)
                                        onBpmChange(clamped)
                                        bpmText = clamped.toString()
                                    } else {
                                        bpmText = bpm.toString()
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color(0xFF383850),
                                focusedContainerColor = Color(0xFF0D0D14),
                                unfocusedContainerColor = Color(0xFF0D0D14)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Text("BPM", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)

                        IconButton(
                            onClick = {
                                val newBpm = (bpm + 1).coerceIn(40, 240)
                                onBpmChange(newBpm)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF26263A))
                        ) {
                            Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Global Tempo Control Slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("40", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
                    Slider(
                        value = bpm.toFloat(),
                        onValueChange = { floatVal ->
                            onBpmChange(floatVal.toInt())
                        },
                        valueRange = 40f..240f,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bpm_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color(0xFF2E2E42)
                        )
                    )
                    Text("240", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
                }

                // Quick Tempo Presets Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tempoPresets = listOf(
                        60 to "60 Lento",
                        90 to "90 Lo-Fi",
                        120 to "120 House",
                        128 to "128 EDM",
                        140 to "140 Dubstep",
                        174 to "174 DnB"
                    )
                    tempoPresets.forEach { (presetBpm, label) ->
                        FilterChip(
                            selected = bpm == presetBpm,
                            onClick = { onBpmChange(presetBpm) },
                            label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF242436),
                                labelColor = Color.LightGray
                            ),
                            border = null
                        )
                    }
                }
            }

            // Swing Control Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Groove Swing: ${(swing * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Slider(
                    value = swing,
                    onValueChange = onSwingChange,
                    valueRange = 0f..0.5f,
                    modifier = Modifier.width(180.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Cyan,
                        activeTrackColor = Color.Cyan,
                        inactiveTrackColor = Color(0xFF2E2E42)
                    )
                )
            }
        }
    }
}

@Composable
fun StepHeaderRow(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track Controls spacer width
        Spacer(modifier = Modifier.width(130.dp))

        // 16 Step Labels
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (step in 0 until 16) {
                val isBeatHeader = step % 4 == 0
                val isActiveStep = currentStep == step

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 1.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                isActiveStep -> MaterialTheme.colorScheme.primary
                                isBeatHeader -> Color(0xFF323246)
                                else -> Color(0xFF22222E)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${step + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = if (isBeatHeader || isActiveStep) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActiveStep) Color.Black else if (isBeatHeader) Color.White else Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun TrackRowItem(
    track: SequencerTrack,
    currentStep: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleStep: (Int) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onPanChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onDistortionChange: (Float) -> Unit,
    onDelayChange: (Float) -> Unit,
    onReverbChange: (Float) -> Unit,
    onFilterChange: (Float) -> Unit,
    onResetFx: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSolo: () -> Unit,
    onDeleteTrack: () -> Unit,
    onApplyStylePreset: (String) -> Unit
) {
    var showPresetDropdown by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (track.isMuted) Color(0xFF14141C) else Color(0xFF1A1A26)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (track.isSoloed) Color.Yellow else Color(0xFF2B2B3D))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Track Info & Controls (Width 130dp)
                Column(
                    modifier = Modifier
                        .width(130.dp)
                        .padding(end = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = track.instrumentType.icon,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = track.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (track.isMuted) Color.Gray else Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = onToggleExpand,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.Tune,
                                contentDescription = "Tune Track",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // AI Style Dropdown Pill
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF26263A))
                                .clickable { showPresetDropdown = true }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "AI Style",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = track.appliedStylePreset ?: "AI Style",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = if (track.appliedStylePreset != null) Color(0xFF00E5FF) else Color.LightGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showPresetDropdown,
                            onDismissRequest = { showPresetDropdown = false },
                            modifier = Modifier.background(Color(0xFF20202E))
                        ) {
                            val stylePresets = listOf(
                                "Lo-Fi Beats" to "🎧",
                                "Cinematic Synth" to "🎹",
                                "Jazz Piano" to "🎷",
                                "808 Trap Bounce" to "🔥",
                                "Afrobeat Funk" to "🪘",
                                "Ambient Space Pad" to "✨"
                            )

                            stylePresets.forEach { (preset, emoji) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(emoji, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                preset,
                                                color = if (track.appliedStylePreset == preset) Color(0xFF00E5FF) else Color.White,
                                                fontWeight = if (track.appliedStylePreset == preset) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        onApplyStylePreset(preset)
                                        showPresetDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Compact Inline Track Waveform Preview
                    TrackWaveformPreview(
                        track = track,
                        barCount = 18,
                        currentStep = currentStep,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Mute (M) & Solo (S) & Volume Slider Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Mute button
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (track.isMuted) Color.Red else Color(0xFF2C2C3E))
                                .clickable { onToggleMute() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "M",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (track.isMuted) Color.White else Color.Gray
                            )
                        }

                        // Solo button
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (track.isSoloed) Color.Yellow else Color(0xFF2C2C3E))
                                .clickable { onToggleSolo() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "S",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (track.isSoloed) Color.Black else Color.Gray
                            )
                        }

                        // Volume mini slider
                        Slider(
                            value = track.volume,
                            onValueChange = onVolumeChange,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = track.color,
                                activeTrackColor = track.color
                            )
                        )
                    }
                }

                // Right 16-Step Grid
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (stepIndex in 0 until 16) {
                        val isStepActive = track.steps[stepIndex]
                        val isCurrentPlayhead = currentStep == stepIndex
                        val isQuarterBeat = stepIndex % 4 == 0

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .padding(horizontal = 1.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        isStepActive -> track.color.copy(alpha = if (track.isMuted) 0.3f else 1.0f)
                                        isCurrentPlayhead -> Color.White.copy(alpha = 0.3f)
                                        isQuarterBeat -> Color(0xFF2A2A3A)
                                        else -> Color(0xFF1E1E2C)
                                    }
                                )
                                .border(
                                    width = if (isCurrentPlayhead) 2.dp else 1.dp,
                                    color = when {
                                        isCurrentPlayhead -> Color.White
                                        isStepActive -> track.color
                                        else -> Color.Transparent
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onToggleStep(stepIndex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isStepActive) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }

            // Expanded Pitch / Pan / Tuning Controls
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color(0xFF14141E), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    // Full Detailed Audio Waveform Preview
                    TrackWaveformPreview(
                        track = track,
                        barCount = 48,
                        showDetails = true,
                        currentStep = currentStep,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pitch Slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tonhöhe: ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Slider(
                                value = track.pitch,
                                onValueChange = onPitchChange,
                                valueRange = 0.5f..2.0f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = track.color,
                                    activeTrackColor = track.color
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Pan Slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Pan: ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Slider(
                                value = track.pan,
                                onValueChange = onPanChange,
                                valueRange = -1.0f..1.0f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.Cyan,
                                    activeTrackColor = Color.Cyan
                                )
                            )
                        }

                        IconButton(
                            onClick = onDeleteTrack,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Track",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Quick AI Preset Selector Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Style Preset: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val stylePresets = listOf("Lo-Fi Beats", "Cinematic Synth", "Jazz Piano", "808 Trap Bounce", "Afrobeat Funk", "Ambient Space Pad")
                            stylePresets.forEach { preset ->
                                FilterChip(
                                    selected = track.appliedStylePreset == preset,
                                    onClick = { onApplyStylePreset(preset) },
                                    label = { Text(preset, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF00E5FF),
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF222234),
                                        labelColor = Color.White
                                    ),
                                    border = null
                                )
                            }
                        }
                    }

                    // Real-time Audio FX Rack Section
                    TrackFxRackSection(
                        track = track,
                        onDistortionChange = onDistortionChange,
                        onDelayChange = onDelayChange,
                        onReverbChange = onReverbChange,
                        onFilterChange = onFilterChange,
                        onResetFx = onResetFx
                    )
                }
            }
        }
    }
}

@Composable
fun TrackFxRackSection(
    track: SequencerTrack,
    onDistortionChange: (Float) -> Unit,
    onDelayChange: (Float) -> Unit,
    onReverbChange: (Float) -> Unit,
    onFilterChange: (Float) -> Unit,
    onResetFx: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10101A)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF28283E))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: FX Rack Title + Active Count Badge + Reset Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = "Audio FX",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ECHTZEIT AUDIO-FX RACK",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    val activeFxCount = (if (track.distortion > 0.01f) 1 else 0) +
                            (if (track.delayMix > 0.01f) 1 else 0) +
                            (if (track.reverbMix > 0.01f) 1 else 0) +
                            (if (track.filterCutoff < 0.98f) 1 else 0)

                    if (activeFxCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$activeFxCount FX AKTIV",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }
                }

                // Reset FX Button
                OutlinedButton(
                    onClick = onResetFx,
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF383850))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reset FX",
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset FX", fontSize = 10.sp, color = Color.LightGray)
                }
            }

            // FX Sliders Grid (Distortion, Delay, Reverb, Filter)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // 1. Distortion / Overdrive Slider Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.width(110.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥 Distortion:", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    Slider(
                        value = track.distortion,
                        onValueChange = onDistortionChange,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF5722),
                            activeTrackColor = Color(0xFFFF5722)
                        )
                    )
                    Text(
                        text = "${(track.distortion * 100).toInt()}%",
                        fontSize = 10.sp,
                        color = if (track.distortion > 0) Color(0xFFFF5722) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )
                }

                // 2. Delay Echo Mix Slider Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.width(110.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌊 Delay Echo:", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    Slider(
                        value = track.delayMix,
                        onValueChange = onDelayChange,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF)
                        )
                    )
                    Text(
                        text = "${(track.delayMix * 100).toInt()}%",
                        fontSize = 10.sp,
                        color = if (track.delayMix > 0) Color(0xFF00E5FF) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )
                }

                // 3. Reverb Space Mix Slider Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.width(110.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✨ Reverb Hall:", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    Slider(
                        value = track.reverbMix,
                        onValueChange = onReverbChange,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFE040FB),
                            activeTrackColor = Color(0xFFE040FB)
                        )
                    )
                    Text(
                        text = "${(track.reverbMix * 100).toInt()}%",
                        fontSize = 10.sp,
                        color = if (track.reverbMix > 0) Color(0xFFE040FB) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )
                }

                // 4. Low-Pass Filter Cutoff Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.width(110.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎚️ LPF Cutoff:", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    Slider(
                        value = track.filterCutoff,
                        onValueChange = onFilterChange,
                        valueRange = 0.1f..1.0f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFD700),
                            activeTrackColor = Color(0xFFFFD700)
                        )
                    )
                    val approxHz = (track.filterCutoff * track.filterCutoff * 20000).toInt().coerceAtLeast(100)
                    Text(
                        text = if (approxHz >= 1000) "${approxHz / 1000}k" else "${approxHz}Hz",
                        fontSize = 10.sp,
                        color = if (track.filterCutoff < 0.98f) Color(0xFFFFD700) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )
                }
            }

            // Quick FX Presets Macros Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("FX Macros:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

                val macroPresets = listOf(
                    "⚡ Drive Crunch" to {
                        onDistortionChange(0.45f)
                        onDelayChange(0.10f)
                        onReverbChange(0.15f)
                        onFilterChange(0.85f)
                    },
                    "🌀 Dub Delay" to {
                        onDistortionChange(0.0f)
                        onDelayChange(0.65f)
                        onReverbChange(0.35f)
                        onFilterChange(0.70f)
                    },
                    "🏛️ Cathedral" to {
                        onDistortionChange(0.0f)
                        onDelayChange(0.20f)
                        onReverbChange(0.75f)
                        onFilterChange(1.00f)
                    },
                    "📻 Lo-Fi Filter" to {
                        onDistortionChange(0.30f)
                        onDelayChange(0.25f)
                        onReverbChange(0.20f)
                        onFilterChange(0.40f)
                    }
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    macroPresets.forEach { (label, applyFx) ->
                        SuggestionChip(
                            onClick = applyFx,
                            label = { Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium) },
                            shape = RoundedCornerShape(6.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFF1C1C2C)
                            ),
                            border = BorderStroke(1.dp, Color(0xFF32324C))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddTrackModal(
    onDismiss: () -> Unit,
    onSelectInstrument: (InstrumentType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C28),
        title = {
            Text("Instrumenten-Spur hinzufügen", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InstrumentType.values().forEach { instrument ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectInstrument(instrument) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF282838)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(instrument.icon, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = instrument.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = Color.Gray)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveProjectModal(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (name: String, saveToCloud: Boolean) -> Unit
) {
    var projectName by remember { mutableStateOf(initialName) }
    var saveToCloud by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C28),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Save, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Arrangement speichern", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Projektname", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFF323246),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF252536))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CloudUpload,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Firebase Cloud Sync",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "In Firestore Datenbank sichern",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    Switch(
                        checked = saveToCloud,
                        onCheckedChange = { saveToCloud = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFF00E5FF)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(projectName, saveToCloud) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Speichern", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = Color.Gray)
            }
        }
    )
}

@Composable
fun LoadProjectsModal(
    projects: List<SequencerProjectEntity>,
    onDismiss: () -> Unit,
    onLoadProject: (SequencerProjectEntity) -> Unit,
    onDeleteProject: (String) -> Unit,
    onSyncCloud: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF181824),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gespeicherte Beats", color = Color.White, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onSyncCloud,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF28283C))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Sync,
                        contentDescription = "Sync Cloud",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            if (projects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Noch keine Arrangements gespeichert", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(projects, key = { it.id }) { project ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF222232)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = project.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (project.isCloudSynced) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("☁️ Cloud", fontSize = 9.sp, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "${project.bpm} BPM • ${dateFormat.format(Date(project.updatedAt))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = { onLoadProject(project) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Laden", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    IconButton(
                                        onClick = { onDeleteProject(project.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFFF5252),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen", color = Color.Gray)
            }
        }
    )
}

@Composable
fun TrackWaveformPreview(
    track: SequencerTrack,
    modifier: Modifier = Modifier,
    barCount: Int = 32,
    showDetails: Boolean = false,
    currentStep: Int = -1
) {
    val barAmplitudes = remember(
        track.instrumentType,
        track.pitch,
        track.volume,
        track.distortion,
        track.delayMix,
        track.reverbMix,
        track.filterCutoff,
        barCount
    ) {
        generateWaveformAmplitudes(track, barCount)
    }

    Column(modifier = modifier) {
        if (showDetails) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = "Waveform",
                        tint = track.color,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AUDIO WAVEFORM PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${(track.volume * 100).toInt()}% • Pitch x${String.format(Locale.US, "%.2f", track.pitch)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF101018))
                .border(1.dp, track.color.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (showDetails) 32.dp else 16.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerY = canvasHeight / 2f
                val spacing = 2f
                val totalSpacing = spacing * (barCount - 1)
                val barWidth = (canvasWidth - totalSpacing) / barCount

                // Zero-dB center reference line
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(0f, centerY),
                    end = Offset(canvasWidth, centerY),
                    strokeWidth = 1f
                )

                // Playhead active position marker
                if (currentStep in 0..15) {
                    val playheadX = (currentStep / 16f) * canvasWidth
                    drawLine(
                        color = Color.White.copy(alpha = 0.85f),
                        start = Offset(playheadX, 0f),
                        end = Offset(playheadX, canvasHeight),
                        strokeWidth = 2f
                    )
                }

                // Mirrored audio waveform amplitude bars
                barAmplitudes.forEachIndexed { index, amp ->
                    val x = index * (barWidth + spacing) + barWidth / 2f
                    val barHeight = (amp * (canvasHeight - 4f)).coerceAtLeast(3f)
                    val isStepPosition = currentStep in 0..15 && (index * 16 / barCount) == currentStep

                    val barColor = when {
                        isStepPosition -> Color.White
                        track.isMuted -> Color.DarkGray
                        else -> track.color.copy(alpha = (0.45f + amp * 0.55f).coerceIn(0.2f, 1.0f))
                    }

                    drawLine(
                        color = barColor,
                        start = Offset(x, centerY - barHeight / 2f),
                        end = Offset(x, centerY + barHeight / 2f),
                        strokeWidth = barWidth.coerceAtLeast(1.5f),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
    }
}

private fun generateWaveformAmplitudes(
    track: SequencerTrack,
    count: Int
): FloatArray {
    val result = FloatArray(count)
    val freqFactor = track.pitch.coerceIn(0.5f, 2.0f)
    val type = track.instrumentType
    val volume = track.volume

    for (i in 0 until count) {
        val t = i.toFloat() / (count - 1)
        var amp = when (type) {
            InstrumentType.KICK -> {
                kotlin.math.exp(-t * 4.5f) * kotlin.math.abs(kotlin.math.sin(t * 12.0 * freqFactor)).toFloat()
            }
            InstrumentType.SNARE -> {
                val snap = kotlin.math.exp(-t * 8.0f)
                val noise = kotlin.math.exp(-t * 2.5f) * ((i * 17) % 10 / 10f)
                (snap * 0.7f + noise * 0.5f).toFloat()
            }
            InstrumentType.HIHAT -> {
                val env = kotlin.math.exp(-t * 9.0f)
                (env * ((i * 31) % 10 / 10f)).toFloat()
            }
            InstrumentType.CLAP -> {
                val burst = kotlin.math.sin(t * 25.0 * freqFactor).toFloat()
                val env = kotlin.math.exp(-t * 3.5f)
                kotlin.math.abs(burst) * env
            }
            InstrumentType.BASS -> {
                val wave = kotlin.math.sin(t * 10.0 * freqFactor) + 0.3 * kotlin.math.sin(t * 20.0 * freqFactor)
                (kotlin.math.abs(wave) * kotlin.math.exp(-t * 1.2f)).toFloat()
            }
            InstrumentType.SYNTH -> {
                val wave = kotlin.math.sin(t * 18.0 * freqFactor) * 0.7 + kotlin.math.cos(t * 36.0 * freqFactor) * 0.3
                (kotlin.math.abs(wave) * kotlin.math.exp(-t * 1.8f)).toFloat()
            }
            InstrumentType.PERC -> {
                val wave = kotlin.math.sin(t * 15.0 * freqFactor)
                (kotlin.math.abs(wave) * kotlin.math.exp(-t * 5.0f)).toFloat()
            }
            InstrumentType.FX -> {
                val wave = kotlin.math.sin(t * t * 30.0 * freqFactor)
                (kotlin.math.abs(wave) * (0.2f + t * 0.8f)).toFloat()
            }
        }

        // Apply Distortion boost & saturation
        if (track.distortion > 0.01f) {
            amp = (amp * (1.0f + track.distortion * 1.2f)).coerceIn(0f, 1f)
        }

        // Apply LPF Cutoff dampening
        if (track.filterCutoff < 0.98f) {
            amp *= (0.3f + 0.7f * track.filterCutoff)
        }

        result[i] = (amp * volume).coerceIn(0.05f, 1.0f)
    }

    // Apply Delay Echo repetitions visual ghost peaks
    if (track.delayMix > 0.01f) {
        val delayShift = (count * 0.28f).toInt()
        val echoGain = track.delayMix * 0.55f
        for (i in delayShift until count) {
            result[i] = (result[i] + result[i - delayShift] * echoGain).coerceIn(0.05f, 1.0f)
        }
    }

    // Apply Reverb space tail visual decay spread
    if (track.reverbMix > 0.01f) {
        val revSpread = track.reverbMix * 0.35f
        for (i in 1 until count) {
            result[i] = (result[i] + result[i - 1] * revSpread).coerceIn(0.05f, 1.0f)
        }
    }

    return result
}

@Composable
fun BeatAndSyncDisplay(
    isPlaying: Boolean,
    currentStep: Int,
    currentBar: Int,
    isSynced: Boolean,
    trackCount: Int,
    onSyncTracks: () -> Unit
) {
    val activeBeat = if (currentStep >= 0) (currentStep / 4) + 1 else 0
    val activeSubStep = if (currentStep >= 0) (currentStep % 4) + 1 else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181826)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF2A2A3E))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Title + Sync Status + 1-Click Sync Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = "Beat Clock",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BEAT & TAKT DISPLAY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Sync Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSynced) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFFFF9800).copy(alpha = 0.2f))
                            .border(0.5.dp, if (isSynced) Color(0xFF00E5FF) else Color(0xFFFF9800), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isSynced) Color(0xFF00E5FF) else Color(0xFFFF9800))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSynced) "IN SYNC • 16th LOCK" else "SYNC READY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSynced) Color(0xFF00E5FF) else Color(0xFFFF9800)
                            )
                        }
                    }
                }

                // 1-Click Track Sync Button
                Button(
                    onClick = onSyncTracks,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("sync_tracks_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Sync,
                        contentDescription = "Sync Tracks",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TRACKS SYNCHRONISIEREN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            // Takt & Beat Meter Display Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F0F18))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Takt / Bar Display Box
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1E2E))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "TAKT",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.US, "%02d", if (isPlaying) currentBar else 1),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Beats 1 - 2 - 3 - 4 Cards Row
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (beatNum in 1..4) {
                        val isBeatActive = activeBeat == beatNum && isPlaying
                        val beatColor = if (beatNum == 1) Color(0xFF00E5FF) else MaterialTheme.colorScheme.primary

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isBeatActive) beatColor
                                    else Color(0xFF181824)
                                )
                                .border(
                                    1.dp,
                                    if (isBeatActive) Color.White else Color(0xFF28283C),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "BEAT $beatNum",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBeatActive) Color.Black else Color.Gray
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                // Sub-step mini dots (4 dots per beat)
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    for (sub in 1..4) {
                                        val isSubActive = isBeatActive && activeSubStep == sub
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isSubActive -> Color.Black
                                                        isBeatActive -> Color.Black.copy(alpha = 0.3f)
                                                        else -> Color(0xFF383850)
                                                    }
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sync Status Details Note
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Phase Grid: 16 Steps @ 4/4 Takt • $trackCount Tracks gekoppelt",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                Text(
                    text = if (isPlaying) "Step ${(currentStep + 1).coerceAtLeast(1)} / 16" else "Bereit",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

