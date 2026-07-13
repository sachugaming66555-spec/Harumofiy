package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long, // in seconds
    val audioUrl: String, // Simulated or actual audio source
    val coverUrl: String,
    val isDownloaded: Boolean = false,
    val downloadProgress: Int = 0, // 0 to 100 for simulated download tracking
    val isLiked: Boolean = false,
    val lyrics: String = "",
    val genre: String = "Unknown",
    val playCount: Int = 0
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val coverUrl: String,
    val isCustom: Boolean = true
)

@Entity(tableName = "playlist_track_cross_ref", primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrackCrossRef(
    val playlistId: Long,
    val trackId: Long
)

@Entity(tableName = "friend_activities")
data class FriendActivity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarUrl: String,
    val currentTrackTitle: String,
    val currentTrackArtist: String,
    val isPlaying: Boolean,
    val lastActive: String
)
