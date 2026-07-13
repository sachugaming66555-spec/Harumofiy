package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.OfflinePin
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.Playlist
import com.example.data.Track
import com.example.ui.MusicViewModel
import com.example.ui.theme.*
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val allTracks by viewModel.allTracks.collectAsState()
    val allPlaylists by viewModel.allPlaylists.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val isOfflineMode by viewModel.offlineOnlyMode.collectAsState()
    val currentPlayingTrack by viewModel.currentTrack.collectAsState()

    val displayTracks = if (isOfflineMode) downloadedTracks else allTracks

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Hero Section Gradient
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                SpotifyGreen.copy(alpha = 0.3f),
                                DeepBlack
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = greeting,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.testTag("home_greeting")
                        )

                        // Offline Mode Indicator Button
                        FilterChip(
                            selected = isOfflineMode,
                            onClick = { viewModel.toggleOfflineOnlyMode() },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.OfflinePin,
                                        contentDescription = "Offline Mode",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Offline Only")
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SpotifyGreen,
                                selectedLabelColor = DeepBlack,
                                containerColor = LightCharcoal,
                                labelColor = Color.White
                            ),
                            modifier = Modifier.testTag("offline_toggle")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Your daily personalized soundscape is ready.",
                        color = MutedGray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Active Offline Filter Alert
        if (isOfflineMode) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = SpotifyGreen.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OfflinePin,
                            contentDescription = "Offline Mode Active",
                            tint = SpotifyGreen
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Offline Mode Active",
                                color = SpotifyGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Showing only tracks you downloaded to your device.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Featured Playlists section (Not filtered because playlists are guides, but will list tracks inside)
        if (allPlaylists.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Text(
                        text = "Featured Playlists",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(allPlaylists) { playlist ->
                            PlaylistCard(
                                playlist = playlist,
                                onClick = { viewModel.viewPlaylist(playlist) }
                            )
                        }
                    }
                }
            }
        }

        // Dynamic Recommendations Section
        item {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = "Trending Now",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                )

                if (displayTracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tracks available offline. Download tracks to play in offline mode!",
                            color = MutedGray,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(displayTracks.take(4)) { track ->
                            TrackCoverCard(
                                track = track,
                                isPlaying = currentPlayingTrack?.id == track.id,
                                onClick = { viewModel.selectAndPlayTrack(track) }
                            )
                        }
                    }
                }
            }
        }

        // Recommended tracks list
        if (displayTracks.isNotEmpty()) {
            item {
                Text(
                    text = "Recommended for You",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
                )
            }

            items(displayTracks) { track ->
                TrackRowItem(
                    track = track,
                    isActive = currentPlayingTrack?.id == track.id,
                    onClick = { viewModel.selectAndPlayTrack(track) },
                    onLikeToggle = { viewModel.toggleLike(track) },
                    onDownloadToggle = {
                        if (track.isDownloaded) {
                            viewModel.removeDownload(track)
                        } else {
                            viewModel.downloadTrack(track)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
            .testTag("playlist_card_${playlist.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal)
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(playlist.coverUrl),
                contentDescription = playlist.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = playlist.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = playlist.description,
                    color = MutedGray,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TrackCoverCard(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable { onClick() }
            .testTag("track_cover_${track.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal)
    ) {
        Column {
            Box {
                Image(
                    painter = rememberAsyncImagePainter(track.coverUrl),
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )
                
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Playing",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = track.title,
                    color = if (isPlaying) SpotifyGreen else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    color = MutedGray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TrackRowItem(
    track: Track,
    isActive: Boolean,
    onClick: () -> Unit,
    onLikeToggle: () -> Unit,
    onDownloadToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("track_row_${track.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(track.coverUrl),
            contentDescription = track.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isActive) SpotifyGreen else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (track.isDownloaded) {
                    Icon(
                        imageVector = Icons.Filled.OfflinePin,
                        contentDescription = "Offline downloaded",
                        tint = SpotifyGreen,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(end = 4.dp)
                    )
                }
                Text(
                    text = track.artist,
                    color = MutedGray,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(onClick = onLikeToggle) {
            Icon(
                imageVector = if (track.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like Song",
                tint = if (track.isLiked) SpotifyGreen else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(onClick = onDownloadToggle) {
            Icon(
                imageVector = if (track.isDownloaded) Icons.Filled.OfflinePin else Icons.Filled.ArrowCircleDown,
                contentDescription = "Download Song",
                tint = if (track.isDownloaded) SpotifyGreen else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
