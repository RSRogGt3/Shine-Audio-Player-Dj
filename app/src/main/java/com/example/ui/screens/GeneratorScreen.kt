package com.example.ui.screens

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.BuildConfig
import com.example.ui.viewmodels.MusicUiState
import com.example.ui.viewmodels.MusicViewModel
import java.io.File
import java.io.FileOutputStream
import android.util.Base64
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(viewModel: MusicViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var prompt by remember { mutableStateOf("") }
    
    // 0: Short Clip, 1: Full Track, 2: MIDI Sequence
    var generationType by remember { mutableIntStateOf(0) }
    
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF12121A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AI Music Generator",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp, top = 32.dp)
        )

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Describe the track or melody...", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = generationType == 0,
                onClick = { generationType = 0 },
                label = { Text("Short Clip", fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF00E5FF),
                    selectedLabelColor = Color.Black
                )
            )
            FilterChip(
                selected = generationType == 1,
                onClick = { generationType = 1 },
                label = { Text("Full Track", fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE040FB),
                    selectedLabelColor = Color.Black
                )
            )
            FilterChip(
                selected = generationType == 2,
                onClick = { generationType = 2 },
                label = { Text("MIDI Sequence", fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4CAF50),
                    selectedLabelColor = Color.Black
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (generationType == 2) {
                    viewModel.generateMidiSequence(prompt, apiKey, context)
                } else {
                    viewModel.generateTrack(prompt, apiKey, generationType == 0)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (generationType == 2) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = uiState !is MusicUiState.Loading && prompt.isNotBlank()
        ) {
            if (uiState is MusicUiState.Loading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating...", fontWeight = FontWeight.Bold)
            } else {
                Icon(if (generationType == 2) Icons.Filled.Piano else Icons.Filled.MusicNote, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("GENERATE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (val state = uiState) {
            is MusicUiState.Error -> {
                Text(
                    text = state.message,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is MusicUiState.MidiSuccess -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B26)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Piano, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "MIDI Sequence Generated!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Saved to: ${state.midiFilePath}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                Toast.makeText(context, "MIDI saved at ${state.midiFilePath}", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.Black)
                        ) {
                            Text("SHOW LOCATION", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            is MusicUiState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B26)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Generated Track Ready",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (isPlaying) {
                                    mediaPlayer?.stop()
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                    isPlaying = false
                                } else {
                                    try {
                                        val audioBytes = Base64.decode(state.base64Audio, Base64.DEFAULT)
                                        val tempFile = File.createTempFile("generated_audio", ".mp3", context.cacheDir)
                                        FileOutputStream(tempFile).use { it.write(audioBytes) }
                                        
                                        mediaPlayer = MediaPlayer().apply {
                                            setDataSource(tempFile.absolutePath)
                                            prepare()
                                            start()
                                            setOnCompletionListener {
                                                isPlaying = false
                                            }
                                        }
                                        isPlaying = true
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlaying) Color.Red else Color(0xFF00E5FF),
                                contentColor = if (isPlaying) Color.White else Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isPlaying) "STOP PLAYBACK" else "PLAY GENERATED TRACK", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
