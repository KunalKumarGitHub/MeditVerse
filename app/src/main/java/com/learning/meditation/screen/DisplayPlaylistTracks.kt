package com.learning.meditation.screen

import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.learning.meditation.R
import com.learning.meditation.ui.theme.TextWhite
import com.learning.meditation.ui.theme.deepBlue
import com.learning.meditation.viewmodel.ConnectivityViewModel
import com.learning.meditation.viewmodel.PlaylistViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayPlaylistTracks(
    playlistId: String,
    navController: NavHostController,
    playlistName: String,
    viewModel: PlaylistViewModel,
    connectivityViewModel:ConnectivityViewModel
){
    val isLoading by viewModel.loading.collectAsState()
    val snackbarHostState = remember{ SnackbarHostState() }
    val tracks by viewModel.selectedPlaylist.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId= currentUser?.uid

    LaunchedEffect(userId,playlistId) {
        userId?.let { viewModel.fetchPlaylistTracksIfNeeded(userId,playlistId) }
    }

    Box(modifier = Modifier.fillMaxSize()){
        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back",
                            tint = Color.White,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable {
                                    navController.popBackStack(route = "music",inclusive = false)
                                },
                        )
                    },
                    title = { Text(playlistName,color= TextWhite) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            if(!isLoading){
                if(tracks.isEmpty()){
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Playlist is empty!!", fontFamily = FontFamily.Default,color = Color.White)
                    }
                }else {
                    LazyColumn(
                        contentPadding = paddingValues
                    ) {
                        items(tracks.size) { index ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                                    .clickable {
                                        val musicJson = Uri.encode(Json.encodeToString(tracks[index]))
                                        navController.navigate("meditationDetail/$musicJson") {
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                colors = CardDefaults.cardColors(deepBlue),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            model = tracks[index].imageUrl,
                                            error = painterResource(id = R.drawable.default_music_image)
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = tracks[index].title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White,
                                            fontFamily = FontFamily.Default,
                                            modifier = Modifier.align(Alignment.CenterStart)
                                        )
                                    }
                                }
                            }
                            Divider(
                                color = Color.White,
                                thickness = 1.dp,
                                modifier = Modifier.padding(
                                    top = 2.dp,
                                    bottom = 2.dp,
                                    start = 8.dp,
                                    end = 8.dp
                                )
                            )
                        }
                    }
                }
            }
            else{
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

    }


}