package com.example.subtitleapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Ensure correct import paths
import com.example.subtitleapp.api.ApiService
import com.example.subtitleapp.api.SubtitleResponse
import com.example.subtitleapp.api.VideoRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

// --- Define SubtitleState ONCE ---
// (Move this to its own file 'SubtitleState.kt' for better organization if preferred)
sealed class SubtitleState {
    object Idle : SubtitleState()
    object Loading : SubtitleState()
    data class Success(val srtContent: String) : SubtitleState()
    data class Error(val message: String) : SubtitleState()
}
// --- End of SubtitleState definition ---

class SubtitleViewModel : ViewModel() {

    private val _state = MutableStateFlow<SubtitleState>(SubtitleState.Idle)
    val state: StateFlow<SubtitleState> = _state.asStateFlow()

    fun generateSubtitles(apiUrl: String, videoUrl: String) {
        viewModelScope.launch {
            _state.value = SubtitleState.Loading
            try {
                // Input validation (basic check if URL seems valid)
                if (!apiUrl.startsWith("http://") && !apiUrl.startsWith("https://")) {
                   _state.value = SubtitleState.Error("Invalid API URL format.")
                   return@launch
                }
                 if (!videoUrl.startsWith("http://") && !videoUrl.startsWith("https://")) {
                   _state.value = SubtitleState.Error("Invalid Video URL format.")
                   return@launch
                }

                val service = ApiService.create(apiUrl)
                val response: Response<SubtitleResponse> = service.generateSubtitles(VideoRequest(videoUrl))

                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        // Assuming SubtitleResponse has srtContent
                        _state.value = SubtitleState.Success(result.srtContent)
                    } ?: run {
                        _state.value = SubtitleState.Error("Empty response body from server")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    _state.value = SubtitleState.Error("API Error: ${response.code()} ${response.message()} - $errorBody")
                }
            } catch (e: Exception) {
                e.printStackTrace() // Log error
                _state.value = SubtitleState.Error(e.message ?: "Network or unexpected error")
            } finally {
                 // Optional: Reset to Idle after a short delay if needed, or rely on UI observation
                 // if (_state.value is SubtitleState.Success || _state.value is SubtitleState.Error) {
                 //    kotlinx.coroutines.delay(3000) // Keep message visible for 3 seconds
                 //    _state.value = SubtitleState.Idle
                 // }
            }
        }
    }
}
