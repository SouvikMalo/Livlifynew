package com.livlify.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.livlify.app.data.MusicRepository
import com.livlify.app.data.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LibraryState {
    data object Loading : LibraryState
    data object Empty : LibraryState
    data class Ready(val songs: List<Song>) : LibraryState
}

class LibraryViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = MusicRepository(app)

    private val _state = MutableStateFlow<LibraryState>(LibraryState.Loading)
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = LibraryState.Loading
            val songs = repository.loadSongs()
            _state.value = if (songs.isEmpty()) LibraryState.Empty else LibraryState.Ready(songs)
        }
    }
}
