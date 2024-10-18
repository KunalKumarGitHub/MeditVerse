package com.learning.meditation.screen

import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.firebase.auth.FirebaseAuth
import com.learning.meditation.R
import com.learning.meditation.dataclass.SleepTrack
import com.learning.meditation.repository.PlaylistRepository
import com.learning.meditation.ui.theme.ButtonTeal
import com.learning.meditation.ui.theme.TextWhite
import com.learning.meditation.viewmodel.ConnectivityViewModel
import com.learning.meditation.viewmodel.PlaylistViewModel
import com.learning.meditation.viewmodelfactory.PlaylistViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
fun SleepDetailScreen(
    trackJson: String,
    navController: NavHostController,
    connectivityViewModel: ConnectivityViewModel
) {
    val isConnected by connectivityViewModel.isConnected.collectAsState()
    val userId= FirebaseAuth.getInstance().currentUser?.uid
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val repositoryPlaylist = remember { PlaylistRepository() }
    val viewModelPlaylist: PlaylistViewModel = viewModel(
        factory = PlaylistViewModelFactory(repositoryPlaylist)
    )

    val track = Json.decodeFromString<SleepTrack>(trackJson)

    val context = LocalContext.current
    var player: SimpleExoPlayer? by remember { mutableStateOf(null) }
    var isPlaying by remember { mutableStateOf(false) }
    val playlists by viewModelPlaylist.playlists.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedPlaylistName by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0L) }
    var currentPosition by remember { mutableStateOf(0L) }
    val rotation = remember { Animatable(0f) }

    var canNavigateBack by remember { mutableStateOf(true) }
    DisposableEffect(navController) {
        onDispose {
            canNavigateBack = true
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 9000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rotation.snapTo(0f)
        }
    }

    LaunchedEffect(showDialog,userId) {
        if (showDialog) {
            userId?.let { viewModelPlaylist.fetchPlaylistsIfNeeded(it) }
        }
    }

    LaunchedEffect(track) {
        player = SimpleExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(track.audioUrl))
            prepare()
        }
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    duration = player?.duration ?: 0L
                } else if (state == Player.STATE_ENDED) {
                    player?.seekTo(0)
                    currentPosition = 0L
                    progress = 0f
                    isPlaying = false
                    player?.playWhenReady=false
                }
            }
        })
    }

    LaunchedEffect(player, isPlaying) {
        while (isPlaying && player != null) {
            currentPosition = player!!.currentPosition
            progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
            delay(1000L)
        }
    }

    DisposableEffect(track) {
        onDispose {
            player?.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ){
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "back",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        if (canNavigateBack) {
                            canNavigateBack = false
                            navController.popBackStack()
                        }
                    },
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = track.title,
                fontFamily = FontFamily.Default,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        if(isConnected){
            Image(
                painter = rememberAsyncImagePainter(track.imageUrl),
                contentDescription = "Sleep Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .graphicsLayer {
                        rotationZ = rotation.value
                    },
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Slider(
                value = progress,
                onValueChange = { value ->
                    val seekPosition = (duration * value).toLong()
                    player?.seekTo(seekPosition)
                    progress = value
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Gray
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formattime(currentPosition), fontFamily = FontFamily.Default,color = Color.White)
                Text(track.duration, fontFamily = FontFamily.Default,color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = {
                    player?.seekTo(0)
                    player?.playWhenReady = true
                    isPlaying = true
                },
                    modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White,modifier = Modifier.size(48.dp))
                }

                if (isPlaying) {
                    IconButton(onClick = {
                        player?.playWhenReady = false
                        isPlaying = false
                    },
                        modifier = Modifier.size(64.dp)) {
                        Icon(painter = painterResource(id = R.drawable.pause), contentDescription = "Pause",tint= Color.White,modifier = Modifier.size(48.dp))
                    }
                } else {
                    IconButton(onClick = {
                        player?.playWhenReady = true
                        isPlaying = true
                    },
                        modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White,modifier = Modifier.size(48.dp))
                    }
                }
            }

            Button(onClick = {
                showDialog = true
            },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonTeal)
            ) {
                Text("Add to Playlist", fontFamily = FontFamily.Default,color = TextWhite)
            }
        }else{
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        ImageView(context).apply {
                            setImageResource(R.drawable.animated_logo)
                            (drawable as? AnimatedVectorDrawable)?.start()
                        }
                    }
                )
            }
        }
    }


    if (showDialog) {
        val context1= LocalContext.current
        AlertDialog(
            containerColor = ButtonTeal,
            onDismissRequest = { showDialog = false },
            title = { Text("Select Playlist",fontFamily = FontFamily.Default,color= Color.White) },
            text = {
                Column {
                    if (playlists.isEmpty()) {
                        Text("No playlists available",fontFamily = FontFamily.Default)
                    } else {
                        LazyColumn {
                            items(playlists.size) { index ->
                                Text(
                                    color= Color.White,
                                    text = playlists[index].playlistName,
                                    fontFamily = FontFamily.Default,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedPlaylistName = playlists[index].playlistName
                                            if (userId != null) {
                                                viewModelPlaylist.addSleepTrackToPlaylist(
                                                    userId,
                                                    playlists[index],
                                                    track,
                                                    context1
                                                ) { exception ->
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar("Failed to add track: ${exception.message}")
                                                    }
                                                }
                                            }
                                            showDialog = false
                                        }
                                        .padding(8.dp)
                                )
                                Divider(color= Color.White,modifier=Modifier.padding(start=8.dp,end=8.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", fontFamily = FontFamily.Default,color = Color.White)
                }
            }
        )
    }
}

fun formattime(ms: Long): String {
    val minutes = (ms / 1000) / 60
    val seconds = (ms / 1000) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
