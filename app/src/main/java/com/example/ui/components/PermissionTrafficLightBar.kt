package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionTrafficLightBar(
    isStorageGranted: Boolean,
    isMicGranted: Boolean,
    isNotificationGranted: Boolean,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("permission_traffic_light_bar"),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF141322),
        border = BorderStroke(1.dp, Color(0xFF26253C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BERECHTIGUNGS-AMPEL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = if (isStorageGranted && isMicGranted) "Vollzugriff aktiv" else "Streaming frei • Speicher optional",
                    fontSize = 9.sp,
                    color = Color.LightGray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Traffic Light Status Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Storage Permission
                TrafficLightPill(
                    label = "Speicher",
                    icon = Icons.Filled.Folder,
                    isGranted = isStorageGranted,
                    optionalText = "Optional",
                    onClick = { if (!isStorageGranted) onRequestPermissions() },
                    modifier = Modifier.weight(1f)
                )

                // 2. Microphone Permission
                TrafficLightPill(
                    label = "Mikrofon",
                    icon = Icons.Filled.Mic,
                    isGranted = isMicGranted,
                    optionalText = "Visualizer",
                    onClick = { if (!isMicGranted) onRequestPermissions() },
                    modifier = Modifier.weight(1f)
                )

                // 3. Notification Permission
                TrafficLightPill(
                    label = "HUD/Lock",
                    icon = Icons.Filled.Notifications,
                    isGranted = isNotificationGranted,
                    optionalText = "Optional",
                    onClick = { if (!isNotificationGranted) onRequestPermissions() },
                    modifier = Modifier.weight(1f)
                )

                // 4. Cloud Audio (Zero-Permission Engine)
                TrafficLightPill(
                    label = "Cloud/Dienste",
                    icon = Icons.Filled.CloudDone,
                    isGranted = true, // Always ready
                    optionalText = "100% Bereit",
                    onClick = {},
                    modifier = Modifier.weight(1.1f)
                )
            }
        }
    }
}

@Composable
private fun TrafficLightPill(
    label: String,
    icon: ImageVector,
    isGranted: Boolean,
    optionalText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = if (isGranted) Color(0xFF00E676) else Color(0xFFFFB300)
    val bgColor = if (isGranted) Color(0x1A00E676) else Color(0x1AFFB300)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Glowing Indicator Dot
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Column {
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = if (isGranted) "Aktiv" else optionalText,
                    fontSize = 8.sp,
                    color = statusColor,
                    maxLines = 1
                )
            }
        }
    }
}
