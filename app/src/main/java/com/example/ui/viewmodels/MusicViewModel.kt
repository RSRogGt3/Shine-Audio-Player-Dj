package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.Part
import com.example.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<MusicUiState>(MusicUiState.Idle)
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    fun generateTrack(prompt: String, apiKey: String) {
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
                val response = RetrofitClient.service.generateContent("lyria-3-clip-preview", apiKey, request)
                
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
}

sealed class MusicUiState {
    object Idle : MusicUiState()
    object Loading : MusicUiState()
    data class Success(val base64Audio: String) : MusicUiState()
    data class Error(val message: String) : MusicUiState()
}
