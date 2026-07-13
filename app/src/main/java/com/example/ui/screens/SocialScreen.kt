package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.FriendActivity
import com.example.data.Track
import com.example.ui.MusicViewModel
import com.example.ui.components.ShareCardDialog
import com.example.ui.theme.*

@Composable
fun SocialScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val friends by viewModel.friendActivities.collectAsState()
    val allTracks by viewModel.allTracks.collectAsState()
    val currentPlayingTrack by viewModel.currentTrack.collectAsState()

    var activeShareTrack by remember { mutableStateOf<Track?>(null) }
    var snackbarMessage by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage.isNotEmpty()) {
            showSnackbar = true
            kotlinx.coroutines.delay(3000)
            showSnackbar = false
            snackbarMessage = ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp, top = 12.dp)
        ) {
            // Header Title
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Social Listening",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "See what your friends are jamming to in real time.",
                        color = MutedGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Friend Activity Horizontal list or vertical feeds
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Friend Activity",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Green live pulsing dot
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Live", color = SpotifyGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (friends.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No friends active.", color = MutedGray)
                    }
                }
            } else {
                items(friends) { friend ->
                    FriendActivityItem(
                        friend = friend,
                        onListenAlong = {
                            // Find the matching track in our local seeded songs to play it
                            val match = allTracks.find {
                                it.title.equals(friend.currentTrackTitle, ignoreCase = true)
                            }
                            if (match != null) {
                                viewModel.selectAndPlayTrack(match)
                                snackbarMessage = "Now listening along with ${friend.name}!"
                            } else {
                                // Default to a random song if metadata didn't match perfectly
                                if (allTracks.isNotEmpty()) {
                                    viewModel.selectAndPlayTrack(allTracks.random())
                                    snackbarMessage = "Now listening along with ${friend.name}!"
                                }
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = LightCharcoal, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Dedicated Share Card Generator Section
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Music Share Generator",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Generate a customized visual sharing card for any track in your library.",
                        color = MutedGray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (allTracks.isNotEmpty()) {
                        Text(
                            text = "Select a song to share:",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(allTracks) { track ->
                                Card(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .clickable { activeShareTrack = track }
                                        .testTag("social_share_track_${track.id}"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (activeShareTrack?.id == track.id) SpotifyGreen.copy(alpha = 0.2f) else DarkCharcoal
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(track.coverUrl),
                                            contentDescription = track.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(70.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = track.title,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = track.artist,
                                            color = MutedGray,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Send to Friend Inbox Simulation
                    if (activeShareTrack != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCharcoal)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = rememberAsyncImagePainter(activeShareTrack!!.coverUrl),
                                        contentDescription = "Cover",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            activeShareTrack!!.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "by ${activeShareTrack!!.artist}",
                                            color = MutedGray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    "Direct Send to Active Friend Inbox:",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    friends.forEach { f ->
                                        Button(
                                            onClick = {
                                                snackbarMessage = "Successfully shared '${activeShareTrack!!.title}' directly with ${f.name}!"
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = LightCharcoal),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            Text(f.name.split(" ")[0], fontSize = 11.sp, color = SpotifyGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Custom Snackbar style alert for social activities
        AnimatedVisibility(
            visible = showSnackbar,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 96.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SpotifyGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share notification",
                        tint = DeepBlack,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = snackbarMessage,
                        color = DeepBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // Modal dialogue popup when we select a track to customize and share
    if (activeShareTrack != null && !showSnackbar) {
        ShareCardDialog(
            track = activeShareTrack!!,
            onDismiss = { activeShareTrack = null }
        )
    }
}

@Composable
fun FriendActivityItem(
    friend: FriendActivity,
    onListenAlong: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("friend_item_${friend.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Image(
                    painter = rememberAsyncImagePainter(friend.avatarUrl),
                    contentDescription = friend.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )

                if (friend.isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(DeepBlack)
                            .padding(1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(SpotifyGreen)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Listening to",
                        tint = SpotifyGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${friend.currentTrackTitle} • ${friend.currentTrackArtist}",
                        color = MutedGray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Button(
                onClick = onListenAlong,
                colors = ButtonDefaults.buttonColors(containerColor = if (friend.isPlaying) SpotifyGreen else LightCharcoal),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Listen Along",
                    color = if (friend.isPlaying) DeepBlack else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
