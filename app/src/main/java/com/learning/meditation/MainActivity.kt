package com.learning.meditation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.learning.meditation.authentication.SignInScreen
import com.learning.meditation.authentication.SignUpScreen
import com.learning.meditation.ui.HomeScreen
import com.learning.meditation.ui.theme.MeditationTheme
import com.learning.meditation.ui.theme.TealGradient
import com.learning.meditation.viewmodel.ConnectivityViewModel
import com.learning.meditation.viewmodelfactory.ConnectivityViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Meditation_NoActionBar)
        setContent {
            MeditationTheme {
                Box(modifier = Modifier.fillMaxSize().background(TealGradient)){
                    val navController = rememberNavController()
                    NavHostSetup(navController)
                }
            }
        }
    }

    @Composable
    fun NavHostSetup(navController: NavHostController) {
        val context = LocalContext.current
        val connectivityViewModel: ConnectivityViewModel = viewModel(factory = ConnectivityViewModelFactory(context))
        val currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
        val startDestination = if (currentUser != null) "home" else "signIn"

        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            composable("signIn") {
                SignInScreen(navController = navController,connectivityViewModel)
            }
            composable("home") {
                val currentUser1 = FirebaseAuth.getInstance().currentUser
                if (currentUser1 != null) {
                    HomeScreen(mainNavController = navController,connectivityViewModel)
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate("signIn") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }
            }
            composable("signUp") {
                SignUpScreen(navController = navController,connectivityViewModel)
            }
        }
    }
}
