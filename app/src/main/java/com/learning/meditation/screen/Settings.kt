package com.learning.meditation.screen

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.learning.meditation.R
import com.learning.meditation.ui.theme.ButtonTeal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun Settings(
    mainNavController: NavHostController,
    navController: NavHostController
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var currentName by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var nameDialog by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }
    var passwordDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false)}

    currentName= currentUser?.displayName.toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        navController.popBackStack(route = "profile", inclusive = false)
                    }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Settings",
                fontFamily = FontFamily.Default,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        SettingsSection(
            title = "Name",
            value = currentName,
            onEditClick = {
                newName=""
                nameDialog = true
            }
        )

        SettingsSection(
            title = "Current Password",
            value = "********",
            onEditClick = {
                currentPassword=""
                newPassword=""
                passwordDialog = true
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(Color.Red),
                onClick = { deleteDialog = true },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Delete Account", fontFamily = FontFamily.Default,color = Color.White)
            }
        }
    }

    if (nameDialog) {
        EditNameDialog(
            name = newName,
            onNameChange = { newName = it },
            onSave = {
                changeName(context, auth, newName.trim(),
                    onSuccess = { currentName=newName.trim() }
                )
                nameDialog = false
            },
            onCancel = { nameDialog = false }
        )
    }

    if (passwordDialog) {
        EditPasswordDialog(
            currentPassword = currentPassword,
            onCurrentPasswordChange = { currentPassword = it },
            newPassword = newPassword,
            onNewPasswordChange = { newPassword = it },
            onSuccess = {passwordDialog=false},
            onCancel = { passwordDialog = false }
        )
    }

    if (deleteDialog) {
        ConfirmDeleteDialog(
            onConfirm = {
                isLoading=true
                deleteAccount(mainNavController, context, auth,onSuccess={isLoading=false})
                deleteDialog = false
            },
            onCancel = { deleteDialog = false }
        )
    }
    if(isLoading){
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

@Composable
fun SettingsSection(title: String, value: String, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ButtonTeal),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Default,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 12.dp, top = 12.dp)
        )
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = value, color = Color.White,fontFamily = FontFamily.Default)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit $title",
                modifier = Modifier.clickable { onEditClick() },
                tint = Color.Green
            )
        }
    }
}

@Composable
fun EditNameDialog(name: String, onNameChange: (String) -> Unit, onSave: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        containerColor = ButtonTeal,
        onDismissRequest = { onCancel() },
        text = {
            Column {
                Text("Enter the new name", fontFamily = FontFamily.Default,color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    singleLine = true,
                    onValueChange = { onNameChange(it) },
                    label = { Text("New Name",fontFamily = FontFamily.Default) },
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
        },
        confirmButton = {
            TextButton(onClick = { onSave() }) {
                Text("Save", fontFamily = FontFamily.Default,color = Color.Green)
            }
        },
        dismissButton = {
            TextButton(onClick = { onCancel() }) {
                Text("Cancel",fontFamily = FontFamily.Default, color = Color.White)
            }
        }
    )
}

@Composable
fun EditPasswordDialog(
    currentPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var errorMessage by remember { mutableStateOf<String?>("") }

    AlertDialog(
        containerColor = ButtonTeal,
        onDismissRequest = { onCancel() },
        text = {
            val localFocusManager= LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current
            val keyboardActions = KeyboardActions(
                onNext = { localFocusManager.moveFocus(FocusDirection.Down) },
                onDone = {
                    changePassword(context, auth, currentPassword, newPassword,onSuccess={
                        onSuccess()
                    },onFailure={
                        errorMessage=it?.message
                    })
                    localFocusManager.clearFocus()
                    keyboardController?.hide()
                }
            )
            val imeActionNext = ImeAction.Next
            val imeActionDone = ImeAction.Done
            Column {
                Text("Enter current Password",fontFamily = FontFamily.Default, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentPassword,
                    singleLine = true,
                    onValueChange = { onCurrentPasswordChange(it) },
                    label = { Text("Current Password",fontFamily = FontFamily.Default) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color=Color.White),
                    keyboardActions = keyboardActions,
                    keyboardOptions = KeyboardOptions(imeAction = imeActionNext, keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Enter new Password", fontFamily = FontFamily.Default,color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    singleLine = true,
                    onValueChange = { onNewPasswordChange(it) },
                    label = { Text("New Password",fontFamily = FontFamily.Default) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color=Color.White),
                    keyboardActions = keyboardActions,
                    keyboardOptions = KeyboardOptions(imeAction = imeActionDone, keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (errorMessage != null) {
                    Text(text = errorMessage!!, fontFamily = FontFamily.Default,color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if(currentPassword.isNotEmpty()&&newPassword.isNotEmpty()) {
                    changePassword(context, auth, currentPassword, newPassword, onSuccess = {
                        onSuccess()
                    }, onFailure = {
                        errorMessage = it?.message
                    })
                }
                else{
                    Toast.makeText(context,"Fields can not be empty",Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Save", fontFamily = FontFamily.Default,color = Color.Green)
            }
        },
        dismissButton = {
            TextButton(onClick = { onCancel() }) {
                Text("Cancel", fontFamily = FontFamily.Default,color = Color.White)
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(onConfirm: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        containerColor = ButtonTeal,
        onDismissRequest = { onCancel() },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    tint = Color.Red,
                    contentDescription = null
                )
                Text("Are you sure you want to delete the account?",fontFamily = FontFamily.Default,color = Color.White)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("Delete", fontFamily = FontFamily.Default,color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = { onCancel() }) {
                Text("Cancel", fontFamily = FontFamily.Default,color = Color.White)
            }
        }
    )
}


fun changeName(context: Context, auth: FirebaseAuth, newName: String,onSuccess:()->Unit) {
    val currentUser = auth.currentUser
    val firestore=FirebaseFirestore.getInstance()
    val currentUserUid= currentUser?.let { firestore.collection("users").document(it.uid) }
    val profileUpdates = UserProfileChangeRequest.Builder()
        .setDisplayName(newName)
        .build()
    currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            currentUserUid?.update("name",newName.trim())?.addOnSuccessListener {
                onSuccess()
                Toast.makeText(context,"Name changed successfully",Toast.LENGTH_SHORT).show()
            }?.addOnFailureListener { exception ->
                throw(exception)
            }
        } else {
            Toast.makeText(context,"Unable to change Name",Toast.LENGTH_SHORT).show()
        }
    }
}

fun changePassword(
    context: Context,
    auth: FirebaseAuth,
    currentPassword: String,
    newPassword: String,
    onSuccess: () -> Unit,
    onFailure:(Exception?)->Unit
) {
    val user = auth.currentUser
    val credential = EmailAuthProvider.getCredential(user?.email ?: "", currentPassword)

    user?.reauthenticate(credential)
        ?.addOnCompleteListener { reAuthTask ->
            if (reAuthTask.isSuccessful) {
                user.updatePassword(newPassword)
                    .addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        } else {
                            onFailure(updateTask.exception)
                        }
                    }
            } else {
                onFailure(reAuthTask.exception)
            }
        }

}

fun deleteAccount(mainNavController: NavHostController, context: Context, auth: FirebaseAuth,onSuccess: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    currentUser?.let { user ->
        user.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                CoroutineScope(Dispatchers.IO).launch {
                    val isSuccess = deleteUserDataBatch(user.uid, firestore)
                    withContext(Dispatchers.Main) {
                        if (isSuccess) {
                            auth.signOut()
                            onSuccess()
                            mainNavController.popBackStack(route="home",inclusive = true)
                            Toast.makeText(context, "Account and data deleted successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to delete user data from Firestore.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                onSuccess()
                val exception = task.exception
                if (exception is FirebaseAuthRecentLoginRequiredException) {
                    Toast.makeText(context, "Please re-authenticate to delete your account.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show()
                }
            }
        }
    } ?: run {
        onSuccess()
        Toast.makeText(context, "No user is currently signed in.", Toast.LENGTH_SHORT).show()
    }
}

suspend fun deleteUserDataBatch(userId: String, firestore: FirebaseFirestore): Boolean {
    return try {
        val userDocRef = firestore.collection("users").document(userId)
        val batch = firestore.batch()

        val playlists = userDocRef.collection("myPlaylists").get().await()
        for (playlist in playlists) {
            val tracks = playlist.reference.collection("tracks").get().await()
            for (track in tracks) {
                batch.delete(track.reference)
            }
            batch.delete(playlist.reference)
        }

        val routines = userDocRef.collection("Routines").get().await()
        for (routine in routines) {
            val steps = routine.reference.collection("steps").get().await()
            for (step in steps) {
                batch.delete(step.reference)
            }
            batch.delete(routine.reference)
        }

        batch.delete(userDocRef)

        batch.commit().await()

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
