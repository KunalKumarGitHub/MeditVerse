package com.learning.meditation.ui

import android.annotation.SuppressLint
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.learning.meditation.R
import com.learning.meditation.dataclass.BottomMenuContent
import com.learning.meditation.fragments.HomeNavigationFun
import com.learning.meditation.fragments.MeditationNavigationFun
import com.learning.meditation.fragments.MusicNavigationFun
import com.learning.meditation.fragments.ProfileNavigationFun
import com.learning.meditation.fragments.SleepNavigationFun
import com.learning.meditation.repository.MeditationRepository
import com.learning.meditation.repository.SleepRepository
import com.learning.meditation.ui.theme.*
import com.learning.meditation.viewmodel.ConnectivityViewModel
import com.learning.meditation.viewmodel.MeditationViewModel
import com.learning.meditation.viewmodel.SleepViewModel
import com.learning.meditation.viewmodelfactory.MeditationViewModelFactory
import com.learning.meditation.viewmodelfactory.SleepViewModelFactory


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(mainNavController: NavHostController, connectivityViewModel: ConnectivityViewModel) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = deepBlue,
        bottomBar = {
            Surface(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = Color.Transparent,
                modifier = Modifier.background(Color.Transparent)
            ) {
                BottomNavigationBar(
                    items = listOf(
                        BottomMenuContent("Home", "homeContent", R.drawable.ic_home),
                        BottomMenuContent("Meditate", "meditate", R.drawable.ic_bubble),
                        BottomMenuContent("Sleep", "sleep", R.drawable.ic_moon),
                        BottomMenuContent("Music", "music", R.drawable.ic_music),
                        BottomMenuContent("Profile", "profile", R.drawable.ic_profile),
                    ),
                    navController = navController,
                    onItemClick = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                )
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .background(TealGradient)
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                BottomNavigationFun(navController = navController, mainNavController,connectivityViewModel)
            }
        }
    )
}


@Composable
fun BottomNavigationBar(
    items: List<BottomMenuContent>,
    navController: NavHostController,
    onItemClick: (BottomMenuContent) -> Unit)
{
    val backStackEntry=navController.currentBackStackEntryAsState()
    NavigationBar(
        modifier= Modifier,
        containerColor = deepBlue,
        tonalElevation = 10.dp
    ) {
        items.forEach{item->
            val selected= item.route==backStackEntry.value?.destination?.route
            NavigationBarItem(
                selected = selected,
                modifier = Modifier
                    .background(color = deepBlue),
                onClick = { onItemClick(item) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ButtonBlue,
                    unselectedIconColor = Color.Gray
                ),
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Icon(
                            painter= painterResource(id = item.iconId),
                            contentDescription = item.title,
                            modifier = Modifier.size(22.dp)
                        )
                        if(selected){
                            Text(
                                text = item.title,
                                fontFamily = FontFamily.Default,
                                color= Color.Black,
                                textAlign = TextAlign.Center,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavigationFun(
    navController: NavHostController,
    mainNavController: NavHostController,
    connectivityViewModel: ConnectivityViewModel
){
    NavHost(navController = navController,
        startDestination = "homeContent",
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }){
        composable("homeContent"){
            HomeNavigationFun(homeNavController=navController,connectivityViewModel)
        }
        composable("meditate"){
            MeditationNavigationFun(homeNavController =navController,connectivityViewModel)
        }
        composable("sleep"){
            SleepNavigationFun(homeNavController=navController,connectivityViewModel)
        }
        composable("music"){
            MusicNavigationFun(homeNavController = navController,connectivityViewModel)
        }
        composable("profile"){
            ProfileNavigationFun(homeNavController = navController,mainNavController=mainNavController,connectivityViewModel)
        }
    }
}


