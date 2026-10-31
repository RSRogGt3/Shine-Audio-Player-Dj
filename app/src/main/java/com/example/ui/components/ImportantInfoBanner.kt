package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ImportantInfoBanner(
    isPermissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(!isPermissionGranted) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("important_info_banner"),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF161524),
        border = BorderStroke(1.dp, if (!isPermissionGranted) Color(0xFF00E5FF).copy(alpha = 0.5f) else Color(0xFF262538))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Row (Clickable to collapse/expand)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Wichtige Informationen & Datenschutz",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Einklappen" else "Ausklappen",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Item 1: Streaming without storage permissions
                    InfoRowItem(
                        icon = Icons.Filled.CloudDone,
                        iconColor = Color(0xFF00E676),
                        title = "Keine Berechtigung für Streaming nötig",
                        description = "Spotify, YouTube Music, Amazon Music und Demo-Tracks laufen komplett ohne Dateizugriff auf dein Handy."
                    )

                    // Item 2: Optional local files
                    InfoRowItem(
                        icon = Icons.Filled.FolderOpen,
                        iconColor = Color(0xFF00E5FF),
                        title = "Lokale Handy-Dateien (Optional)",
                        description = "Die Speicherberechtigung ist rein optional und wird nur benötigt, wenn du eigene MP3s von deiner SD-Karte oder deinem Download-Ordner laden willst."
                    )

                    // Item 3: Microphone security
                    InfoRowItem(
                        icon = Icons.Filled.Mic,
                        iconColor = Color(0xFFFFB300),
                        title = "Mikrofon nur für Live-Visualizer",
                        description = "Das Mikrofon wird ausschließlich auf deinem Gerät für den Echtzeit-Frequenz-Equalizer und eigene Aufnahmen genutzt. Keine Daten werden gesendet."
                    )

                    // Item 4: Audio Clipping Alert
                    InfoRowItem(
                        icon = Icons.Filled.GraphicEq,
                        iconColor = Color(0xFFFF1744),
                        title = "Automatischer Clipping-Schutz",
                        description = "Der DJ-Player warnt dich automatisch bei Pegel-Übersteuerung (>1.2x), um Verzerrungen und Lautsprecherschäden zu vermeiden."
                    )

                    // Optional Permission Button if not granted
                    if (!isPermissionGranted) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26253C)),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Folder,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lokale Handy-Dateien freischalten (Optional)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 10.sp,
                color = Color.LightGray,
                lineHeight = 14.sp
            )
        }
    }
}
