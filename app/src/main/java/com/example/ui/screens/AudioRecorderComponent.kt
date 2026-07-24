package com.example.ui.screens

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioRecorderManager
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AudioRecorderComponent(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF00E5FF),
    onRecordingFinished: (File) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val recorderManager = remember { AudioRecorderManager(context) }
    
    var isRecording by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf("WAV (192000Hz)") }
    var hasPermission by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B26)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Mic, contentDescription = "Mic", tint = if (isRecording) Color.Red else primaryColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AUDIO RECORDER",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (!hasPermission) {
                Text(
                    text = "Mikrofon-Berechtigung erforderlich.",
                    color = Color.Red,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black)
                ) {
                    Text("Berechtigung anfragen", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = selectedFormat == "WAV (192000Hz)",
                        onClick = { if (!isRecording) selectedFormat = "WAV (192000Hz)" },
                        label = { Text("WAV 192kHz", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = selectedFormat == "MP3 (48000Hz)",
                        onClick = { if (!isRecording) selectedFormat = "MP3 (48000Hz)" },
                        label = { Text("MP3 48kHz", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = Color.Black
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isRecording) {
                            recorderManager.stopRecording()
                            isRecording = false
                        } else {
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                            val isWav = selectedFormat == "WAV (192000Hz)"
                            val extension = if (isWav) "wav" else "mp3"
                            val musicDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC) ?: context.cacheDir
                            if (!musicDir.exists()) {
                                musicDir.mkdirs()
                            }
                            val file = File(musicDir, "record_${timestamp}.${extension}")
                            
                            isRecording = true
                            coroutineScope.launch {
                                if (isWav) {
                                    recorderManager.startRecordingWav(file)
                                } else {
                                    recorderManager.startRecordingMp3(file)
                                }
                                android.media.MediaScannerConnection.scanFile(
                                    context,
                                    arrayOf(file.absolutePath),
                                    null,
                                    null
                                )
                                onRecordingFinished(file)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) Color.Red else primaryColor,
                        contentColor = if (isRecording) Color.White else Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = if (isRecording) "Stop" else "Record"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRecording) "AUFNAHME STOPPEN" else "AUFNAHME STARTEN",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
