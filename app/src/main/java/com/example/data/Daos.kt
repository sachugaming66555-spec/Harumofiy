package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY playCount DESC")
    fun getAllTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    fun getTrackById(id: Long): Flow<Track?>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackByIdSync(id: Long): Track?

    @Query("SELECT * FROM tracks WHERE isLiked = 1")
    fun getLikedTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isDownloaded = 1")
    fun getDownloadedTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE genre = :genre")
    fun getTracksByGenre(genre: String): Flow<List<Track>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTracks(tracks: List<Track>)

    @Update
    suspend fun updateTrack(track: Track)

    @Query("UPDATE tracks SET isLiked = :isLiked WHERE id = :trackId")
    suspend fun setLiked(trackId: Long, isLiked: Boolean)

    @Query("UPDATE tracks SET isDownloaded = :isDownloaded, downloadProgress = :progress WHERE id = :trackId")
    suspend fun setDownloaded(trackId: Long, isDownloaded: Boolean, progress: Int)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY id DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun getPlaylistById(id: Long): Flow<Playlist?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrackCrossRef(ref: PlaylistTrackCrossRef)

    @Delete
    suspend fun deletePlaylistTrackCrossRef(ref: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun deletePlaylistTracks(playlistId: Long)

    @Query("""
        SELECT * FROM tracks 
        INNER JOIN playlist_track_cross_ref ON tracks.id = playlist_track_cross_ref.trackId 
        WHERE playlist_track_cross_ref.playlistId = :playlistId
    """)
    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>>
}

@Dao
interface FriendDao {
    @Query("SELECT * FROM friend_activities")
    fun getFriendActivities(): Flow<List<FriendActivity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<FriendActivity>)

    @Update
    suspend fun updateFriendActivity(friend: FriendActivity)
}
