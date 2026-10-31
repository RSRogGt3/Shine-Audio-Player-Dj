package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.models.StreamingProvider
import com.example.ui.models.StreamingTrack
import com.example.ui.viewmodels.LocalMusicViewModel

@Composable
fun StreamingHubSection(
    viewModel: LocalMusicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val streamingTracks by viewModel.streamingTracks.collectAsState()
    val selectedProvider by viewModel.selectedStreamingProvider.collectAsState()
    val searchQuery by viewModel.streamingSearchQuery.collectAsState()

    val isSpotifyConnected by viewModel.isSpotifyConnected.collectAsState()
    val isYouTubeConnected by viewModel.isYouTubeMusicConnected.collectAsState()
    val isAmazonConnected by viewModel.isAmazonMusicConnected.collectAsState()

    var showAddStreamDialog by remember { mutableStateOf(false) }

    // Filter tracks based on service provider, active connection switch, and search query
    val filteredTracks = remember(streamingTracks, selectedProvider, searchQuery, isSpotifyConnected, isYouTubeConnected, isAmazonConnected) {
        streamingTracks.filter { track ->
            val isProviderActive = when (track.provider) {
                StreamingProvider.SPOTIFY -> isSpotifyConnected
                StreamingProvider.YOUTUBE_MUSIC -> isYouTubeConnected
                StreamingProvider.AMAZON_MUSIC -> isAmazonConnected
            }
            val matchesProvider = selectedProvider == null || track.provider == selectedProvider
            val matchesQuery = searchQuery.isBlank() ||
                    track.title.contains(searchQuery, ignoreCase = true) ||
                    track.artist.contains(searchQuery, ignoreCase = true) ||
                    track.album.contains(searchQuery, ignoreCase = true) ||
                    track.genre.contains(searchQuery, ignoreCase = true)
            isProviderActive && matchesProvider && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("streaming_hub_section")
    ) {
        // STREAMING HEADER BANNER
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF141320),
            border = BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF1DB954), // Spotify Green
                        Color(0xFFFF0000), // YouTube Red
                        Color(0xFF00A8E1)  // Amazon Blue
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "STREAMING HUB",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Spotify • YouTube Music • Amazon Music direkt in Decks auflegen",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        maxLines = 1
                    )
                }

                // Add Custom Stream Button
                Button(
                    onClick = { showAddStreamDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26253A)),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_custom_stream_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "URL +",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                }
            }
        }

        // PROVIDER SELECTION CHIPS
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ProviderFilterChip(
                    label = "ALLE DIENSTE",
                    isSelected = selectedProvider == null,
                    activeColor = Color(0xFF00E5FF),
                    iconRes = R.drawable.ic_music_note,
                    onClick = { viewModel.selectStreamingProvider(null) }
                )
            }
            items(StreamingProvider.entries) { provider ->
                ProviderFilterChip(
                    label = provider.displayName.uppercase(),
                    isSelected = selectedProvider == provider,
                    activeColor = Color(provider.colorHex),
                    iconRes = provider.iconRes,
                    onClick = {
                        if (selectedProvider == provider) {
                            viewModel.selectStreamingProvider(null)
                        } else {
                            viewModel.selectStreamingProvider(provider)
                        }
                    }
                )
            }
        }

        // SEARCH BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setStreamingSearchQuery(it) },
            placeholder = {
                Text(
                    text = "Streaming Titel, Künstler oder Genre suchen...",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = "Suchen", tint = Color.Gray)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setStreamingSearchQuery("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Löschen", tint = Color.Gray)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF161522),
                unfocusedContainerColor = Color(0xFF161522),
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF262538),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .height(52.dp)
                .testTag("streaming_search_input")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // TRACK LIST
        if (filteredTracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Headphones,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Keine passenden Streaming-Tracks gefunden",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = {
                        viewModel.selectStreamingProvider(null)
                        viewModel.setStreamingSearchQuery("")
                    }) {
                        Text("Filter zurücksetzen", color = Color(0xFF00E5FF))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredTracks, key = { it.id }) { track ->
                    StreamingTrackCard(
                        track = track,
                        onLoadDeckA = {
                            viewModel.loadStreamingTrackToDeckA(context, track)
                            Toast.makeText(context, "In DECK A geladen: ${track.title}", Toast.LENGTH_SHORT).show()
                        },
                        onLoadDeckB = {
                            viewModel.loadStreamingTrackToDeckB(context, track)
                            Toast.makeText(context, "In DECK B geladen: ${track.title}", Toast.LENGTH_SHORT).show()
                        },
                        onOpenInExternalApp = {
                            openStreamingService(context, track)
                        }
                    )
                }
            }
        }
    }

    // ADD CUSTOM STREAM DIALOG
    if (showAddStreamDialog) {
        AddCustomStreamDialog(
            onDismiss = { showAddStreamDialog = false },
            onAddStream = { title, artist, url, provider, bpm ->
                viewModel.addCustomStreamingTrack(title, artist, url, provider, bpm)
                showAddStreamDialog = false
                Toast.makeText(context, "Stream hinzugefügt: $title", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun ProviderFilterChip(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    iconRes: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) activeColor.copy(alpha = 0.25f) else Color(0xFF1A1926),
        border = BorderStroke(1.dp, if (isSelected) activeColor else Color(0xFF333246))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.LightGray
            )
        }
    }
}

@Composable
fun StreamingTrackCard(
    track: StreamingTrack,
    onLoadDeckA: () -> Unit,
    onLoadDeckB: () -> Unit,
    onOpenInExternalApp: () -> Unit
) {
    val brandColor = Color(track.provider.colorHex)

    Surface(
        onClick = onLoadDeckA,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF141320),
        border = BorderStroke(1.dp, brandColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Track Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Provider Badge Circle
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(brandColor.copy(alpha = 0.2f))
                            .border(1.dp, brandColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = track.provider.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = track.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${track.artist} • ${track.album}",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // BPM & Genre Pill
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF222034)
                    ) {
                        Text(
                            text = "${track.bpm} BPM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = brandColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = track.genre,
                        fontSize = 9.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: [ LOAD DECK A ] [ LOAD DECK B ] [ IN APP ÖFFNEN ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Load Deck A (Cyan)
                Button(
                    onClick = onLoadDeckA,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(34.dp)
                ) {
                    Text(
                        text = "DECK A",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }

                // Load Deck B (Magenta)
                Button(
                    onClick = onLoadDeckB,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(34.dp)
                ) {
                    Text(
                        text = "DECK B",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                // Open in Original App
                OutlinedButton(
                    onClick = onOpenInExternalApp,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, brandColor),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.1f).height(34.dp)
                ) {
                    Text(
                        text = "Öffnen",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = brandColor
                    )
                }
            }
        }
    }
}

@Composable
fun AddCustomStreamDialog(
    onDismiss: () -> Unit,
    onAddStream: (title: String, artist: String, url: String, provider: StreamingProvider, bpm: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var bpmText by remember { mutableStateOf("128") }
    var selectedProvider by remember { mutableStateOf(StreamingProvider.SPOTIFY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF181726),
        title = {
            Text(
                text = "Eigenen Stream hinzufügen",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Füge eine beliebige MP3/AAC-Stream-URL oder Radiostreams zu deiner DJ-Konsole hinzu.",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Künstler / Sender") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Stream URL (https://...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bpmText,
                    onValueChange = { bpmText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("BPM (Tempo)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Dienst-Kategorie:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StreamingProvider.entries.forEach { provider ->
                        val isSelected = selectedProvider == provider
                        Surface(
                            onClick = { selectedProvider = provider },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(provider.colorHex).copy(alpha = 0.3f) else Color(0xFF222134),
                            border = BorderStroke(1.dp, if (isSelected) Color(provider.colorHex) else Color.Transparent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = provider.displayName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (url.isNotBlank()) {
                        val bpm = bpmText.toIntOrNull() ?: 128
                        onAddStream(title, artist, url, selectedProvider, bpm)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text("Hinzufügen", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = Color.LightGray)
            }
        }
    )
}

private fun openStreamingService(context: Context, track: StreamingTrack) {
    try {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(track.provider.packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(track.externalUrl))
            context.startActivity(webIntent)
        }
    } catch (_: Exception) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(track.externalUrl))
        try {
            context.startActivity(webIntent)
        } catch (_: Exception) {}
    }
}
