package com.learning.meditation.fragments

import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.learning.meditation.R
import com.learning.meditation.dataclass.MeditationTrack
import com.learning.meditation.repository.MeditationRepository
import com.learning.meditation.screen.MeditationDetailScreen
import com.learning.meditation.ui.theme.TextWhite
import com.learning.meditation.viewmodel.ConnectivityViewModel
import com.learning.meditation.viewmodel.MeditationViewModel
import com.learning.meditation.viewmodelfactory.MeditationViewModelFactory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun Meditate(
    navController: NavHostController,
    viewModel: MeditationViewModel,
    connectivityViewModel: ConnectivityViewModel
) {

    LaunchedEffect(Unit) {
        viewModel.fetchMeditationTracksIfNeeded()
    }

    val isConnected by connectivityViewModel.isConnected.collectAsState()
    var lastClickTime by remember { mutableStateOf(0L) }
    val meditationTracks by viewModel.meditationTracks.collectAsState()
    val isLoading by viewModel.loading.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "Meditation Music",
            fontFamily = FontFamily.Default,
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if(!isLoading) {
            if(isConnected){
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(meditationTracks.size) { index ->
                        MeditationMusicCard(meditationTracks[index],
                            navController,
                            lastClickTime = lastClickTime,
                            onClickUpdateLastTime = { time -> lastClickTime = time },
                            connectivityViewModel)
                    }
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


@Composable
fun MeditationMusicCard(
    music: MeditationTrack,
    navController: NavHostController,
    lastClickTime: Long,
    onClickUpdateLastTime: (Long) -> Unit,
    connectivityViewModel: ConnectivityViewModel
) {
    val isConnected by connectivityViewModel.isConnected.collectAsState()
    val context= LocalContext.current
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                if (isConnected) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastClickTime > 1000) {

                        val musicJson = Uri.encode(Json.encodeToString(music))
                        navController.navigate("meditationDetail/$musicJson") {
                            launchSingleTop = true
                            restoreState = true
                        }
                        onClickUpdateLastTime(currentTime)

                        onClickUpdateLastTime(currentTime)
                    }
                }else {
                    Toast.makeText(context, "You are currently offline", Toast.LENGTH_SHORT).show()
                }
            }
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(music.imageUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.default_music_image)
                        .error(R.drawable.default_music_image)
                        .size(Size.ORIGINAL)
                        .build(),
                    contentScale = ContentScale.Crop
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        buildAnnotatedString {
            Text(
                text = music.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = music.duration,
                style = MaterialTheme.typography.bodySmall,
                color = TextWhite
            )
        }
    }
}


@Composable
fun MeditationNavigationFun(
    homeNavController: NavHostController,
    connectivityViewModel: ConnectivityViewModel
){
    val repositoryMeditate = remember { MeditationRepository() }
    val viewModel: MeditationViewModel = viewModel(
        factory = MeditationViewModelFactory(repositoryMeditate)
    )
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "meditate") {
        composable("meditate") {
            Meditate(navController = navController, viewModel = viewModel,connectivityViewModel)
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
