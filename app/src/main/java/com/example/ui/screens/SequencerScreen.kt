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
import androidx.compose.ui.draw.scale
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
import com.example.ui.viewmodels.CloudSyncState
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

    val isMasterBassKilled by viewModel.isMasterBassKilled.collectAsState()
    val isMasterMidKilled by viewModel.isMasterMidKilled.collectAsState()
    val isMasterHighKilled by viewModel.isMasterHighKilled.collectAsState()
    val isGeneratingAiBeat by viewModel.isGeneratingAiBeat.collectAsState()

    val cloudSyncState by viewModel.cloudSyncState.collectAsState()
    val lastCloudSyncTime by viewModel.lastCloudSyncTime.collectAsState()
    val isDriveBackupEnabled by viewModel.isDriveBackupEnabled.collectAsState()
    val lastDriveBackupTime by viewModel.lastDriveBackupTime.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var showAddTrackDialog by remember { mutableStateOf(false) }
    var showSaveModal by remember { mutableStateOf(false) }
    var showLoadModal by remember { mutableStateOf(false) }
    var showAiBeatModal by remember { mutableStateOf(false) }
    var showCloudSyncModal by remember { mutableStateOf(false) }
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "Project: $currentProjectName",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Medium
                            )

                            Surface(
                                onClick = { showCloudSyncModal = true },
                                shape = RoundedCornerShape(12.dp),
                                color = when (cloudSyncState) {
                                    CloudSyncState.SAVED -> Color(0xFF132B20)
                                    CloudSyncState.SAVING -> Color(0xFF142B38)
                                    CloudSyncState.NEEDS_ATTENTION -> Color(0xFF332010)
                                },
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = when (cloudSyncState) {
                                        CloudSyncState.SAVED -> Color(0xFF00E676)
                                        CloudSyncState.SAVING -> Color(0xFF00E5FF)
                                        CloudSyncState.NEEDS_ATTENTION -> Color(0xFFFF9100)
                                    }
                                ),
                                modifier = Modifier.testTag("firestore_sync_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    val (icon, text, badgeColor) = when (cloudSyncState) {
                                        CloudSyncState.SAVED -> Triple(Icons.Filled.CloudDone, "Firestore", Color(0xFF00E676))
                                        CloudSyncState.SAVING -> Triple(Icons.Filled.Sync, "Speichert...", Color(0xFF00E5FF))
                                        CloudSyncState.NEEDS_ATTENTION -> Triple(Icons.Filled.CloudQueue, "Offen", Color(0xFFFF9100))
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = "Cloud Sync Status",
                                        tint = badgeColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                    if (isDriveBackupEnabled) {
                                        Text("| 📁 Drive", style = MaterialTheme.typography.labelSmall, color = Color(0xFF81D4FA), fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFE040FB))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = "Export Mix",
                                tint = Color.Black
                            )
                        }
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
                    // ✨ KI BEAT GENERATOR BUTTON
                    Button(
                        onClick = { showAiBeatModal = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("ai_beat_gen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "AI Beat Generator",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isGeneratingAiBeat) "KI erstellt..." else "✨ KI Beat Generator",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

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
            // Sequencer Master Kill-Knöpfe Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B26)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF2E2E42))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Equalizer,
                            contentDescription = "Master Kill",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MASTER KILL-KNÖPFE:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Bass Kill
                        FilterChip(
                            selected = isMasterBassKilled,
                            onClick = { viewModel.toggleMasterBassKill() },
                            label = { Text("BASS KILL", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF1744),
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF282838),
                                labelColor = Color.White
                            ),
                            border = null
                        )
                        // Mid Kill
                        FilterChip(
                            selected = isMasterMidKilled,
                            onClick = { viewModel.toggleMasterMidKill() },
                            label = { Text("MID KILL", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFC400),
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF282838),
                                labelColor = Color.White
                            ),
                            border = null
                        )
                        // High Kill
                        FilterChip(
                            selected = isMasterHighKilled,
                            onClick = { viewModel.toggleMasterHighKill() },
                            label = { Text("HI KILL", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00E5FF),
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF282838),
                                labelColor = Color.White
                            ),
                            border = null
                        )
                        if (isMasterBassKilled || isMasterMidKilled || isMasterHighKilled) {
                            IconButton(
                                onClick = { viewModel.resetMasterKills() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Reset Kills", tint = Color.Red, modifier = Modifier.size(14.dp))
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
                            onToggleBassKill = { viewModel.toggleTrackBassKill(track.id) },
                            onToggleMidKill = { viewModel.toggleTrackMidKill(track.id) },
                            onToggleHighKill = { viewModel.toggleTrackHighKill(track.id) },
                            onResetFx = { viewModel.resetTrackFx(track.id) },
                            onToggleMute = { viewModel.toggleTrackMute(track.id) },
                            onToggleSolo = { viewModel.toggleTrackSolo(track.id) },
                            onDeleteTrack = { viewModel.removeTrack(track.id) },
                            onApplyStylePreset = { preset -> viewModel.applyTrackStylePreset(track.id, preset) },
                            onSampleRecorded = { path -> viewModel.setTrackSamplePath(track.id, path) },
                            masterBpm = bpm,
                            onSampleBpmChange = { sampleBpm -> viewModel.setTrackSampleBpm(track.id, sampleBpm) },
                            onToggleTempoSync = { viewModel.toggleTrackTempoSync(track.id) }
                        )
                    }
                }
            }
        }
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

    if (showAiBeatModal) {
        AiBeatGeneratorModal(
            isGenerating = isGeneratingAiBeat,
            onDismiss = { showAiBeatModal = false },
            onGenerate = { prompt: String ->
                viewModel.generateAiBeat(prompt)
                showAiBeatModal = false
            }
        )
    }

    if (showCloudSyncModal) {
        CloudSyncCenterModal(
            cloudSyncState = cloudSyncState,
            lastCloudSyncTime = lastCloudSyncTime,
            isDriveBackupEnabled = isDriveBackupEnabled,
            lastDriveBackupTime = lastDriveBackupTime,
            onDismiss = { showCloudSyncModal = false },
            onSyncFirestore = { viewModel.syncCloudProjects() },
            onSaveProject = {
                showCloudSyncModal = false
                showSaveModal = true
            },
            onToggleDriveBackup = { viewModel.toggleDriveBackup(it) },
            onBackupDrive = { viewModel.backupToGoogleDrive() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncCenterModal(
    cloudSyncState: CloudSyncState,
    lastCloudSyncTime: Long?,
    isDriveBackupEnabled: Boolean,
    lastDriveBackupTime: Long?,
    onDismiss: () -> Unit,
    onSyncFirestore: () -> Unit,
    onSaveProject: () -> Unit,
    onToggleDriveBackup: (Boolean) -> Unit,
    onBackupDrive: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2C),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CloudDone,
                        contentDescription = "Cloud Center",
                        tint = Color(0xFF00E5FF)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cloud & Google Drive Center", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Section 1: Firestore Sync Status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141422)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF2E2E42))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔥 FIRESTORE CLOUD-SYNC", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            
                            val (badgeBg, badgeText, badgeColor) = when (cloudSyncState) {
                                CloudSyncState.SAVED -> Triple(Color(0xFF1B382B), "GESICHERT", Color(0xFF00E676))
                                CloudSyncState.SAVING -> Triple(Color(0xFF1E3A4A), "SYNCHRONISIERT...", Color(0xFF00E5FF))
                                CloudSyncState.NEEDS_ATTENTION -> Triple(Color(0xFF3E2818), "ÄNDERUNGEN OFFEN", Color(0xFFFF9100))
                            }
                            Surface(
                                color = badgeBg,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (lastCloudSyncTime != null) "Letzter Sync: ${dateFormat.format(Date(lastCloudSyncTime))}" else "Noch nicht synchronisiert",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onSyncFirestore,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28283E)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Sync, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync Now", fontSize = 11.sp, color = Color.White)
                            }
                            Button(
                                onClick = onSaveProject,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Speichern", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Section 2: Google Drive Backup
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141422)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF2E2E42))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📁", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("GOOGLE DRIVE BACKUP", color = Color(0xFF81D4FA), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Switch(
                                checked = isDriveBackupEnabled,
                                onCheckedChange = onToggleDriveBackup,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = Color(0xFF81D4FA)
                                ),
                                modifier = Modifier.scale(0.8f).testTag("drive_backup_switch")
                            )
                        }

                        Text(
                            text = if (lastDriveBackupTime != null) "Letztes Drive Backup: ${dateFormat.format(Date(lastDriveBackupTime))}" else "Noch kein Drive Backup erstellt",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Konto: hellrider66683@gmail.com (Aktiv)",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )

                        Button(
                            onClick = onBackupDrive,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.FolderZip, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("In Google Drive sichern", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen", color = Color.Gray)
            }
        }
    )
}

@Composable
fun AiBeatGeneratorModal(
    isGenerating: Boolean,
    onDismiss: () -> Unit,
    onGenerate: (String) -> Unit
) {
    var promptInput by remember { mutableStateOf("Dark Berlin Techno 132 BPM with heavy Kick and syncopated HiHats") }
    val quickPrompts = listOf(
        "Dark Techno 132 BPM",
        "80s Synthwave Drive 118",
        "Afrobeats Dance Groove 105",
        "Lofi Chillhop Vinyl 85",
        "Hard Trap 808 Bounce 140",
        "Melodic House 124"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2C),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color(0xFF00E5FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text("✨ KI Beat Generator", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Gib der On-Device KI deine Beat-Idee oder wähle einen Schnell-Style:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )

                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    label = { Text("Beat Beschreibung") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF00E5FF),
                        cursorColor = Color(0xFF00E5FF)
                    )
                )

                Text("Schnell-Auswahl:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickPrompts.forEach { qp ->
                        FilterChip(
                            selected = promptInput == qp,
                            onClick = { promptInput = qp },
                            label = { Text(qp, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00E5FF),
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF28283E),
                                labelColor = Color.White
                            ),
                            border = null
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGenerate(promptInput) },
                enabled = !isGenerating && promptInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                modifier = Modifier.testTag("submit_ai_beat_gen")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                } else {
                    Text("✨ Beat Generieren", fontWeight = FontWeight.Bold)
                }
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
fun BeatAndSyncDisplay(
    isPlaying: Boolean,
    currentStep: Int,
    currentBar: Int,
    isSynced: Boolean,
    trackCount: Int,
    onSyncTracks: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Takt $currentBar | Beat ${(currentStep / 4) + 1}.${(currentStep % 4) + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPlaying) Color(0xFF00E5FF) else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$trackCount Spuren",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
            Button(
                onClick = onSyncTracks,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSynced) Color(0xFF00E5FF) else Color(0xFF282838),
                    contentColor = if (isSynced) Color.Black else Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("PHASEN SYNC", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
    onToggleBassKill: () -> Unit,
    onToggleMidKill: () -> Unit,
    onToggleHighKill: () -> Unit,
    onResetFx: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSolo: () -> Unit,
    onDeleteTrack: () -> Unit,
    onApplyStylePreset: (String) -> Unit,
    onSampleRecorded: (String) -> Unit = {},
    masterBpm: Int = 120,
    onSampleBpmChange: (Int) -> Unit = {},
    onToggleTempoSync: () -> Unit = {}
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
                        onToggleBassKill = onToggleBassKill,
                        onToggleMidKill = onToggleMidKill,
                        onToggleHighKill = onToggleHighKill,
                        onResetFx = onResetFx
                    )

                    if (track.instrumentType == InstrumentType.SAMPLE) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF141422)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE040FB).copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🎤", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "SAMPLE TEMPO SYNCHRONISATION",
                                            color = Color(0xFFE040FB),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (track.isTempoSynced) "SYNC AN" else "OFF",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (track.isTempoSynced) Color(0xFF00E5FF) else Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Switch(
                                            checked = track.isTempoSynced,
                                            onCheckedChange = { onToggleTempoSync() },
                                            modifier = Modifier
                                                .scale(0.8f)
                                                .testTag("tempo_sync_switch_${track.id}"),
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.Black,
                                                checkedTrackColor = Color(0xFF00E5FF)
                                            )
                                        )
                                    }
                                }

                                if (track.isTempoSynced) {
                                    val ratio = masterBpm.toFloat() / track.sampleOriginalBpm.coerceAtLeast(40).toFloat()
                                    val ratioPercentage = (ratio * 100).toInt()
                                    Surface(
                                        color = Color(0xFF1E1E30),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Project: $masterBpm BPM | Sample: ${track.sampleOriginalBpm} BPM",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.LightGray
                                            )
                                            Text(
                                                text = String.format("Speed: %.2fx (%d%%)", ratio, ratioPercentage),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF00E5FF),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Original Sample BPM:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IconButton(
                                                onClick = { onSampleBpmChange((track.sampleOriginalBpm - 1).coerceIn(40, 240)) },
                                                modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF26263A))
                                            ) {
                                                Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                            Text(
                                                text = "${track.sampleOriginalBpm} BPM",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            IconButton(
                                                onClick = { onSampleBpmChange((track.sampleOriginalBpm + 1).coerceIn(40, 240)) },
                                                modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF26263A))
                                            ) {
                                                Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                        }
                                    }

                                    Slider(
                                        value = track.sampleOriginalBpm.toFloat(),
                                        onValueChange = { onSampleBpmChange(it.toInt()) },
                                        valueRange = 40f..240f,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("sample_bpm_slider_${track.id}"),
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFFE040FB),
                                            activeTrackColor = Color(0xFFE040FB),
                                            inactiveTrackColor = Color(0xFF2E2E42)
                                        )
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val sampleBpmPresets = listOf(80, 90, 100, 110, 120, 128, 140, 160)
                                        sampleBpmPresets.forEach { presetBpm ->
                                            FilterChip(
                                                selected = track.sampleOriginalBpm == presetBpm,
                                                onClick = { onSampleBpmChange(presetBpm) },
                                                label = { Text("$presetBpm", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFFE040FB),
                                                    selectedLabelColor = Color.Black,
                                                    containerColor = Color(0xFF222234),
                                                    labelColor = Color.LightGray
                                                ),
                                                border = null,
                                                modifier = Modifier.height(26.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Custom Sample Aufnahme / Import", color = Color(0xFFE040FB), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                AudioRecorderComponent(onRecordingFinished = { file -> onSampleRecorded(file.absolutePath) })
                            }
                        }
                    }
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
    onToggleBassKill: () -> Unit,
    onToggleMidKill: () -> Unit,
    onToggleHighKill: () -> Unit,
    onResetFx: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10101A)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ECHTZEIT AUDIO-FX RACK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Distortion", color = Color.Gray, fontSize = 10.sp)
                    Slider(value = track.distortion, onValueChange = onDistortionChange, valueRange = 0f..1f)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Delay", color = Color.Gray, fontSize = 10.sp)
                    Slider(value = track.delayMix, onValueChange = onDelayChange, valueRange = 0f..1f)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reverb", color = Color.Gray, fontSize = 10.sp)
                    Slider(value = track.reverbMix, onValueChange = onReverbChange, valueRange = 0f..1f)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Filter Cutoff", color = Color.Gray, fontSize = 10.sp)
                    Slider(value = track.filterCutoff, onValueChange = onFilterChange, valueRange = 0f..1f)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(selected = track.isBassKilled, onClick = onToggleBassKill, label = { Text("BASS KILL") })
                FilterChip(selected = track.isMidKilled, onClick = onToggleMidKill, label = { Text("MID KILL") })
                FilterChip(selected = track.isHighKilled, onClick = onToggleHighKill, label = { Text("HI KILL") })
                Button(onClick = onResetFx, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("RESET") }
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
        containerColor = Color(0xFF1E1E2C),
        title = {
            Text("Instrument Hinzufügen", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InstrumentType.values().forEach { inst ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF28283E))
                            .clickable {
                                onSelectInstrument(inst)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(inst.icon, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(inst.displayName, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
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
    onSave: (String, Boolean) -> Unit
) {
    var nameInput by remember { mutableStateOf(initialName) }
    var saveToCloud by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2C),
        title = {
            Text("Arrangement Speichern", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Projekt Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = saveToCloud,
                        onCheckedChange = { saveToCloud = it }
                    )
                    Text("In Firebase Cloud-Sync sichern", color = Color.White, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nameInput, saveToCloud) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black)
            ) {
                Text("Speichern", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = Color.Gray)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadProjectsModal(
    projects: List<SequencerProjectEntity>,
    onDismiss: () -> Unit,
    onLoadProject: (SequencerProjectEntity) -> Unit,
    onDeleteProject: (String) -> Unit,
    onSyncCloud: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, LOCAL, CLOUD

    val filteredProjects = remember(projects, searchQuery, selectedFilter) {
        projects.filter { proj ->
            val matchesQuery = proj.name.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "CLOUD" -> proj.isCloudSynced
                "LOCAL" -> !proj.isCloudSynced
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2C),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Storage,
                        contentDescription = "Room Database",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lokale Room-Datenbank", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                IconButton(onClick = onSyncCloud) {
                    Icon(Icons.Filled.Sync, contentDescription = "Cloud Sync", tint = Color(0xFF00E5FF))
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Room Stats Summary Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF12121D)),
                    border = BorderStroke(1.dp, Color(0xFF2E2E42)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("📁 Local Room Storage", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("${projects.size} Arrangements gespeichert", color = Color.LightGray, fontSize = 10.sp)
                        }
                        Surface(
                            color = Color(0xFF1A3326),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "SQLite / Room OK",
                                color = Color(0xFF00E676),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Arrangement suchen...", color = Color.Gray, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF141420),
                        unfocusedContainerColor = Color(0xFF141420),
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF2E2E42),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("room_project_search_input")
                )

                // Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL" to "Alle (${projects.size})", "LOCAL" to "Lokal", "CLOUD" to "Cloud Synced").forEach { (filterKey, filterLabel) ->
                        FilterChip(
                            selected = selectedFilter == filterKey,
                            onClick = { selectedFilter = filterKey },
                            label = { Text(filterLabel, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00E5FF),
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF28283E),
                                labelColor = Color.LightGray
                            ),
                            border = null,
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                // Projects List
                if (filteredProjects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (projects.isEmpty()) "Keine gespeicherten Projekte in Room vorhanden." else "Kein Projekt passend zur Suche.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredProjects, key = { it.id }) { proj ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onLoadProject(proj) },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF25253A)),
                                border = BorderStroke(1.dp, Color(0xFF33334D))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(proj.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = Color(0xFF1E1E2E),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    "${proj.bpm} BPM",
                                                    color = Color(0xFF00E5FF),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(proj.updatedAt)),
                                                color = Color.Gray,
                                                fontSize = 10.sp
                                            )
                                            if (proj.isCloudSynced) {
                                                Text("☁️ Firestore OK", color = Color(0xFF00E676), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            } else {
                                                Text("💾 Nur Lokal", color = Color(0xFFFFB74D), fontSize = 9.sp)
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Button(
                                            onClick = { onLoadProject(proj) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("Laden", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { onDeleteProject(proj.id) },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
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
    Box(
        modifier = modifier
            .height(if (showDetails) 40.dp else 20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF12121A))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val barWidth = width / barCount
            
            for (i in 0 until barCount) {
                val stepIdx = (i * 16) / barCount
                val isActive = track.steps.getOrElse(stepIdx) { false }
                val isPlayhead = currentStep == stepIdx
                val barHeight = if (isActive) height * 0.8f else height * 0.2f
                
                val color = when {
                    isPlayhead -> Color.White
                    isActive -> track.color
                    else -> Color(0xFF282838)
                }
                
                drawRect(
                    color = color,
                    topLeft = Offset(i * barWidth + 1f, (height - barHeight) / 2f),
                    size = Size(barWidth - 2f, barHeight)
                )
            }
        }
    }
}
