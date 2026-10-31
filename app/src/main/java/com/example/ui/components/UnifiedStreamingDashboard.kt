package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.StreamingProvider
import com.example.ui.viewmodels.LocalMusicViewModel
import com.example.ui.viewmodels.LocalTrack

@Composable
fun UnifiedStreamingDashboard(
    viewModel: LocalMusicViewModel,
    isStorageGranted: Boolean,
    isMicGranted: Boolean,
    isNotificationGranted: Boolean,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSpotifyConnected by viewModel.isSpotifyConnected.collectAsState()
    val isYouTubeConnected by viewModel.isYouTubeMusicConnected.collectAsState()
    val isAmazonConnected by viewModel.isAmazonMusicConnected.collectAsState()

    val currentTrackA by viewModel.currentTrackA.collectAsState()
    val currentTrackB by viewModel.currentTrackB.collectAsState()
    val isPlayingA by viewModel.isPlayingA.collectAsState()
    val isPlayingB by viewModel.isPlayingB.collectAsState()
    val bpmA by viewModel.bpmA.collectAsState()
    val bpmB by viewModel.bpmB.collectAsState()

    var isDashboardExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("unified_streaming_dashboard"),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF141322),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF1DB954), // Spotify Green
                    Color(0xFFFF0000), // YouTube Music Red
                    Color(0xFF00A8E1)  // Amazon Music Blue
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Dashboard Header with Toggle and Traffic Light
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isDashboardExpanded = !isDashboardExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF232138)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Dashboard,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "STREAMING & AUDIO HUB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isDashboardExpanded) "Spotify • YouTube Music • Amazon Music" else "Tippen für Stream-Schalter & Details",
                            fontSize = 8.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Quick status dots
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isSpotifyConnected) Color(0xFF1DB954) else Color.DarkGray)
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isYouTubeConnected) Color(0xFFFF0000) else Color.DarkGray)
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isAmazonConnected) Color(0xFF00A8E1) else Color.DarkGray)
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Icon(
                        imageVector = if (isDashboardExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isDashboardExpanded) "Einklappen" else "Ausklappen",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Kompakte Berechtigungs-Ampel in der Kopfzeile
            PermissionTrafficLightBar(
                isStorageGranted = isStorageGranted,
                isMicGranted = isMicGranted,
                isNotificationGranted = isNotificationGranted,
                onRequestPermissions = onRequestPermissions
            )

            AnimatedVisibility(visible = isDashboardExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 2. Active Connections Cards
                    // SPOTIFY
                    StreamingServiceConnectionCard(
                        provider = StreamingProvider.SPOTIFY,
                        isConnected = isSpotifyConnected,
                        onToggle = { viewModel.toggleSpotifyConnection() },
                        currentPlaybackStatus = getPlaybackStatusForProvider(
                            provider = StreamingProvider.SPOTIFY,
                            trackA = currentTrackA,
                            trackB = currentTrackB,
                            isPlayingA = isPlayingA,
                            isPlayingB = isPlayingB,
                            bpmA = bpmA.toInt(),
                            bpmB = bpmB.toInt()
                        ),
                        onOpenApp = { openServiceApp(context, StreamingProvider.SPOTIFY) }
                    )

                    // YOUTUBE MUSIC
                    StreamingServiceConnectionCard(
                        provider = StreamingProvider.YOUTUBE_MUSIC,
                        isConnected = isYouTubeConnected,
                        onToggle = { viewModel.toggleYouTubeMusicConnection() },
                        currentPlaybackStatus = getPlaybackStatusForProvider(
                            provider = StreamingProvider.YOUTUBE_MUSIC,
                            trackA = currentTrackA,
                            trackB = currentTrackB,
                            isPlayingA = isPlayingA,
                            isPlayingB = isPlayingB,
                            bpmA = bpmA.toInt(),
                            bpmB = bpmB.toInt()
                        ),
                        onOpenApp = { openServiceApp(context, StreamingProvider.YOUTUBE_MUSIC) }
                    )

                    // AMAZON MUSIC
                    StreamingServiceConnectionCard(
                        provider = StreamingProvider.AMAZON_MUSIC,
                        isConnected = isAmazonConnected,
                        onToggle = { viewModel.toggleAmazonMusicConnection() },
                        currentPlaybackStatus = getPlaybackStatusForProvider(
                            provider = StreamingProvider.AMAZON_MUSIC,
                            trackA = currentTrackA,
                            trackB = currentTrackB,
                            isPlayingA = isPlayingA,
                            isPlayingB = isPlayingB,
                            bpmA = bpmA.toInt(),
                            bpmB = bpmB.toInt()
                        ),
                        onOpenApp = { openServiceApp(context, StreamingProvider.AMAZON_MUSIC) }
                    )
                }
            }
        }
    }
}

@Composable
fun StreamingServiceConnectionCard(
    provider: StreamingProvider,
    isConnected: Boolean,
    onToggle: () -> Unit,
    currentPlaybackStatus: String,
    onOpenApp: () -> Unit
) {
    val brandColor = Color(provider.colorHex)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF19182A),
        border = BorderStroke(
            1.dp,
            if (isConnected) brandColor.copy(alpha = 0.5f) else Color(0xFF2B2A3D)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Service Info & Status
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Service Icon Circle
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) brandColor.copy(alpha = 0.2f) else Color(0xFF262536))
                        .border(1.dp, if (isConnected) brandColor else Color.DarkGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = provider.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = provider.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) Color(0xFF00E676) else Color.Gray)
                        )
                    }

                    Text(
                        text = if (isConnected) currentPlaybackStatus else "Verbindung getrennt",
                        fontSize = 10.sp,
                        color = if (isConnected) brandColor else Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick App Open & Toggle Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TextButton(
                    onClick = onOpenApp,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "App ↗",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = brandColor
                    )
                }

                Switch(
                    checked = isConnected,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = brandColor,
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color(0xFF2B2A3D)
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}

private fun getPlaybackStatusForProvider(
    provider: StreamingProvider,
    trackA: LocalTrack?,
    trackB: LocalTrack?,
    isPlayingA: Boolean,
    isPlayingB: Boolean,
    bpmA: Int,
    bpmB: Int
): String {
    val isDeckA = trackA?.artist?.contains("[${provider.displayName}]") == true
    val isDeckB = trackB?.artist?.contains("[${provider.displayName}]") == true

    return when {
        isDeckA && isPlayingA -> "▶ LIVE DECK A • $bpmA BPM"
        isDeckB && isPlayingB -> "▶ LIVE DECK B • $bpmB BPM"
        isDeckA -> "PAUSE (DECK A) • ${trackA.title}"
        isDeckB -> "PAUSE (DECK B) • ${trackB.title}"
        else -> "Bereit zum Streamen & Mixen"
    }
}

private fun openServiceApp(context: Context, provider: StreamingProvider) {
    try {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(provider.packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(provider.defaultUrl))
            context.startActivity(webIntent)
        }
    } catch (_: Exception) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(provider.defaultUrl))
            context.startActivity(webIntent)
        } catch (_: Exception) {}
    }
}
