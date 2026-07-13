package com.example.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val trackDao = database.trackDao()
    private val playlistDao = database.playlistDao()
    private val friendDao = database.friendDao()

    val allTracks: Flow<List<Track>> = trackDao.getAllTracks()
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    val likedTracks: Flow<List<Track>> = trackDao.getLikedTracks()
    val downloadedTracks: Flow<List<Track>> = trackDao.getDownloadedTracks()
    val friendActivities: Flow<List<FriendActivity>> = friendDao.getFriendActivities()

    init {
        // Run database seeding on initialization in a IO coroutine
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialData()
        }
    }

    fun getTrackById(id: Long): Flow<Track?> = trackDao.getTrackById(id)
    
    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> = playlistDao.getTracksForPlaylist(playlistId)

    suspend fun toggleLike(trackId: Long) = withContext(Dispatchers.IO) {
        val track = trackDao.getTrackByIdSync(trackId)
        if (track != null) {
            trackDao.setLiked(trackId, !track.isLiked)
        }
    }

    suspend fun startDownload(trackId: Long) = withContext(Dispatchers.IO) {
        // Simulate incremental downloading
        trackDao.setDownloaded(trackId, isDownloaded = true, progress = 5)
        delay(200)
        trackDao.setDownloaded(trackId, isDownloaded = true, progress = 30)
        delay(250)
        trackDao.setDownloaded(trackId, isDownloaded = true, progress = 65)
        delay(200)
        trackDao.setDownloaded(trackId, isDownloaded = true, progress = 90)
        delay(150)
        trackDao.setDownloaded(trackId, isDownloaded = true, progress = 100)
    }

    suspend fun removeDownload(trackId: Long) = withContext(Dispatchers.IO) {
        trackDao.setDownloaded(trackId, isDownloaded = false, progress = 0)
    }

    suspend fun createPlaylist(name: String, description: String, coverUrl: String = ""): Long = withContext(Dispatchers.IO) {
        val finalCover = coverUrl.ifEmpty {
            "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400&q=80" // default cover
        }
        val playlist = Playlist(name = name, description = description, coverUrl = finalCover, isCustom = true)
        playlistDao.insertPlaylist(playlist)
    }

    suspend fun deletePlaylist(playlist: Playlist) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistTracks(playlist.id)
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylistTrackCrossRef(PlaylistTrackCrossRef(playlistId, trackId))
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistTrackCrossRef(PlaylistTrackCrossRef(playlistId, trackId))
    }

    suspend fun simulateFriendActivityUpdate() = withContext(Dispatchers.IO) {
        // Change song listening titles dynamically for social activity updates
        val friends = friendDao.getFriendActivities().first()
        if (friends.isNotEmpty()) {
            val randomFriend = friends.random()
            val tracks = trackDao.getAllTracks().first()
            if (tracks.isNotEmpty()) {
                val randomTrack = tracks.random()
                friendDao.updateFriendActivity(
                    randomFriend.copy(
                        currentTrackTitle = randomTrack.title,
                        currentTrackArtist = randomTrack.artist,
                        isPlaying = true,
                        lastActive = "Listening now"
                    )
                )
            }
        }
    }

    private suspend fun seedInitialData() {
        val existingTracks = trackDao.getAllTracks().first()
        if (existingTracks.isEmpty()) {
            // Seed Tracks
            val defaultTracks = listOf(
                Track(
                    id = 1,
                    title = "Sunset Boulevard",
                    artist = "Lofi Dreamer",
                    album = "Chilled Beats",
                    duration = 184,
                    audioUrl = "simulated_audio_sunset_boulevard.mp3",
                    coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&q=80",
                    lyrics = "Reflections on the glass...\nAs the city starts to glow...\nWe watch the moments pass...\nNowhere left we need to go...\n\nJust us under the twilight sky...",
                    genre = "Lofi",
                    playCount = 1240
                ),
                Track(
                    id = 2,
                    title = "Neon Horizon",
                    artist = "Synthwave Kid",
                    album = "Retro Future",
                    duration = 215,
                    audioUrl = "simulated_audio_neon_horizon.mp3",
                    coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400&q=80",
                    lyrics = "Flying through the grid of light\nSpeeding into endless night\nCan you feel the baseline drop?\nWe are never going to stop...",
                    genre = "Electronic",
                    playCount = 3420
                ),
                Track(
                    id = 3,
                    title = "Acoustic Breeze",
                    artist = "Emma Woods",
                    album = "Woodland Sessions",
                    duration = 152,
                    audioUrl = "simulated_audio_acoustic_breeze.mp3",
                    coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&q=80",
                    lyrics = "Sipping coffee on a Sunday morning\nLeaves are rustling, wind is sighing\nNo alarm, no heavy warning\nJust you and I, slowly flying...",
                    genre = "Acoustic",
                    playCount = 850
                ),
                Track(
                    id = 4,
                    title = "Summer Wave",
                    artist = "Vibe Collector",
                    album = "Beachside Club",
                    duration = 198,
                    audioUrl = "simulated_audio_summer_wave.mp3",
                    coverUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=400&q=80",
                    lyrics = "Golden sands under our feet\nDance with me to this ocean beat\nLet the waves wash it all away\nWe could stay here another day...",
                    genre = "Pop",
                    playCount = 4500
                ),
                Track(
                    id = 5,
                    title = "Midnight Jazz Café",
                    artist = "The Blue Notes",
                    album = "Late Night Lounge",
                    duration = 242,
                    audioUrl = "simulated_audio_midnight_jazz.mp3",
                    coverUrl = "https://images.unsplash.com/photo-1487180142328-0c4e37023af5?w=400&q=80",
                    lyrics = "[Instrumental Saxophone Intro]\n\nIn the quiet corner of the bar\nUnder the light of a fading star\nWhispered notes of sweet desire\nSetting the lonely souls on fire...",
                    genre = "Jazz",
                    playCount = 1100
                ),
                Track(
                    id = 6,
                    title = "Starlight Voyage",
                    artist = "Cosmos",
                    album = "Stargazer",
                    duration = 220,
                    audioUrl = "simulated_audio_starlight_voyage.mp3",
                    coverUrl = "https://images.unsplash.com/photo-1506157786151-b8491531f063?w=400&q=80",
                    lyrics = "Counting stars across the deep blue sky\nAsking ourselves if we could fly\nWill you take my hand tonight?\nWe're sailing past the speed of light...",
                    genre = "Indie",
                    playCount = 2300
                ),
                Track(
                    id = 7,
                    title = "Golden Hour",
                    artist = "Sunkissed",
                    album = "Summer Vibe",
                    duration = 176,
                    audioUrl = "simulated_audio_golden_hour.mp3",
                    coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=400&q=80",
                    lyrics = "Sun is going down, sky of gold\nStories that we've never told\nHolding you close, feeling so free\nThis is where we're meant to be...",
                    genre = "Pop",
                    playCount = 3110
                ),
                Track(
                    id = 8,
                    title = "Rainy Day",
                    artist = "Cozy Cabin",
                    album = "Raindrops",
                    duration = 145,
                    audioUrl = "simulated_audio_rainy_day.mp3",
                    coverUrl = "https://images.unsplash.com/photo-1437622368342-7a3d73a34c8f?w=400&q=80",
                    lyrics = "[Rain sound effects...]\n\nRaindrops tapping on the pane\nSafe and warm, let's stay inside\nLetting go of all the pain\nWe have nothing left to hide...",
                    genre = "Lofi",
                    playCount = 1950
                )
            )
            trackDao.insertTracks(defaultTracks)

            // Seed Playlists
            val lofiChill = Playlist(
                id = 1,
                name = "Lofi Chill Beats",
                description = "Relaxing, ambient lofi tracks for studying, sleeping, or unwinding.",
                coverUrl = "https://images.unsplash.com/photo-1437622368342-7a3d73a34c8f?w=400&q=80",
                isCustom = false
            )
            val acousticWarmth = Playlist(
                id = 2,
                name = "Acoustic Warmth",
                description = "Unplugged acoustic favorites to brighten up your mood.",
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&q=80",
                isCustom = false
            )
            val weeklyDiscoveries = Playlist(
                id = 3,
                name = "Weekly Discoveries",
                description = "Your fresh dose of the best electronic and indie hits.",
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400&q=80",
                isCustom = false
            )

            playlistDao.insertPlaylist(lofiChill)
            playlistDao.insertPlaylist(acousticWarmth)
            playlistDao.insertPlaylist(weeklyDiscoveries)

            // Cross references
            playlistDao.insertPlaylistTrackCrossRef(PlaylistTrackCrossRef(1, 1)) // Sunset Boulevard in Lofi Chill
            playlistDao.insertPlaylistTrackCrossRef(PlaylistTrackCrossRef(1, 8)) // Rainy Day in Lofi Chill

            playlistDao.insertPlaylistTrackCrossRef(PlaylistTrackCrossRef(2, 3)) // Acoustic Breeze in Acoustic Warmth
            playlistDao.insertPlaylistTrackCrossRef(PlaylistTrackCrossRef(2, 5)) // Midnight Jazz in Acoustic Warmth

            playlistDao.insertPlaylistTrackCrossRef(PlaylistTrackCrossRef(3, 2)) // Neon Horizon in Weekly Discoveries
            playlistDao.insertPlaylistTrackCrossRef(PlaylistTrackCrossRef(3, 4)) // Summer Wave in Weekly Discoveries
            playlistDao.insertPlaylistTrackCrossRef(PlaylistTrackCrossRef(3, 6)) // Starlight Voyage in Weekly Discoveries
            playlistDao.insertPlaylistTrackCrossRef(PlaylistTrackCrossRef(3, 7)) // Golden Hour in Weekly Discoveries

            // Seed Friends
            val defaultFriends = listOf(
                FriendActivity(
                    id = 1,
                    name = "Sarah Jenkins",
                    avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=120&q=80",
                    currentTrackTitle = "Sunset Boulevard",
                    currentTrackArtist = "Lofi Dreamer",
                    isPlaying = true,
                    lastActive = "Listening now"
                ),
                FriendActivity(
                    id = 2,
                    name = "David Chen",
                    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=120&q=80",
                    currentTrackTitle = "Neon Horizon",
                    currentTrackArtist = "Synthwave Kid",
                    isPlaying = false,
                    lastActive = "Active 5m ago"
                ),
                FriendActivity(
                    id = 3,
                    name = "Maya Patel",
                    avatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=120&q=80",
                    currentTrackTitle = "Acoustic Breeze",
                    currentTrackArtist = "Emma Woods",
                    isPlaying = true,
                    lastActive = "Listening now"
                )
            )
            friendDao.insertFriends(defaultFriends)
        }
    }
}
