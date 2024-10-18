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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.learning.meditation.R
import com.learning.meditation.ui.theme.ButtonBlue
import com.learning.meditation.ui.theme.TealGradient
import com.learning.meditation.ui.theme.TextWhite
import com.learning.meditation.viewmodel.ConnectivityViewModel
import com.learning.meditation.viewmodel.LoadingViewModel

@Composable
fun SignUpScreen(
    navController: NavHostController,
    connectivityViewModel: ConnectivityViewModel
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isVerificationStage by remember { mutableStateOf(false) }
    val viewModel:LoadingViewModel= viewModel()
    val isLoading by viewModel.loading.collectAsState()

    val context= LocalContext.current
    val isConnected by connectivityViewModel.isConnected.collectAsState()
    val firebaseAuthHelper = FirebaseAuthHelper()

    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager= LocalFocusManager.current
    val keyboardActions = KeyboardActions(
        onNext = { localFocusManager.moveFocus(FocusDirection.Down) },
        onDone = {
            onSubmit2(
                context,
                name,
                email,
                password,
                confirmPassword,
                verificationCode,
                viewModel,
                firebaseAuthHelper,
                navController,
                isConnected,
                onError = {
                    errorMessage=it
                },
                isVerificationStage,
                verificationTrue = {
                    isVerificationStage=true
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
        Text(text = if (isVerificationStage) "Verify Code" else "Sign Up", style = MaterialTheme.typography.headlineMedium,fontFamily = FontFamily.Default)

        if (!isVerificationStage) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name",fontFamily = FontFamily.Default) },
                modifier = Modifier.fillMaxWidth(),
                keyboardActions = keyboardActions,
                keyboardOptions = KeyboardOptions(imeAction = imeActionNext),
                singleLine = true,
                textStyle = TextStyle(color=Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White
                )
            )
            OutlinedTextField(
                value = email,
                singleLine = true,
                onValueChange = { email = it.trim() },
                keyboardActions = keyboardActions,
                keyboardOptions = KeyboardOptions(imeAction = imeActionNext, keyboardType = KeyboardType.Email),
                label = { Text("Email",fontFamily = FontFamily.Default) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color=Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White
                )
            )
            OutlinedTextField(
                value = password,
                singleLine = true,
                onValueChange = { password = it.trim() },
                label = { Text("Password",fontFamily = FontFamily.Default) },
                modifier = Modifier.fillMaxWidth(),
                keyboardActions = keyboardActions,
                keyboardOptions = KeyboardOptions(imeAction = imeActionNext, keyboardType = KeyboardType.Password),
                textStyle = TextStyle(color=Color.White),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White
                )
            )
            OutlinedTextField(
                value = confirmPassword,
                singleLine = true,
                onValueChange = { confirmPassword = it.trim() },
                label = { Text("Confirm Password",fontFamily = FontFamily.Default) },
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
        } else {
            OutlinedTextField(
                value = verificationCode,
                singleLine = true,
                onValueChange = { verificationCode = it.trim() },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeActionDone),
                label = { Text("Enter 6 digit Verification Code",fontFamily = FontFamily.Default) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color=Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(text = errorMessage!!, fontFamily = FontFamily.Default,color = Color.Red)
        }

        Button(
            onClick = {
                onSubmit2(
                    context,
                    name,
                    email,
                    password,
                    confirmPassword,
                    verificationCode,
                    viewModel,
                    firebaseAuthHelper,
                    navController,
                    isConnected,
                    onError = {
                        errorMessage=it
                    },
                    isVerificationStage,
                    verificationTrue = {
                        isVerificationStage=true
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ButtonBlue)
        ) {
            Text(text = if (isVerificationStage) "Verify" else "Sign Up", fontFamily = FontFamily.Default,color = TextWhite)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!isVerificationStage) {
            TextButton(onClick = {
                navController.navigate("signIn") {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo("signUp") { inclusive = true }
                }
            }) {
                Text("Already have an account? Sign In", fontFamily = FontFamily.Default,textDecoration = TextDecoration.Underline)
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
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

fun onSubmit2(
    context: Context,
    name:String,
    email: String,
    password: String,
    confirmPassword:String,
    verificationCode:String,
    viewModel:LoadingViewModel,
    firebaseAuthHelper: FirebaseAuthHelper,
    navController: NavHostController,
    isConnected: Boolean,
    onError: (String?) -> Unit,
    isVerificationStage:Boolean,
    verificationTrue:()->Unit,
){
    if (!isVerificationStage) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            onError("All fields are required")
        } else if (password != confirmPassword) {
            onError("Passwords do not match")
        } else {
            if(isConnected){
                viewModel.yesLoading()
                firebaseAuthHelper.sendVerificationEmailBeforeSignUp(
                    context = context,
                    email = email,
                    onCodeSent = {
                        verificationTrue()
                        viewModel.notLoading()
                        onError(null)
                    },
                    onFailure = {
                        viewModel.notLoading()
                        onError(it?.message)
                    }
                )
            }else{
                Toast.makeText(context,"You are currently offline", Toast.LENGTH_SHORT).show()
            }
        }
    } else {
        if(isConnected) {
            firebaseAuthHelper.signUpWithEmail(
                context,
                email,
                password,
                name,
                verificationCode,
                viewModel,
                onSuccess = {
                    navController.navigate("home") {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo("signUp") { inclusive = true }
                    }
                    viewModel.notLoading()
                }
            ) {
                viewModel.notLoading()
                onError(it?.message)
            }
        }else{
            Toast.makeText(context,"You are currently offline", Toast.LENGTH_SHORT).show()
        }
    }
}

