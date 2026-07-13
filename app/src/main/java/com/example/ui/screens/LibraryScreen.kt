package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.Track
import com.example.ui.MusicViewModel
import com.example.ui.theme.*

@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val likedTracks by viewModel.likedTracks.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val isOfflineMode by viewModel.offlineOnlyMode.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Liked, 1: Downloads
    val tabs = listOf("Liked Tracks", "Offline Downloads")

    val simulatedSizeMb = downloadedTracks.sumOf { (it.duration * 0.024).toInt() } // approx 1.4MB per minute of MP3

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Title
            Text(
                text = "Your Library",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
            )

            // Custom sliding Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DeepBlack,
                contentColor = SpotifyGreen,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (selectedTab == index) SpotifyGreen else MutedGray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Content depending on selected tab
            when (selectedTab) {
                0 -> { // Liked Tracks
                    if (likedTracks.isEmpty()) {
                        EmptyLibraryState(
                            icon = Icons.Default.FavoriteBorder,
                            title = "No Liked Songs Yet",
                            description = "Songs you tap the heart on will appear here in your personalized collection."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${likedTracks.size} items",
                                        color = MutedGray,
                                        fontSize = 13.sp
                                    )

                                    Button(
                                        onClick = { viewModel.selectAndPlayTrack(likedTracks.random()) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play Mix", tint = DeepBlack, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Play Mix", color = DeepBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }

                            items(likedTracks) { track ->
                                LibraryTrackRow(
                                    track = track,
                                    isPlaying = currentTrack?.id == track.id,
                                    onClick = { viewModel.selectAndPlayTrack(track) },
                                    actionIcon = Icons.Default.Favorite,
                                    onActionClick = { viewModel.toggleLike(track) },
                                    actionTint = SpotifyGreen
                                )
                            }
                        }
                    }
                }
                1 -> { // Offline Downloads
                    if (downloadedTracks.isEmpty()) {
                        EmptyLibraryState(
                            icon = Icons.Default.DownloadForOffline,
                            title = "No Offline Downloads",
                            description = "Tap the download option inside song player to listen entirely offline."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Storage and offline switch row
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkCharcoal)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                "Offline Storage",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                "Using ~$simulatedSizeMb.0 MB on device",
                                                color = MutedGray,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "Offline Play",
                                                color = if (isOfflineMode) SpotifyGreen else MutedGray,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Switch(
                                                checked = isOfflineMode,
                                                onCheckedChange = { viewModel.toggleOfflineOnlyMode() },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = DeepBlack,
                                                    checkedTrackColor = SpotifyGreen,
                                                    uncheckedThumbColor = MutedGray,
                                                    uncheckedTrackColor = LightCharcoal
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            items(downloadedTracks) { track ->
                                LibraryTrackRow(
                                    track = track,
                                    isPlaying = currentTrack?.id == track.id,
                                    onClick = { viewModel.selectAndPlayTrack(track) },
                                    actionIcon = Icons.Default.DeleteOutline,
                                    onActionClick = { viewModel.removeDownload(track) },
                                    actionTint = Color.Red.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryTrackRow(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onActionClick: () -> Unit,
    actionTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkCharcoal)
            .clickable { onClick() }
            .padding(10.dp)
            .testTag("library_row_${track.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(track.coverUrl),
            contentDescription = track.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isPlaying) SpotifyGreen else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                color = MutedGray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onActionClick) {
            Icon(
                imageVector = actionIcon,
                contentDescription = "Action",
                tint = actionTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EmptyLibraryState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MutedGray,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = MutedGray,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
