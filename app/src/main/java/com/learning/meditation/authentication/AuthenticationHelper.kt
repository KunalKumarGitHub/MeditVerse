package com.learning.meditation.authentication

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.learning.meditation.dataclass.Playlist
import com.learning.meditation.dataclass.Routine
import com.learning.meditation.dataclass.RoutineStep
import com.learning.meditation.viewmodel.LoadingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class FirebaseAuthHelper {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: (FirebaseUser?) -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(firebaseAuth.currentUser)
                } else {
                    onFailure(task.exception)
                }
            }
    }

    fun sendVerificationEmailBeforeSignUp(
        context: Context,
        email: String,
        onCodeSent: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        val verificationCode = (100000..999999).random().toString()

        sendVerificationEmail(email, verificationCode,onFailure)

        cacheVerificationCode(context, email, verificationCode)

        onCodeSent()
    }

    private fun cacheVerificationCode(context: Context, email: String, verificationCode: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("${KEY_VERIFICATION_CODE_PREFIX}$email", verificationCode)
        editor.apply()
    }

    private fun getCachedVerificationCode(context: Context, email: String): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("${KEY_VERIFICATION_CODE_PREFIX}$email", null)
    }

    private fun clearCachedVerificationCode(context: Context, email: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.remove("${KEY_VERIFICATION_CODE_PREFIX}$email")
        editor.apply()
    }


    fun signUpWithEmail(
        context: Context,
        email: String,
        password: String,
        name: String,
        inputCode: String,
        viewModel: LoadingViewModel,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        verifyEmailCode(context, email, inputCode,onSuccess= {
            viewModel.yesLoading()
            createFirebaseUser(email, password, name, onSuccess, onFailure)
        },onFailure= { exception ->
            onFailure(exception)
        })
    }

    private fun createFirebaseUser(
        email: String,
        password: String,
        name: String,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.let {
                        createUserInFirestore(it.uid, name, email, onSuccess, onFailure)

                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        it.updateProfile(profileUpdates)
                    }
                } else {
                    onFailure(task.exception)
                }
            }
    }


    private fun sendVerificationEmail(email: String, code: String, onFailure: (Exception?) -> Unit) {
        val username = "meditverse@gmail.com"
        val password = "bajblrpjdpkfovdz"

        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        CoroutineScope(Dispatchers.IO).launch {
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })

            try {
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(username))
                    setRecipients(
                        Message.RecipientType.TO,
                        InternetAddress.parse(email)
                    )
                    subject = "Verification Code"
                    setText("Your verification code is: $code")
                }

                Transport.send(message)

                withContext(Dispatchers.Main) {
                    println("Verification code sent to $email")
                }
            } catch (e: MessagingException) {
                onFailure(e)
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    val errorMessage = "Failed to send verification email: ${e.localizedMessage}"
                    Log.e("verification", errorMessage)
                }
            }
        }
    }

    private fun verifyEmailCode(
        context: Context,
        email: String,
        inputCode: String,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        val cachedCode = getCachedVerificationCode(context, email)

        if (cachedCode != null && cachedCode == inputCode) {
            onSuccess()
            clearCachedVerificationCode(context, email)
        } else {
            onFailure(Exception("Invalid verification code"))
        }
    }


    private fun createUserInFirestore(
        userId: String,
        name: String,
        email: String,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "isVerified" to false
        )

        val currentUser=firestore.collection("users").document(userId)

        currentUser
            .set(userData)
            .addOnSuccessListener {
                val playlistId = UUID.randomUUID().toString()
                val initialPlaylist = Playlist(
                    playlistId = playlistId,
                    playlistName = "Favorites",
                    trackCount = 0
                )

                currentUser
                    .collection("myPlaylists")
                    .document(playlistId)
                    .set(initialPlaylist)
                    .addOnSuccessListener {
                        currentUser
                            .collection("myPlaylists")
                            .document(playlistId)
                            .collection("tracks")
                            .document()
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }

                val routines=listOf(
                    Routine(
                        id = UUID.randomUUID().toString(),
                        name = "Nightly Routine",
                        stepCount = 8
                    ),
                    Routine(
                        id = UUID.randomUUID().toString(),
                        name = "Morning Routine",
                        stepCount = 5
                    ),
                    Routine(
                        id = UUID.randomUUID().toString(),
                        name = "Afternoon Relaxation",
                        stepCount = 4
                    )
                )
                val stepsList=listOf(
                    listOf(
                        RoutineStep("", 1,"Meditation", "Relax your mind with guided meditation", 10),
                        RoutineStep("", 2,"Stretching", "Gentle stretches to ease muscle tension", 5),
                        RoutineStep("", 3,"Journaling", "Write down your thoughts or worries", 5),
                        RoutineStep("", 4,"Ambient Sound", "Set up a calming sound environment", 2),
                        RoutineStep("", 5,"Wind Down", "Relax your mind and body by engaging in calm activities like reading or listening to soothing music.", 10),
                        RoutineStep("", 6,"Avoid Screens", "Turn off all electronic devices to reduce blue light exposure before bedtime.", 10),
                        RoutineStep("", 7,"Breathing Exercise", "Practice deep breathing exercises to lower your heart rate and calm your nervous system.", 5),
                        RoutineStep("", 8,"Ambient Sound", "Set up a calming sound environment", 2)
                    ),
                    listOf(
                        RoutineStep("", 1,"Wake Up", "Gently wake up with light stretching", 5),
                        RoutineStep("", 2,"Hydrate", "Drink a glass of water to start your day hydrated", 2),
                        RoutineStep("", 3,"Exercise", "Light exercise to boost your energy", 10),
                        RoutineStep("", 4,"Breakfast", "Prepare and eat a healthy breakfast", 20),
                        RoutineStep("", 5,"Plan Your Day", "Write down your goals for the day", 5)
                    ),
                    listOf(
                        RoutineStep("", 1,"Breathing Exercises", "Take deep breaths to relax", 5),
                        RoutineStep("", 2,"Light Snack", "Have a light and healthy snack", 5),
                        RoutineStep("", 3,"Stretching", "Stretch your muscles to stay flexible", 5),
                        RoutineStep("", 4,"Walk", "Take a short walk to refresh your mind", 10)
                    )
                )
                routines.forEachIndexed { index, routine ->
                    addRoutinesInUser(userId,routine,stepsList[index],onFailure)
                }
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    private fun addRoutinesInUser(
        userId: String,
        routine: Routine,
        steps:List<RoutineStep>,
        onFailure: (Exception?) -> Unit
    ){
        val currentUser=firestore.collection("users").document(userId)
        currentUser
            .collection("Routines")
            .document(routine.id)
            .set(routine)
            .addOnSuccessListener {
                steps.forEach {step->
                    val stepId = UUID.randomUUID().toString()
                    step.id=stepId
                    currentUser
                        .collection("Routines")
                        .document(routine.id)
                        .collection("steps")
                        .document(stepId)
                        .set(step)
                        .addOnSuccessListener {  }
                        .addOnFailureListener { onFailure(it) }
                }
            }
    }

    companion object {
        private const val PREFS_NAME = "VerificationPrefs"
        private const val KEY_VERIFICATION_CODE_PREFIX = "verification_code_"
    }
}

