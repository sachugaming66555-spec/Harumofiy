package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FriendActivity
import com.example.data.MusicRepository
import com.example.data.Playlist
import com.example.data.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MusicRepository(application)

    // UI state flows
    val allTracks: StateFlow<List<Track>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedTracks: StateFlow<List<Track>> = repository.likedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedTracks: StateFlow<List<Track>> = repository.downloadedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val friendActivities: StateFlow<List<FriendActivity>> = repository.friendActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active playback states
    val currentTrack = MutableStateFlow<Track?>(null)
    val isPlaying = MutableStateFlow(false)
    val currentPositionSeconds = MutableStateFlow(0L)
    val isShuffleEnabled = MutableStateFlow(false)
    val isRepeatEnabled = MutableStateFlow(false)

    // Navigation and viewing state
    val currentScreen = MutableStateFlow("home") // "home", "playlists", "social", "library"
    val selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val offlineOnlyMode = MutableStateFlow(false)

    // Search queries
    val searchQuery = MutableStateFlow("")

    // Playlist tracks stream
    val playlistTracks: StateFlow<List<Track>> = selectedPlaylist
        .flatMapLatest { playlist ->
            if (playlist != null) {
                repository.getTracksForPlaylist(playlist.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var playbackJob: Job? = null
    private var socialSimulationJob: Job? = null

    init {
        // Start simulated periodic updates to friend activity for a lively social experience
        startSocialActivitySimulation()
    }

    // Playback control
    fun selectAndPlayTrack(track: Track) {
        currentTrack.value = track
        isPlaying.value = true
        currentPositionSeconds.value = 0L
        startPlaybackTimer()
        
        // Update local play count and save
        viewModelScope.launch {
            val updated = track.copy(playCount = track.playCount + 1)
            // Save inside repository or DAO
            // For now, let's just trigger playback. Track state stays in DB.
        }
    }

    fun togglePlayPause() {
        if (currentTrack.value == null) return
        isPlaying.value = !isPlaying.value
        if (isPlaying.value) {
            startPlaybackTimer()
        } else {
            playbackJob?.cancel()
        }
    }

    fun seekTo(seconds: Long) {
        val maxDuration = currentTrack.value?.duration ?: 0L
        currentPositionSeconds.value = seconds.coerceIn(0L, maxDuration)
    }

    fun skipNext() {
        val tracks = getActiveTrackList()
        if (tracks.isEmpty()) return
        val current = currentTrack.value
        
        val nextIndex = if (isShuffleEnabled.value) {
            tracks.indices.random()
        } else {
            if (current == null) 0 else {
                val index = tracks.indexOfFirst { it.id == current.id }
                if (index == -1 || index == tracks.lastIndex) 0 else index + 1
            }
        }
        selectAndPlayTrack(tracks[nextIndex])
    }

    fun skipPrevious() {
        val tracks = getActiveTrackList()
        if (tracks.isEmpty()) return
        val current = currentTrack.value
        
        val prevIndex = if (isShuffleEnabled.value) {
            tracks.indices.random()
        } else {
            if (current == null) 0 else {
                val index = tracks.indexOfFirst { it.id == current.id }
                if (index == -1) 0 else if (index == 0) tracks.lastIndex else index - 1
            }
        }
        selectAndPlayTrack(tracks[prevIndex])
    }

    fun toggleShuffle() {
        isShuffleEnabled.value = !isShuffleEnabled.value
    }

    fun toggleRepeat() {
        isRepeatEnabled.value = !isRepeatEnabled.value
    }

    private fun getActiveTrackList(): List<Track> {
        // Find which list the user is currently playing from
        val list = if (offlineOnlyMode.value) {
            downloadedTracks.value
        } else if (selectedPlaylist.value != null) {
            playlistTracks.value
        } else {
            allTracks.value
        }
        return list.ifEmpty { allTracks.value }
    }

    private fun startPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (isPlaying.value) {
                val track = currentTrack.value ?: break
                delay(1000)
                if (currentPositionSeconds.value >= track.duration) {
                    if (isRepeatEnabled.value) {
                        currentPositionSeconds.value = 0L
                    } else {
                        skipNext()
                    }
                } else {
                    currentPositionSeconds.value += 1
                }
            }
        }
    }

    // Likes and Downloads
    fun toggleLike(track: Track) {
        viewModelScope.launch {
            repository.toggleLike(track.id)
            // Update currently playing reference if it's the same track
            if (currentTrack.value?.id == track.id) {
                currentTrack.value = currentTrack.value?.copy(isLiked = !track.isLiked)
            }
        }
    }

    fun downloadTrack(track: Track) {
        viewModelScope.launch {
            repository.startDownload(track.id)
            // Update currently playing reference if it's the same track
            if (currentTrack.value?.id == track.id) {
                currentTrack.value = currentTrack.value?.copy(isDownloaded = true, downloadProgress = 100)
            }
        }
    }

    fun removeDownload(track: Track) {
        viewModelScope.launch {
            repository.removeDownload(track.id)
            // Update currently playing reference if it's the same track
            if (currentTrack.value?.id == track.id) {
                currentTrack.value = currentTrack.value?.copy(isDownloaded = false, downloadProgress = 0)
            }
        }
    }

    // Playlist Operations
    fun createPlaylist(name: String, description: String, coverUrl: String = "") {
        viewModelScope.launch {
            repository.createPlaylist(name, description, coverUrl)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            if (selectedPlaylist.value?.id == playlist.id) {
                selectedPlaylist.value = null
            }
            repository.deletePlaylist(playlist)
        }
    }

    fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlist.id, track.id)
        }
    }

    fun removeTrackFromPlaylist(playlist: Playlist, track: Track) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlist.id, track.id)
        }
    }

    // Navigation and offline toggles
    fun setScreen(screen: String) {
        currentScreen.value = screen
        if (screen != "playlists") {
            selectedPlaylist.value = null
        }
    }

    fun viewPlaylist(playlist: Playlist) {
        selectedPlaylist.value = playlist
        currentScreen.value = "playlist_detail"
    }

    fun toggleOfflineOnlyMode() {
        offlineOnlyMode.value = !offlineOnlyMode.value
        // If offline mode is enabled and current track is not downloaded, pause
        if (offlineOnlyMode.value) {
            val current = currentTrack.value
            if (current != null && !current.isDownloaded) {
                isPlaying.value = false
                playbackJob?.cancel()
            }
        }
    }

    // Periodic live simulation of friend activity (Social listen-along)
    private fun startSocialActivitySimulation() {
        socialSimulationJob?.cancel()
        socialSimulationJob = viewModelScope.launch {
            while (true) {
                delay(20000) // update a friend every 20 seconds
                repository.simulateFriendActivityUpdate()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
        socialSimulationJob?.cancel()
    }
}
