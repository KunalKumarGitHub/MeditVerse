package com.learning.meditation.authentication

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.learning.meditation.R
import com.learning.meditation.ui.theme.ButtonBlue
import com.learning.meditation.ui.theme.TealGradient
import com.learning.meditation.ui.theme.TextWhite
import com.learning.meditation.viewmodel.ConnectivityViewModel


@Composable
fun SignInScreen(
    navController: NavHostController = rememberNavController(),
    connectivityViewModel: ConnectivityViewModel
) {
    val context= LocalContext.current
    val isConnected by connectivityViewModel.isConnected.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val firebaseAuthHelper = FirebaseAuthHelper()
    var isLoading by remember {
        mutableStateOf(false)
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager= LocalFocusManager.current
    val keyboardActions = KeyboardActions(
        onNext = { localFocusManager.moveFocus(FocusDirection.Down) },
        onDone = {
            onSubmit(
                context,
                email,
                password,
                firebaseAuthHelper,
                navController,
                isConnected,
                onError = {
                    errorMessage = it
                },
                loadingTrue={
                    isLoading=true
                },
                loadingFalse={
                    isLoading=false
                }
            )
            localFocusManager.clearFocus()
            keyboardController?.hide()
        }
    )
    val imeActionNext = ImeAction.Next
    val imeActionDone = ImeAction.Done

    Column(
        modifier = Modifier
            .background(TealGradient)
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Sign In", fontFamily = FontFamily.Default,style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = email,
            singleLine = true,
            onValueChange = { email = it.trim() },
            label = { Text("Email",fontFamily = FontFamily.Default) },
            modifier = Modifier.fillMaxWidth(),
            keyboardActions = keyboardActions,
            keyboardOptions = KeyboardOptions(imeAction = imeActionNext, keyboardType = KeyboardType.Email),
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

        OutlinedTextField(
            value = password,
            singleLine = true,
            onValueChange = { password = it.trim() },
            label = { Text("Password",fontFamily = FontFamily.Default) },
            keyboardActions = keyboardActions,
            keyboardOptions = KeyboardOptions(imeAction = imeActionDone, keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color=Color.White),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = Color.White,
                focusedBorderColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(text = errorMessage!!, fontFamily = FontFamily.Default,color = Color.Red)
        }

        Button(
            onClick = {
                onSubmit(
                    context,
                    email,
                    password,
                    firebaseAuthHelper,
                    navController,
                    isConnected,
                    onError = {
                        errorMessage = it
                    },
                    loadingTrue={
                        isLoading=true
                    },
                    loadingFalse={
                        isLoading=false
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ButtonBlue)
        ) {
            Text("Sign In",fontFamily = FontFamily.Default,color= TextWhite)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            navController.navigate("signUp") {
                launchSingleTop=true
                restoreState=true
                popUpTo("signIn") { inclusive = true }
            }
        }) {
            Text("Don't have an account? Sign Up", fontFamily = FontFamily.Default,textDecoration = TextDecoration.Underline)
        }
    }
    if(isLoading){
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
            contentAlignment = Alignment.Center){
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

fun onSubmit(
    context: Context,
    email: String,
    password: String,
    firebaseAuthHelper: FirebaseAuthHelper,
    navController: NavHostController,
    isConnected: Boolean,
    onError: (String) -> Unit,
    loadingTrue:()->Unit,
    loadingFalse:()->Unit,
) {
    if (email.isBlank()) {
        onError("Email cannot be empty")
    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onError("Please enter a valid email address")
    } else if (password.isBlank()) {
        onError("Password cannot be empty")
    } else {
        if(isConnected){
            loadingTrue()
            firebaseAuthHelper.signInWithEmail(
                email = email,
                password = password,
                onSuccess = {
                    loadingFalse()
                    navController.navigate("home") {
                        launchSingleTop=true
                        restoreState=true
                        popUpTo("signIn") { inclusive = true }
                    }
                },
                onFailure = {
                    loadingFalse()
                    onError("Invalid Credentials")
                }
            )
        }else{
            Toast.makeText(context,"You are currently offline",Toast.LENGTH_SHORT).show()
        }
    }
}
