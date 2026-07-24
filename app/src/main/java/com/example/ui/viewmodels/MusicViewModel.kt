package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.audio.MidiFileWriter
import com.example.audio.MidiNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream

class MusicViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MusicUiState>(MusicUiState.Idle)
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    fun generateTrack(prompt: String, apiKey: String, isShortClip: Boolean = true) {
        if (apiKey.isEmpty()) {
            _uiState.value = MusicUiState.Error("API Key is missing. Please configure it in Secrets.")
            return
        }

        _uiState.value = MusicUiState.Loading
        viewModelScope.launch {
            try {
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(Part(text = prompt))
                        )
                    ),
                    generationConfig = GenerationConfig(
                        responseModalities = listOf("AUDIO")
                    )
                )

                // Use lyria-3-clip-preview as requested
                val model = if (isShortClip) "lyria-3-clip-preview" else "lyria-3-pro-preview"
                val response = RetrofitClient.service.generateContent(model, apiKey, request)
                
                // Assuming Lyria returns base64 inlineData audio.
                val candidate = response.candidates.firstOrNull()
                val audioPart = candidate?.content?.parts?.find { it.inlineData != null }
                
                if (audioPart?.inlineData != null) {
                    _uiState.value = MusicUiState.Success(audioPart.inlineData.data)
                } else {
                    _uiState.value = MusicUiState.Error("No audio returned from model. Make sure you have Lyria access.")
                }
            } catch (e: Exception) {
                _uiState.value = MusicUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun generateMidiSequence(prompt: String, apiKey: String, context: Context) {
        if (apiKey.isEmpty()) {
            _uiState.value = MusicUiState.Error("API Key is missing. Please configure it in Secrets.")
            return
        }

        _uiState.value = MusicUiState.Loading
        viewModelScope.launch {
            try {
                val systemPrompt = """
                    You are an expert composer. Generate a MIDI sequence based on the user's prompt.
                    Output ONLY valid JSON representing the notes. Do not include markdown code blocks.
                    Format:
                    [
                      {"pitch": 60, "start": 0.0, "duration": 0.5, "velocity": 100},
                      {"pitch": 62, "start": 0.5, "duration": 0.5, "velocity": 100}
                    ]
                    Pitch is MIDI note number (0-127). Start and duration are in quarter notes.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = systemPrompt),
                                Part(text = "User prompt: $prompt")
                            )
                        )
                    )
                )

                val response = RetrofitClient.service.generateContent("gemini-3.1-pro-preview", apiKey, request)
                val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                
                val cleanJson = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                
                val notesList = mutableListOf<MidiNote>()
                val jsonArray = JSONArray(cleanJson)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    notesList.add(
                        MidiNote(
                            pitch = obj.getInt("pitch"),
                            startBeats = obj.getDouble("start").toFloat(),
                            durationBeats = obj.getDouble("duration").toFloat(),
                            velocity = obj.optInt("velocity", 100)
                        )
                    )
                }

                val midiData = MidiFileWriter.createMidi(120, notesList)
                
                withContext(Dispatchers.IO) {
                    val midiDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC) ?: context.cacheDir
                    if (!midiDir.exists()) midiDir.mkdirs()
                    val midiFile = File(midiDir, "generated_sequence_${System.currentTimeMillis()}.mid")
                    FileOutputStream(midiFile).use { it.write(midiData) }
                    
                    withContext(Dispatchers.Main) {
                        _uiState.value = MusicUiState.MidiSuccess(midiFile.absolutePath)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MusicUiState.Error("Failed to generate MIDI: ${e.message}")
            }
        }
    }
}

sealed class MusicUiState {
    object Idle : MusicUiState()
    object Loading : MusicUiState()
    data class Success(val base64Audio: String) : MusicUiState()
    data class MidiSuccess(val midiFilePath: String) : MusicUiState()
    data class Error(val message: String) : MusicUiState()
}
