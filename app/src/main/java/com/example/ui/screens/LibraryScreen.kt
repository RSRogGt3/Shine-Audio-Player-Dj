package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodels.LocalMusicViewModel
import com.example.ui.viewmodels.LocalTrack
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LibraryScreen(viewModel: LocalMusicViewModel = viewModel()) {
    val context = LocalContext.current
    
    val mediaPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    val permissions = listOf(
        mediaPermission,
        Manifest.permission.RECORD_AUDIO
    )
    
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)
    
    val localTracks by viewModel.localTracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var showRecorder by remember { mutableStateOf(false) }
    val filteredTracks = remember(localTracks, searchQuery) {
        if (searchQuery.isBlank()) {
            localTracks
        } else {
            localTracks.filter { 
                it.title.contains(searchQuery, ignoreCase = true) || 
                it.artist.contains(searchQuery, ignoreCase = true) 
            }
        }
    }
    
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackPosition by viewModel.playbackPosition.collectAsState()
    val playbackDuration by viewModel.playbackDuration.collectAsState()
    val isSampleLoaded by viewModel.isSampleLibraryLoaded.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val beatLevel by viewModel.beatLevel.collectAsState()
    val visualizerBands by viewModel.visualizerBands.collectAsState()

    // Query local audio tracks whenever permission is granted
    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            viewModel.fetchLocalMusic(context)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .displayCutoutPadding()
                .navigationBarsPadding()
                .padding(bottom = if (currentTrack != null) 140.dp else 16.dp)
                .widthIn(max = 800.dp)
                .align(Alignment.TopCenter)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Medienbibliothek",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isSampleLoaded) "Beispiel-Bibliothek aktiv" else "Lokale Musikdateien auf deinem Handy",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Icon(
                    imageVector = Icons.Filled.LibraryMusic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text("Tracks suchen...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Suchen", tint = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFF2E2E2E),
                    focusedContainerColor = Color(0xFF1A1A1A),
                    unfocusedContainerColor = Color(0xFF1A1A1A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Real-time Audio Visualizer Card when track active, otherwise Hero Image
            if (currentTrack != null) {
                RealtimeAudioVisualizerCard(
                    currentTrack = currentTrack!!,
                    isPlaying = isPlaying,
                    audioLevel = audioLevel,
                    beatLevel = beatLevel,
                    visualizerBands = visualizerBands,
                    primaryColor = MaterialTheme.colorScheme.primary
                )
            } else {
                coil.compose.AsyncImage(
                    model = com.example.R.drawable.img_library_hero,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permissions Check / Main Library View
            if (!permissionState.allPermissionsGranted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val isPermanentlyDenied = permissionState.permissions.any { it.status is PermissionStatus.Denied && !it.status.shouldShowRationale }
                            val shouldShowRationale = permissionState.permissions.any { it.status.shouldShowRationale }

                            Icon(
                                imageVector = if (isPermanentlyDenied) Icons.Filled.Lock else Icons.Filled.Folder,
                                contentDescription = null,
                                tint = if (isPermanentlyDenied) Color.Gray else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = if (isPermanentlyDenied) "Berechtigung dauerhaft verweigert" else "Berechtigung erforderlich",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (shouldShowRationale) {
                                    "Um Musik von deinem Handy abzuspielen, benötigt der Player Zugriff auf deine Audiodateien."
                                } else if (isPermanentlyDenied) {
                                    "Der Zugriff wurde dauerhaft verweigert. Bitte aktiviere ihn in den Einstellungen deines Handys."
                                } else {
                                    "Um Musik von deinem Handy abzuspielen, benötigt der Player Zugriff auf deine Audiodateien."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            
                            if (isPermanentlyDenied) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text("Einstellungen öffnen", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { permissionState.launchMultiplePermissionRequest() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("grant_permission_button")
                                ) {
                                    Text("Zugriff erlauben", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }

                            TextButton(
                                onClick = { viewModel.loadSampleLibrary() }
                            ) {
                                Text("Demo-Bibliothek laden (Internet-Stream)", color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            } else {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (localTracks.isEmpty()) {
                    // Empty library screen with Option to load Sample Library
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MusicNote,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = "Keine Musik gefunden",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Wir konnten keine Audiodateien auf deinem Handy finden. Lade die Beispiel-Bibliothek, um den Player mit Live-Streams zu testen!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                
                                Button(
                                    onClick = { viewModel.loadSampleLibrary() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("load_sample_button")
                                ) {
                                    Text("Beispiel-Bibliothek laden", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.fetchLocalMusic(context) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Aktualisieren", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aktualisieren", style = MaterialTheme.typography.labelMedium)
                        }

                        TextButton(
                            onClick = { showRecorder = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Filled.Mic, contentDescription = "Aufnehmen", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Neue Aufnahme", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredTracks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Keine Tracks gefunden",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    } else {
                        // Track list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                                .testTag("library_list"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredTracks) { track ->
                                val isSelected = currentTrack?.id == track.id
                                TrackListItem(
                                    track = track,
                                    isSelected = isSelected,
                                    isPlaying = isSelected && isPlaying,
                                    audioLevel = if (isSelected) audioLevel else 0f,
                                    onClick = { viewModel.playTrack(context, track) },
                                    onLoadDeckA = { viewModel.playTrackA(context, track) },
                                    onLoadDeckB = { viewModel.playTrackB(context, track) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Persistent Docked Playback Controls
        AnimatedVisibility(
            visible = currentTrack != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            currentTrack?.let { track ->
                PlaybackControlPanel(
                    track = track,
                    isPlaying = isPlaying,
                    position = playbackPosition,
                    duration = playbackDuration,
                    audioLevel = audioLevel,
                    onPlayPauseToggle = { viewModel.togglePlayPause() },
                    onSkipNext = { viewModel.playNext(context) },
                    onSkipPrev = { viewModel.playPrevious(context) },
                    onSeek = { viewModel.seekTo(it) }
                )
            }
        }

        if (showRecorder) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showRecorder = false }) {
                AudioRecorderComponent(
                    onRecordingFinished = {
                        // After recording, we can refresh the list
                        viewModel.fetchLocalMusic(context)
                        showRecorder = false
                    }
                )
            }
        }
    }
}

@Composable
fun TrackListItem(
    track: LocalTrack,
    isSelected: Boolean,
    isPlaying: Boolean,
    audioLevel: Float = 0f,
    onClick: () -> Unit,
    onLoadDeckA: () -> Unit = {},
    onLoadDeckB: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art Placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) {
                            Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2A2A2A), Color(0xFF1F1F1F))
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.PlayArrow else Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = if (isSelected) Color.Black else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quick Load Buttons: DECK A & DECK B
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    onClick = onLoadDeckA,
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF))
                ) {
                    Text(
                        text = "DECK A",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    onClick = onLoadDeckB,
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFF007F).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFFFF007F))
                ) {
                    Text(
                        text = "DECK B",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF007F),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PlaybackControlPanel(
    track: LocalTrack,
    isPlaying: Boolean,
    position: Int,
    duration: Int,
    audioLevel: Float = 0f,
    onPlayPauseToggle: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrev: () -> Unit,
    onSeek: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(134.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Live top visualizer line across mini player card
            if (isPlaying) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                ) {
                    val count = 32
                    val step = size.width / count
                    for (i in 0 until count) {
                        val h = (audioLevel * (0.3f + 0.7f * kotlin.math.sin(i * 0.8 + (position / 80.0)).toFloat())).coerceIn(0.1f, 1f)
                        drawRect(
                            color = Color.Cyan.copy(alpha = h),
                            topLeft = Offset(i * step, 0f),
                            size = Size(step * 0.7f, size.height)
                        )
                    }
                }
            }
            // Track details and playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info block
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF2A2A2A), CircleShape)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Control deck
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onSkipPrev,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Vorheriger Titel",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = onPlayPauseToggle,
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Abspielen/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Nächster Titel",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Seekbar progress controls
            Column {
                Slider(
                    value = position.toFloat(),
                    onValueChange = { onSeek(it.toInt()) },
                    valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color(0xFF2E2E2E)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(position),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = formatTime(duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
fun RealtimeAudioVisualizerCard(
    currentTrack: LocalTrack,
    isPlaying: Boolean,
    audioLevel: Float,
    beatLevel: Float,
    visualizerBands: FloatArray,
    primaryColor: Color
) {
    var selectedMode by remember { mutableStateOf(0) } // 0 = Spektrum, 1 = Welle, 2 = Pulse

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141418)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.5f), Color(0xFF8A2BE2).copy(alpha = 0.3f))))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Header: Title & Live status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) Color.Green else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPlaying) "ECHTZEIT VISUALIZER" else "PAUSIERT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) primaryColor else Color.Gray
                    )
                }

                // Mode Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val modes = listOf("Spektrum", "Welle", "Pulse")
                    modes.forEachIndexed { index, name ->
                        FilterChip(
                            selected = selectedMode == index,
                            onClick = { selectedMode = index },
                            label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor.copy(alpha = 0.25f),
                                selectedLabelColor = primaryColor,
                                containerColor = Color(0xFF22222A),
                                labelColor = Color.Gray
                            ),
                            border = null,
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas for real-time visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B0B0E)),
                contentAlignment = Alignment.Center
            ) {
                when (selectedMode) {
                    0 -> SpectrumVisualizerCanvas(visualizerBands, audioLevel, isPlaying, primaryColor)
                    1 -> WaveformVisualizerCanvas(audioLevel, beatLevel, isPlaying, primaryColor)
                    2 -> BeatPulseVisualizerCanvas(audioLevel, beatLevel, isPlaying, primaryColor)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer info: VU Meter level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentTrack.title} • ${currentTrack.artist}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Pegel: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    LinearProgressIndicator(
                        progress = { if (isPlaying) audioLevel else 0f },
                        modifier = Modifier
                            .width(60.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = primaryColor,
                        trackColor = Color(0xFF2B2B36)
                    )
                }
            }
        }
    }
}

@Composable
fun SpectrumVisualizerCanvas(
    bands: FloatArray,
    audioLevel: Float,
    isPlaying: Boolean,
    primaryColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        val count = if (bands.isNotEmpty()) bands.size else 16
        val barWidth = (size.width - (count - 1) * 6f) / count
        val maxHeight = size.height

        for (i in 0 until count) {
            val level = if (isPlaying && i < bands.size) bands[i].coerceIn(0.08f, 1f) else 0.05f
            val barHeight = maxHeight * level
            val x = i * (barWidth + 6f)
            val y = maxHeight - barHeight

            // Draw Bar Gradient
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF007F), // Neon Pink at top
                        Color(0xFF8A2BE2), // Violet
                        primaryColor       // Primary brand color at base
                    )
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            // Glowing Peak Cap
            drawRoundRect(
                color = Color.White.copy(alpha = if (isPlaying) 0.9f else 0.3f),
                topLeft = Offset(x, (y - 3f).coerceAtLeast(0f)),
                size = Size(barWidth, 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
        }
    }
}

@Composable
fun WaveformVisualizerCanvas(
    audioLevel: Float,
    beatLevel: Float,
    isPlaying: Boolean,
    primaryColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        val centerY = size.height / 2f
        val points = 64
        val path = Path()

        for (i in 0..points) {
            val x = (i.toFloat() / points) * size.width
            val normX = (i.toFloat() / points) * (Math.PI * 6.0)
            val amplitude = if (isPlaying) (size.height * 0.4f * audioLevel).coerceIn(4f, size.height * 0.45f) else 2f
            val beatFactor = if (isPlaying) (1f + beatLevel * 0.5f) else 1f
            val y = centerY + (kotlin.math.sin(normX + (audioLevel * 10f)).toFloat() * amplitude * beatFactor)

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Draw waveform path glow
        drawPath(
            path = path,
            color = primaryColor.copy(alpha = 0.3f),
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )
        // Draw sharp core line
        drawPath(
            path = path,
            color = Color.Cyan,
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun BeatPulseVisualizerCanvas(
    audioLevel: Float,
    beatLevel: Float,
    isPlaying: Boolean,
    primaryColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = (size.height / 2f) - 8f

        val baseRadius = maxRadius * 0.4f
        val pulseRadius = if (isPlaying) baseRadius + (maxRadius - baseRadius) * beatLevel else baseRadius

        // Outer glowing pulse ring
        drawCircle(
            color = primaryColor.copy(alpha = if (isPlaying) (beatLevel * 0.5f).coerceIn(0.1f, 0.6f) else 0.1f),
            radius = pulseRadius,
            center = center
        )

        // Mid reactive ring
        drawCircle(
            color = Color.Cyan.copy(alpha = if (isPlaying) (audioLevel * 0.7f).coerceIn(0.2f, 0.8f) else 0.2f),
            radius = baseRadius + (pulseRadius - baseRadius) * 0.5f,
            center = center,
            style = Stroke(width = 4f)
        )

        // Radial spectrum spikes
        val spikes = 24
        for (i in 0 until spikes) {
            val angleRad = (i * 360f / spikes) * (Math.PI / 180f).toFloat()
            val factor = if (isPlaying) (audioLevel * (0.3f + 0.7f * kotlin.math.sin(i.toDouble()).toFloat())).coerceIn(0.1f, 1f) else 0.1f
            val innerR = pulseRadius * 0.8f
            val outerR = innerR + (maxRadius - innerR) * factor

            val startX = center.x + innerR * kotlin.math.cos(angleRad)
            val startY = center.y + innerR * kotlin.math.sin(angleRad)
            val endX = center.x + outerR * kotlin.math.cos(angleRad)
            val endY = center.y + outerR * kotlin.math.sin(angleRad)

            drawLine(
                color = if (i % 2 == 0) primaryColor else Color.Cyan,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun MiniTrackVisualizer(
    audioLevel: Float,
    isPlaying: Boolean,
    primaryColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(18.dp)
    ) {
        val bars = 4
        for (i in 0 until bars) {
            val heightFactor = if (isPlaying) {
                val offset = kotlin.math.sin(i * 1.5 + (audioLevel * 8f)).toFloat() * 0.4f + 0.6f
                (audioLevel * offset).coerceIn(0.2f, 1f)
            } else {
                0.2f
            }
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(heightFactor)
                    .clip(RoundedCornerShape(1.dp))
                    .background(primaryColor)
            )
        }
    }
}
