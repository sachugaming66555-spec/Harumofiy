package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.data.Playlist
import com.example.data.Track
import com.example.ui.MusicViewModel
import com.example.ui.theme.*

@Composable
fun PlaylistScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val allPlaylists by viewModel.allPlaylists.collectAsState()
    val allTracks by viewModel.allTracks.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
    val playlistTracks by viewModel.playlistTracks.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    // If viewing a playlist detail
    if (selectedPlaylist != null) {
        PlaylistDetailScreen(
            playlist = selectedPlaylist!!,
            tracks = playlistTracks,
            allTracks = allTracks,
            currentTrack = currentTrack,
            onBack = { viewModel.setScreen("playlists") },
            onPlayTrack = { viewModel.selectAndPlayTrack(it) },
            onRemoveTrack = { viewModel.removeTrackFromPlaylist(selectedPlaylist!!, it) },
            onAddTrack = { viewModel.addTrackToPlaylist(selectedPlaylist!!, it) },
            onDeletePlaylist = {
                viewModel.deletePlaylist(selectedPlaylist!!)
            }
        )
        return
    }

    // Otherwise show search and playlist grids
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Screen Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Search & Playlists",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            // Button to create a custom playlist
            IconButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightCharcoal)
                    .testTag("create_playlist_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Create Playlist",
                    tint = SpotifyGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic Search Text Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("What do you want to listen to?", color = MutedGray) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_bar"),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MutedGray) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LightCharcoal,
                unfocusedContainerColor = LightCharcoal,
                focusedBorderColor = SpotifyGreen,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // If searching, display search results
        if (searchQuery.isNotEmpty()) {
            val filteredTracks = allTracks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.genre.contains(searchQuery, ignoreCase = true) ||
                        it.album.contains(searchQuery, ignoreCase = true)
            }

            Text(
                text = "Search Results",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (filteredTracks.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No songs or genres matched your search.", color = MutedGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(filteredTracks) { track ->
                        SearchTrackResultItem(
                            track = track,
                            isPlaying = currentTrack?.id == track.id,
                            onClick = { viewModel.selectAndPlayTrack(track) },
                            playlists = allPlaylists.filter { it.isCustom },
                            onAddToPlaylist = { playlist ->
                                viewModel.addTrackToPlaylist(playlist, track)
                            }
                        )
                    }
                }
            }
        } else {
            // Otherwise display standard playlist grid
            Text(
                text = "Your Playlists",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(allPlaylists) { playlist ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable { viewModel.viewPlaylist(playlist) }
                            .testTag("playlist_grid_item_${playlist.id}"),
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
                                    .height(130.dp)
                            )
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = playlist.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (playlist.isCustom) "Custom Playlist" else "Curated by Harmonify",
                                    color = if (playlist.isCustom) SpotifyGreen else MutedGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Playlist Dialog
    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, desc ->
                viewModel.createPlaylist(name, desc)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    tracks: List<Track>,
    allTracks: List<Track>,
    currentTrack: Track?,
    onBack: () -> Unit,
    onPlayTrack: (Track) -> Unit,
    onRemoveTrack: (Track) -> Unit,
    onAddTrack: (Track) -> Unit,
    onDeletePlaylist: () -> Unit
) {
    val recommendedAdditions = allTracks.filter { track ->
        tracks.none { it.id == track.id }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Hero Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                LightCharcoal,
                                DeepBlack
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Back button & Delete button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }

                        if (playlist.isCustom) {
                            IconButton(onClick = onDeletePlaylist) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Playlist", tint = Color.Red)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(playlist.coverUrl),
                            contentDescription = playlist.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "PLAYLIST",
                                color = SpotifyGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = playlist.name,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = playlist.description,
                                color = MutedGray,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Action controls (Shuffle play)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${tracks.size} tracks",
                    color = MutedGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                if (tracks.isNotEmpty()) {
                    Button(
                        onClick = { onPlayTrack(tracks.random()) },
                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = DeepBlack)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Shuffle Play", color = DeepBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Tracks inside the playlist
        if (tracks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MutedGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "This playlist is currently empty.",
                            color = MutedGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(tracks) { track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayTrack(track) }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
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
                            color = if (currentTrack?.id == track.id) SpotifyGreen else Color.White,
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

                    IconButton(onClick = { onRemoveTrack(track) }) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircleOutline,
                            contentDescription = "Remove from Playlist",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Recommended Tracks to Add section
        if (recommendedAdditions.isNotEmpty()) {
            item {
                Text(
                    text = "Recommended Additions",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)
                )
            }

            items(recommendedAdditions.take(5)) { track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(track.coverUrl),
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = Color.White,
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

                    IconButton(onClick = { onAddTrack(track) }) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Add Track",
                            tint = SpotifyGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Create Playlist",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist Name", color = MutedGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_playlist_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = MutedGray
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = MutedGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_playlist_desc"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = MutedGray
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onCreate(name, description)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Create", color = DeepBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchTrackResultItem(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit,
    playlists: List<Playlist>,
    onAddToPlaylist: (Playlist) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
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
                color = if (isPlaying) SpotifyGreen else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${track.artist} • ${track.genre}",
                color = MutedGray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Add song option", tint = Color.White)
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(DarkCharcoal)
            ) {
                if (playlists.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No custom playlists", color = MutedGray) },
                        onClick = { showMenu = false }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text("Add to Playlist:", color = SpotifyGreen, fontWeight = FontWeight.Bold) },
                        onClick = {},
                        enabled = false
                    )
                    playlists.forEach { pl ->
                        DropdownMenuItem(
                            text = { Text(pl.name, color = Color.White) },
                            onClick = {
                                onAddToPlaylist(pl)
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}
