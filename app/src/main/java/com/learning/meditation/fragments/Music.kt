package com.learning.meditation.fragments

import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.learning.meditation.R
import com.learning.meditation.dataclass.Playlist
import com.learning.meditation.repository.PlaylistRepository
import com.learning.meditation.screen.DisplayPlaylistTracks
import com.learning.meditation.screen.MeditationDetailScreen
import com.learning.meditation.ui.theme.ButtonTeal
import com.learning.meditation.ui.theme.TextWhite
import com.learning.meditation.viewmodel.ConnectivityViewModel
import com.learning.meditation.viewmodel.PlaylistViewModel
import com.learning.meditation.viewmodelfactory.PlaylistViewModelFactory
import kotlinx.coroutines.launch
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Music(
    navController: NavHostController,
    viewModel: PlaylistViewModel,
    connectivityViewModel: ConnectivityViewModel
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid
    LaunchedEffect(userId) {
        userId?.let { viewModel.fetchPlaylistsIfNeeded(it) }
    }

    val context = LocalContext.current
    val isConnected by connectivityViewModel.isConnected.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }

    fun createNewPlaylist() {
        if (newPlaylistName.isNotBlank() && userId != null) {
            val newPlaylist = Playlist(
                playlistId = UUID.randomUUID().toString(),
                playlistName = newPlaylistName.trim(),
                trackCount = 0
            )
            viewModel.addPlaylist(userId, newPlaylist, {
                showDialog = false
            }, { exception ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Failed to create playlist: ${exception.message}")
                }
            })
        }
        showDialog = false
    }

    Box(modifier = Modifier.fillMaxSize()){
        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
            topBar = {
                TopAppBar(
                    title = { Text("My Playlists", fontFamily = FontFamily.Default,color = TextWhite) },
                    actions = {
                        IconButton(onClick = {
                            newPlaylistName=""
                            showDialog = true
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Create Playlist", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            if(!isLoading){
                if(isConnected){
                    LazyColumn(contentPadding = paddingValues) {
                        items(playlists.size) { index ->
                            val playlist=playlists[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                                    .clickable {
                                        navController.navigate("playlistDetails/${playlist.playlistId}/${playlist.playlistName}")
                                    },
                                colors = CardDefaults.cardColors(ButtonTeal),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Row(modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${playlist.playlistName}  (${playlist.trackCount})",
                                        color= TextWhite,
                                        fontFamily = FontFamily.Default,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        playlistToDelete=playlist
                                        deleteDialog=true
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete",tint= Color.Red)
                                    }
                                }
                            }
                            Divider(color = Color.White, thickness = 1.dp, modifier = Modifier.padding(top=2.dp,bottom=2.dp,start=8.dp,end=8.dp))
                        }
                    }
                }else{
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
            }else{
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


    if(deleteDialog && playlistToDelete!=null){
        AlertDialog(
            containerColor = ButtonTeal,
            icon= {
                Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Warning",
                tint = Color.Red)
            },
            onDismissRequest = { deleteDialog = false },
            text = {
                Column {
                    Text("Are you confirm to delete the playlist?", fontFamily = FontFamily.Default,color = Color.White)
                }
            },
            confirmButton = {
                TextButton(onClick = { userId?.let {
                    if(isConnected) {
                        viewModel.deletePlaylist(it, playlistToDelete!!,
                            onComplete = {
                                Toast.makeText(
                                    context,
                                    "${playlistToDelete!!.playlistName} deleted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                deleteDialog = false
                            },
                            onFailure = {
                                Toast.makeText(
                                    context,
                                    "Failed to delete ${playlistToDelete!!.playlistName}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                deleteDialog = false
                            })
                    }else{
                        Toast.makeText(context,"You are offline",Toast.LENGTH_SHORT).show()
                    }
                } }) {
                    Text("Yes", fontFamily = FontFamily.Default,color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialog = false }) {
                    Text("No", fontFamily = FontFamily.Default,color = Color.White)
                }
            }
        )
    }

    if (showDialog) {
            AlertDialog(
                containerColor = ButtonTeal,
                onDismissRequest = { showDialog = false },
                text = {
                    Column {
                        Text("Enter the name of the new playlist", fontFamily = FontFamily.Default,color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPlaylistName,
                            onValueChange = {
                                newPlaylistName = it
                            },
                            label = { Text("Playlist Name",fontFamily = FontFamily.Default) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TextStyle(color=Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = Color.White,
                                focusedBorderColor = Color.White,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if(isConnected) {
                            createNewPlaylist()
                        }else{
                            Toast.makeText(context,"You are offline",Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Create", fontFamily = FontFamily.Default,color = Color.Green)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel", fontFamily = FontFamily.Default,color = Color.White)
                    }
                }
            )
    }
}


@Composable
fun MusicNavigationFun(
    homeNavController: NavHostController,
    connectivityViewModel: ConnectivityViewModel
){
    val repositoryMusic = remember { PlaylistRepository() }
    val viewModelMusic: PlaylistViewModel = viewModel(
        factory = PlaylistViewModelFactory(repositoryMusic)
    )
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "music") {
        composable("music") {
            Music(navController = navController, viewModel =viewModelMusic,connectivityViewModel)
        }
        composable("playlistDetails/{playlistId}/{playlistName}") { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
            val playlistName = backStackEntry.arguments?.getString("playlistName") ?: ""
            DisplayPlaylistTracks(playlistId = playlistId, playlistName=playlistName,navController = navController,viewModel=viewModelMusic,connectivityViewModel = connectivityViewModel
            )
        }
        composable(route="meditationDetail/{trackJson}",
            arguments = listOf(navArgument("trackJson") { type = NavType.StringType })) { backStackEntry ->
            val trackJson = backStackEntry.arguments?.getString("trackJson") ?: ""
            MeditationDetailScreen(
                trackJson = trackJson,
                navController = navController,
                connectivityViewModel = connectivityViewModel
            )
        }
    }
}