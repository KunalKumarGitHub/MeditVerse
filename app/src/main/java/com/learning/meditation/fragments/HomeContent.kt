package com.learning.meditation.fragments

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.google.firebase.auth.FirebaseAuth
import com.learning.meditation.R
import com.learning.meditation.constants.RetrofitClient
import com.learning.meditation.constants.getChipsData
import com.learning.meditation.dataclass.Feature
import com.learning.meditation.dataclass.Quote
import com.learning.meditation.repository.MeditationRepository
import com.learning.meditation.repository.RoutineRepository
import com.learning.meditation.repository.SleepRepository
import com.learning.meditation.screen.ChipDetailScreen
import com.learning.meditation.screen.MeditationDetailScreen
import com.learning.meditation.screen.RoutinePlannerScreen
import com.learning.meditation.screen.SleepDetailScreen
import com.learning.meditation.ui.theme.Beige1
import com.learning.meditation.ui.theme.Beige2
import com.learning.meditation.ui.theme.Beige3
import com.learning.meditation.ui.theme.BlueViolet1
import com.learning.meditation.ui.theme.BlueViolet2
import com.learning.meditation.ui.theme.BlueViolet3
import com.learning.meditation.ui.theme.ButtonTeal
import com.learning.meditation.ui.theme.LightGreen1
import com.learning.meditation.ui.theme.LightGreen2
import com.learning.meditation.ui.theme.LightGreen3
import com.learning.meditation.ui.theme.TextWhite
import com.learning.meditation.ui.theme.darkerTeal
import com.learning.meditation.utils.standardQuadFromTo
import com.learning.meditation.viewmodel.ConnectivityViewModel
import com.learning.meditation.viewmodel.MeditationViewModel
import com.learning.meditation.viewmodel.RoutineViewModel
import com.learning.meditation.viewmodel.SleepViewModel
import com.learning.meditation.viewmodelfactory.MeditationViewModelFactory
import com.learning.meditation.viewmodelfactory.RoutineViewModelFactory
import com.learning.meditation.viewmodelfactory.SleepViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

@Composable
fun GreetingSection(
    homeNavController: NavHostController
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val profileImageUrl by remember { mutableStateOf(currentUser?.photoUrl?.toString() ?: "") }
    val userName = currentUser?.displayName.toString()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome, $userName",
                fontFamily = FontFamily.Default,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "We wish you have a good day!",
                fontFamily = FontFamily.Default,
                color = TextWhite,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        if (profileImageUrl.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profileImageUrl)
                        .crossfade(true)
                        .build()
                ),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable {
                        homeNavController.navigate("profile") {
                            popUpTo(homeNavController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(R.drawable.profile_pic),
                contentDescription = "Default Profile Image",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable {
                        homeNavController.navigate("profile") {
                            popUpTo(homeNavController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun ChipSection(
    chips: String,
    index:Int,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(start = 15.dp, top = 15.dp, bottom = 15.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                onClick()
            }
            .background(darkerTeal)
            .padding(15.dp)
    ) {
        Text(text = chips, fontFamily = FontFamily.Default, color = TextWhite, textAlign = TextAlign.Center)
    }
}

@Composable
fun CurrentMeditation(color: Color = Color.Black) {
    var quoteText by remember { mutableStateOf("Loading...") }
    var author by remember { mutableStateOf("") }
    val context = LocalContext.current
    val quotePreferences = remember { QuotePreferences(context) }

    LaunchedEffect(Unit) {
        if (quotePreferences.isQuoteStale()) {
            fetchRandomQuote(
                onSuccess = { quote, quoteAuthor ->
                    quoteText = quote
                    author = quoteAuthor
                    quotePreferences.saveQuote(quote, quoteAuthor)
                },
                onFailure = {
                    quoteText = "Failed to load quote"
                }
            )
        }else{
            quotePreferences.getQuote()?.let { (cachedQuote, cachedAuthor) ->
                quoteText = cachedQuote
                author = cachedAuthor
            }
        }
    }

    Box(
        modifier = Modifier
            .padding(15.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(ButtonTeal)
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ){
            Text(
                text = quoteText,
                color = Color.White,
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (author.isNotEmpty()) {
                    Text(
                        text = "- $author",
                        fontFamily = FontFamily.Default,
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

class QuotePreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("QuotePrefs", Context.MODE_PRIVATE)

    fun saveQuote(quote: String, author: String) {
        sharedPreferences.edit().apply {
            putString("quote", quote)
            putString("author", author)
            putLong("lastFetch", System.currentTimeMillis())
            apply()
        }
    }

    fun getQuote(): Pair<String, String>? {
        val quote = sharedPreferences.getString("quote", null)
        val author = sharedPreferences.getString("author", null)
        return if (quote != null && author != null) {
            Pair(quote, author)
        } else {
            null
        }
    }

    fun isQuoteStale(): Boolean {
        val lastFetch = sharedPreferences.getLong("lastFetch", 0)
        val currentTime = System.currentTimeMillis()

        return currentTime-lastFetch>3600000

    }
}


fun fetchRandomQuote(onSuccess: (String, String) -> Unit, onFailure: () -> Unit) {
    val call = RetrofitClient.instance.getRandomQuote()
    call.enqueue(object: Callback<List<Quote>> {
        override fun onResponse(call: Call<List<Quote>>, response: Response<List<Quote>>) {
            if (response.isSuccessful) {
                val quotes = response.body()
                quotes?.let {
                    val todayQuote = it[0]
                    onSuccess(todayQuote.q, todayQuote.a)
                } ?: run {
                    onFailure()
                }
            } else {
                onFailure()
            }
        }

        override fun onFailure(call: Call<List<Quote>>, t: Throwable) {
            Log.e("API Error", "Failed to fetch quote: ${t.message}")
            onFailure()
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureSection(
    features: List<Feature>,
    navController: NavHostController,
    connectivityViewModel: ConnectivityViewModel
) {
    val context= LocalContext.current
    val isConnected by connectivityViewModel.isConnected.collectAsState()

    val repositoryMeditate = remember { MeditationRepository() }
    val meditationViewModel: MeditationViewModel = viewModel(
        factory = MeditationViewModelFactory(repositoryMeditate)
    )
    val repositorySleep = remember { SleepRepository() }
    val sleepViewModel: SleepViewModel = viewModel(
        factory = SleepViewModelFactory(repositorySleep)
    )
    var isClickInProgress by remember { mutableStateOf(false) }
    if(isClickInProgress){
       AlertDialog(onDismissRequest = { isClickInProgress=false }, modifier = Modifier.size(70.dp)) {
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
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Features",
            fontFamily = FontFamily.Default,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(15.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 7.5.dp, end = 7.5.dp, bottom = 100.dp)
        ) {
            items(features.size) {
                val feature=features[it]
                FeatureItem(feature = feature){
                    if(isConnected){
                        if (!isClickInProgress) {
                            when (feature.title) {
                                "Routines" -> navController.navigate("Routines") {
                                    launchSingleTop = true
                                    restoreState = true
                                }

                                "Random Meditate" -> {
                                    meditationViewModel.fetchRandomMeditationTrack { randomTrack ->
                                        isClickInProgress = true
                                        val musicJson = Uri.encode(Json.encodeToString(randomTrack))
                                        navController.navigate("meditationDetail/$musicJson") {
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }

                                "Random Sleep" -> {
                                    sleepViewModel.fetchRandomSleepTrack { randomTrack ->
                                        isClickInProgress = true
                                        val musicJson = Uri.encode(Json.encodeToString(randomTrack))
                                        navController.navigate("sleepDetail/$musicJson") {
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(1000)
                                isClickInProgress = false
                            }
                        }
                    }
                    else{
                        Toast.makeText(context,"You are currently offline",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureItem(
    feature: Feature, onClick: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .padding(7.5.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(feature.darkColor)
    ) {
        val width = constraints.maxWidth
        val height = constraints.maxHeight

        val mediumColoredPoint1 = Offset(0f, height * 0.3f)
        val mediumColoredPoint2 = Offset(width * 0.1f, height * 0.35f)
        val mediumColoredPoint3 = Offset(width * 0.4f, height * 0.5f)
        val mediumColoredPoint4 = Offset(width * 0.75f, height * 0.7f)
        val mediumColoredPoint5 = Offset(width * 1.4f, -height.toFloat())

        val mediumColoredPath = Path().apply {
            moveTo(mediumColoredPoint1.x, mediumColoredPoint1.y)
            standardQuadFromTo(mediumColoredPoint1, mediumColoredPoint2)
            standardQuadFromTo(mediumColoredPoint2, mediumColoredPoint3)
            standardQuadFromTo(mediumColoredPoint3, mediumColoredPoint4)
            standardQuadFromTo(mediumColoredPoint4, mediumColoredPoint5)
            lineTo(width.toFloat() + 100f, height.toFloat() + 100f)
            lineTo(-100f, height.toFloat() + 100f)
            close()
        }

        val lightPoint1 = Offset(0f, height * 0.35f)
        val lightPoint2 = Offset(width * 0.1f, height * 0.4f)
        val lightPoint3 = Offset(width * 0.3f, height * 0.35f)
        val lightPoint4 = Offset(width * 0.65f, height.toFloat())
        val lightPoint5 = Offset(width * 1.4f, -height.toFloat() / 3f)

        val lightColoredPath = Path().apply {
            moveTo(lightPoint1.x, lightPoint1.y)
            standardQuadFromTo(lightPoint1, lightPoint2)
            standardQuadFromTo(lightPoint2, lightPoint3)
            standardQuadFromTo(lightPoint3, lightPoint4)
            standardQuadFromTo(lightPoint4, lightPoint5)
            lineTo(width.toFloat() + 100f, height.toFloat() + 100f)
            lineTo(-100f, height.toFloat() + 100f)
            close()
        }
        Canvas(modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }) {
            drawPath(
                path = mediumColoredPath,
                color = feature.mediumColor
            )
            drawPath(
                path = lightColoredPath,
                color = feature.lightColor
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {
            Text(
                text = feature.title,
                fontFamily = FontFamily.Default,
                style = MaterialTheme.typography.headlineMedium,
                lineHeight = 26.sp,
                modifier = Modifier.align(Alignment.TopStart)
            )
            Icon(
                painter = painterResource(id = feature.iconId),
                contentDescription = feature.title,
                tint = Color.White,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

@Composable
fun HomeContent(
    navController: NavHostController,
    homeNavController: NavHostController,
    connectivityViewModel: ConnectivityViewModel
) {
    val context= LocalContext.current

    val chipData = getChipsData(context)
    Column(modifier = Modifier
        .fillMaxSize()) {
        GreetingSection(homeNavController=homeNavController)
        LazyRow{
            items(chipData.size) { index ->
                ChipSection(chipData[index].name,index) {
                    navController.navigate("ChipDetailScreen/${index}"){
                        launchSingleTop=true
                        restoreState=true
                    }
                }
            }
        }
        CurrentMeditation()
        FeatureSection(
            features = listOf(
                Feature(
                    title = "Random Meditate",
                    R.drawable.ic_headphone,
                    LightGreen1,
                    LightGreen2,
                    LightGreen3
                ),
                Feature(
                    title = "Random Sleep",
                    R.drawable.ic_headphone,
                    Beige1,
                    Beige2,
                    Beige3
                ),
                Feature(
                    title = "Routines",
                    R.drawable.routine,
                    BlueViolet1,
                    BlueViolet2,
                    BlueViolet3
                )
            ),
            navController=navController,
            connectivityViewModel
        )
    }
}

@Composable
fun HomeNavigationFun(
    homeNavController: NavHostController,
    connectivityViewModel: ConnectivityViewModel
){
    val navController = rememberNavController()
    val viewModel: RoutineViewModel = viewModel(factory = RoutineViewModelFactory(RoutineRepository()))
    NavHost(navController = navController, startDestination = "homeContent"){
        composable("homeContent") {
            HomeContent(navController = navController,homeNavController=homeNavController,connectivityViewModel)
        }
        composable("ChipDetailScreen/{chipId}") {backStackEntry->
            val chipId = backStackEntry.arguments?.getString("chipId") ?: ""
            ChipDetailScreen(index = chipId, navController = navController)
        }
        composable("Routines") {
            RoutinePlannerScreen(navController=navController, viewModel=viewModel,connectivityViewModel)
        }
        composable(route="meditationDetail/{trackJson}",
            arguments = listOf(navArgument("trackJson") { type = NavType.StringType })) { backStackEntry ->
            val trackJson = backStackEntry.arguments?.getString("trackJson") ?: ""
            MeditationDetailScreen(trackJson = trackJson, navController = navController,connectivityViewModel)
        }
        composable(route="sleepDetail/{trackJson}",
            arguments = listOf(navArgument("trackJson") { type = NavType.StringType })) { backStackEntry ->
            val trackJson = backStackEntry.arguments?.getString("trackJson") ?: ""
            SleepDetailScreen(trackJson = trackJson, navController = navController,connectivityViewModel)
        }
    }
}

