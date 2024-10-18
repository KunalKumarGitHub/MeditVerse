package com.learning.meditation.fragments

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.learning.meditation.R
import com.learning.meditation.screen.Settings
import com.learning.meditation.ui.theme.TextWhite
import com.learning.meditation.viewmodel.ConnectivityViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    mainNavController: NavHostController,
    navController: NavHostController,
    connectivityViewModel: ConnectivityViewModel
) {
    val isConnected by connectivityViewModel.isConnected.collectAsState()
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = remember { currentUser?.displayName.toString()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
    val email = remember { currentUser?.email ?: "No Email" }
    var profileImageUrl by remember { mutableStateOf(currentUser?.photoUrl?.toString() ?: "") }

    var loading by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { pickedUri ->
            loading = true

            val compressedUri = compressImage(context, pickedUri)

            compressedUri?.let { compressedImageUri ->
                uploadProfileImage(compressedImageUri, context) { newProfileUrl ->
                    profileImageUrl = newProfileUrl
                    loading = false
                }
            } ?: run {
                uploadProfileImage(pickedUri, context) { newProfileUrl ->
                    profileImageUrl = newProfileUrl
                    loading = false
                }
            }
        }
    }

    LaunchedEffect(currentUser) {
        profileImageUrl = currentUser?.photoUrl?.toString() ?: ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            if (loading) {
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUrl.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(profileImageUrl)
                                    .crossfade(true)
                                    .error(R.drawable.profile_pic)
                                    .size(Size.ORIGINAL)
                                    .build(),
                                contentScale = ContentScale.Crop
                            ),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Gray, CircleShape)
                                .clickable {
                                    showProfileDialog = true
                                },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.profile_pic),
                            contentDescription = "Default Profile Image",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Gray, CircleShape)
                                .clickable {
                                    showProfileDialog = true
                                },
                            contentScale = ContentScale.Crop
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile Picture",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .background(Color.White, shape = CircleShape)
                            .padding(4.dp)
                            .clickable {
                                if (isConnected) {
                                    imagePickerLauncher.launch("image/*")
                                } else {
                                    Toast
                                        .makeText(context, "You are offline", Toast.LENGTH_SHORT)
                                        .show()
                                }

                            },
                        tint = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = userName,
                fontFamily = FontFamily.Default,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            ProfileOption(icon = Icons.Default.Settings, title = "Settings", onClick = {
                navController.navigate("settings"){
                    launchSingleTop = true
                    restoreState = true
                }
            })
            ProfileOption(icon = Icons.Default.ExitToApp, title = "Logout", onClick = {
                mainNavController.popBackStack(route="home",inclusive = true)
                FirebaseAuth.getInstance().signOut()
            })

            Spacer(modifier = Modifier.height(32.dp))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.gmail),
                    contentDescription = "Gmail",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 16.dp)
                        .clickable {
                            val gmailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:kunal987kumar@gmail.com")
                                `package` = "com.google.android.gm"
                            }
                            try {
                                context.startActivity(gmailIntent)
                            } catch (e: ActivityNotFoundException) {
                                Toast
                                    .makeText(
                                        context,
                                        "Gmail app is not installed.",

                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        },
                    tint = Color.Unspecified
                )
                Icon(
                    painter = painterResource(R.drawable.linkedin),
                    contentDescription = "LinkedIn",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 16.dp)
                        .clickable {
                            val linkedInIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.linkedin.com/in/kunal-kumar-b93384226/")
                            )
                            context.startActivity(linkedInIntent)
                        },
                    tint = Color.Unspecified
                )

                Icon(
                    painter = painterResource(R.drawable.github),
                    contentDescription = "GitHub",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 16.dp)
                        .clickable {
                            val githubIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/KunalKumarGitHub")
                            )
                            context.startActivity(githubIntent)
                        },
                    tint = Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Developed with ",
                    fontFamily = FontFamily.Default,
                    color = TextWhite,
                    style = MaterialTheme.typography.bodyLarge
                )

                Icon(
                    imageVector = Icons.Default.Favorite,
                    tint = Color.Red,
                    contentDescription = "love",
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = " by Kunal",
                    fontFamily = FontFamily.Default,
                    color = TextWhite,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
    if(showProfileDialog){
        AlertDialog(
            onDismissRequest = { showProfileDialog=false },
            ) {
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
                        .size(240.dp)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.profile_pic),
                    contentDescription = "Default Profile Image",
                    modifier = Modifier
                        .size(240.dp)
                )
            }
        }
    }
}

fun compressImage(context: Context, uri: Uri): Uri? {
    val contentResolver = context.contentResolver

    return try {
        val inputStream = contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)

        val compressedFile = File(context.cacheDir, "compressed_profile_image.jpg")
        val outputStream = FileOutputStream(compressedFile)

        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

        outputStream.flush()
        outputStream.close()

        Uri.fromFile(compressedFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun uploadProfileImage(uri: Uri, context: Context, onComplete: (String) -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val storageReference = FirebaseStorage.getInstance().reference.child("profileImages/$userId.jpg")

    val uploadTask = storageReference.putFile(uri)

    uploadTask.addOnSuccessListener {
        storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
            updateFirebaseProfileImage(downloadUri) { newProfileUrl ->
                onComplete(newProfileUrl)
            }
        }
    }.addOnFailureListener {
        Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
        onComplete("")
    }
}

fun updateFirebaseProfileImage(downloadUri: Uri, onComplete: (String) -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser

    val profileUpdates = UserProfileChangeRequest.Builder()
        .setPhotoUri(downloadUri)
        .build()

    user?.updateProfile(profileUpdates)
        ?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(downloadUri.toString())
            } else {
                Log.e("ProfileUpdate", "Failed to update profile image.")
            }
        }
}



@Composable
fun ProfileOption(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier
                .size(36.dp)
                .padding(start = 10.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontFamily = FontFamily.Default,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
    }
    Divider(color = Color.White, thickness = 1.dp, modifier = Modifier.padding(10.dp))
}

@Composable
fun ProfileNavigationFun(
    homeNavController: NavHostController,
    mainNavController: NavHostController,
    connectivityViewModel: ConnectivityViewModel
){
    val navController= rememberNavController()
    NavHost(navController=navController, startDestination = "profile"){
        composable("profile") {
            Profile(mainNavController = mainNavController,navController=navController,connectivityViewModel)
        }
        composable("settings") {
            Settings(mainNavController,navController=navController)
        }
    }
}
