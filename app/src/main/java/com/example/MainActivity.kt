package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Track
import com.example.ui.MusicViewModel
import com.example.ui.components.FullScreenPlayer
import com.example.ui.components.MiniPlayer
import com.example.ui.components.ShareCardDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlaylistScreen
import com.example.ui.screens.SocialScreen
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.MutedGray
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpotifyGreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    val musicViewModel: MusicViewModel = viewModel()
    val currentScreen by musicViewModel.currentScreen.collectAsState()
    val currentTrack by musicViewModel.currentTrack.collectAsState()

    var isPlayerExpanded by remember { mutableStateOf(false) }
    var activeShareTrack by remember { mutableStateOf<Track?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBlack)
                    .navigationBarsPadding()
            ) {
                // If a song is currently selected/loaded, overlay the MiniPlayer just above bottom tabs
                if (currentTrack != null) {
                    MiniPlayer(
                        viewModel = musicViewModel,
                        onExpand = { isPlayerExpanded = true }
                    )
                }

                // Standard Material 3 Bottom Navigation Bar
                NavigationBar(
                    containerColor = DeepBlack,
                    tonalElevation = 8.dp,
                    modifier = Modifier.height(72.dp)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "home" || currentScreen == "home_detail",
                        onClick = { musicViewModel.setScreen("home") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepBlack,
                            selectedTextColor = SpotifyGreen,
                            indicatorColor = SpotifyGreen,
                            unselectedIconColor = Color.White,
                            unselectedTextColor = MutedGray
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen == "playlists" || currentScreen == "playlist_detail",
                        onClick = { musicViewModel.setScreen("playlists") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        label = { Text("Search") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepBlack,
                            selectedTextColor = SpotifyGreen,
                            indicatorColor = SpotifyGreen,
                            unselectedIconColor = Color.White,
                            unselectedTextColor = MutedGray
                        ),
                        modifier = Modifier.testTag("nav_search")
                    )

                    NavigationBarItem(
                        selected = currentScreen == "social",
                        onClick = { musicViewModel.setScreen("social") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Social"
                            )
                        },
                        label = { Text("Social") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepBlack,
                            selectedTextColor = SpotifyGreen,
                            indicatorColor = SpotifyGreen,
                            unselectedIconColor = Color.White,
                            unselectedTextColor = MutedGray
                        ),
                        modifier = Modifier.testTag("nav_social")
                    )

                    NavigationBarItem(
                        selected = currentScreen == "library",
                        onClick = { musicViewModel.setScreen("library") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = "Library"
                            )
                        },
                        label = { Text("Library") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepBlack,
                            selectedTextColor = SpotifyGreen,
                            indicatorColor = SpotifyGreen,
                            unselectedIconColor = Color.White,
                            unselectedTextColor = MutedGray
                        ),
                        modifier = Modifier.testTag("nav_library")
                    )
                }
            }
        },
        containerColor = DeepBlack
    ) { innerPadding ->
        // Render screens inside a standard box that respects upper/notch boundaries
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (currentScreen) {
                "home" -> HomeScreen(
                    viewModel = musicViewModel,
                    modifier = Modifier.fillMaxSize()
                )
                "playlists", "playlist_detail" -> PlaylistScreen(
                    viewModel = musicViewModel,
                    modifier = Modifier.fillMaxSize()
                )
                "social" -> SocialScreen(
                    viewModel = musicViewModel,
                    modifier = Modifier.fillMaxSize()
                )
                "library" -> LibraryScreen(
                    viewModel = musicViewModel,
                    modifier = Modifier.fillMaxSize()
                )
                else -> HomeScreen(
                    viewModel = musicViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Slide-up animation overlay for the full screen player
    AnimatedVisibility(
        visible = isPlayerExpanded,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
        ) + fadeOut()
    ) {
        FullScreenPlayer(
            viewModel = musicViewModel,
            onCollapse = { isPlayerExpanded = false },
            onShowShareDialog = { track ->
                activeShareTrack = track
            }
        )
    }

    // Standard in-app share sheet launcher
    if (activeShareTrack != null) {
        ShareCardDialog(
            track = activeShareTrack!!,
            onDismiss = { activeShareTrack = null }
        )
    }
}
