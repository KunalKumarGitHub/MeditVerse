package com.learning.meditation.screen

import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageView
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.learning.meditation.R
import com.learning.meditation.dataclass.Routine
import com.learning.meditation.dataclass.RoutineStep
import com.learning.meditation.ui.theme.ButtonBlue
import com.learning.meditation.ui.theme.ButtonTeal
import com.learning.meditation.viewmodel.ConnectivityViewModel
import com.learning.meditation.viewmodel.RoutineViewModel
import java.util.UUID

@Composable
fun RoutinePlannerScreen(
    navController: NavHostController,
    viewModel: RoutineViewModel,
    connectivityViewModel: ConnectivityViewModel
) {
    val isConnected by connectivityViewModel.isConnected.collectAsState()
    val context= LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid

    LaunchedEffect(Unit){
        if (userId != null) {
            viewModel.fetchRoutinesIfNeeded(userId)
        }
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val selectedRoutine by viewModel.selectedRoutine.collectAsState()
    val routineSteps by viewModel.routineSteps.collectAsState()
    val currentStepIndex by viewModel.currentStepIndex.collectAsState(initial = 0)
    var showAddRoutineDialog by remember { mutableStateOf(false) }
    var showEditIcon by remember { mutableStateOf(false) }
    var showEditStepDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                modifier = Modifier
                    .clickable {
                        showEditIcon = false
                        if (selectedRoutine == null) {
                            navController.popBackStack(route = "homeContent", inclusive = false)
                        } else {
                            viewModel.notSelectRoutine()
                        }
                    },
                tint = Color.White,
                contentDescription = "back"
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (selectedRoutine == null) {
                    Text(
                        text = "Routine Planner",
                        fontFamily = FontFamily.Default,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = selectedRoutine!!.name,
                        fontFamily = FontFamily.Default,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (showEditIcon) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    modifier = Modifier.clickable {
                        if(isConnected){
                            showEditStepDialog = true
                        }else{
                            Toast.makeText(context,"You are currently offline",Toast.LENGTH_SHORT).show()
                        }
                    },
                    tint = Color.White,
                    contentDescription = "edit"
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    modifier = Modifier.clickable {
                        if(isConnected){
                            showAddRoutineDialog = true
                        }else{
                            Toast.makeText(context,"You are currently offline",Toast.LENGTH_SHORT).show()
                        }
                    },
                    tint = Color.White,
                    contentDescription = "add"
                )
            }
        }

        if (isLoading) {
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
        } else {
            if(isConnected){
                if (selectedRoutine == null) {
                    LazyColumn {
                        items(routines) { routine ->
                            RoutineItem(
                                routine,
                                onClick = {
                                    if (userId != null) {
                                        viewModel.fetchRoutineStepsIfNeeded(userId, routine.id)
                                        viewModel.selectRoutine(routine, userId)
                                    }
                                },
                                onDelete = {
                                    if (userId != null) {
                                        viewModel.deleteRoutine(userId, routine)
                                    }
                                },
                                connectivityViewModel
                            )
                        }
                    }
                } else {
                    showEditIcon = true
                    if (routineSteps.isNotEmpty() && currentStepIndex < routineSteps.size) {
                        RoutineStepDisplay(
                            step = routineSteps[currentStepIndex],
                            viewModel = viewModel,
                            onComplete = {
                                viewModel.markStepCompleted(currentStepIndex)
                                viewModel.goToNextStep()
                            }
                        )
                    } else if (routineSteps.isEmpty()) {
                        Text(text = "No steps available in this routine.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        showEditIcon = false
                        RoutineCompletionSummary(
                            onBack = {
                                viewModel.notSelectRoutine()
                            },
                            onRestart = {
                                if (userId != null) {
                                    viewModel.selectRoutine(selectedRoutine!!, userId)
                                    viewModel.fetchRoutineStepsIfNeeded(userId, selectedRoutine!!.id)
                                }
                            }
                        )
                    }
                }
            }else {
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




    if (showAddRoutineDialog) {
        AddRoutineDialog(
            onDismiss = { showAddRoutineDialog = false },
            onAddRoutine = { routine, steps ->
                if(isConnected){
                    if (userId != null) {
                        viewModel.addRoutine(userId,routine,steps)
                    }
                    showAddRoutineDialog = false
                }else{
                    Toast.makeText(context,"You are currently offline",Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showEditStepDialog) {
        Dialog(onDismissRequest = { showEditStepDialog = false }) {
            val keyboardController = LocalSoftwareKeyboardController.current
            val localFocusManager= LocalFocusManager.current
            val keyboardActions = KeyboardActions(
                onNext = { localFocusManager.moveFocus(FocusDirection.Down) },
                onDone = {
                    localFocusManager.clearFocus()
                    keyboardController?.hide()
                }
            )
            val imeActionNext = ImeAction.Next
            val imeActionDone = ImeAction.Done

            val step = routineSteps.getOrNull(currentStepIndex)
            var stepTitle by remember { mutableStateOf(step?.title ?: "") }
            var stepDescription by remember { mutableStateOf(step?.description ?: "") }
            var stepDuration by remember { mutableStateOf(step?.durationMinutes?.toString() ?: "") }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ButtonTeal,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = stepTitle,
                        singleLine = true,
                        onValueChange = { stepTitle = it },
                        label = { Text("Step Title",fontFamily = FontFamily.Default) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardActions = keyboardActions,
                        keyboardOptions = KeyboardOptions(imeAction = imeActionNext),
                        textStyle = TextStyle(color=Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = Color.White,
                            focusedBorderColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = stepDescription,
                        onValueChange = { stepDescription = it },
                        label = { Text("Step Description",fontFamily = FontFamily.Default) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardActions = keyboardActions,
                        textStyle = TextStyle(color=Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = Color.White,
                            focusedBorderColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = stepDuration,
                        singleLine = true,
                        onValueChange = { stepDuration = it.filter { char -> char.isDigit() } },
                        label = { Text("Step Duration (minutes)",fontFamily = FontFamily.Default) },
                        keyboardActions = keyboardActions,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = imeActionDone),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color=Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = Color.White,
                            focusedBorderColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        colors=ButtonDefaults.buttonColors(ButtonBlue),
                        onClick = {
                        if(isConnected){
                            if (stepTitle.isNotBlank() && stepDuration.isNotBlank()) {
                                if (userId != null) {
                                    selectedRoutine?.let { viewModel.editStep(userId,title=stepTitle,desc= stepDescription,duration= stepDuration.toInt(), it.id,routineSteps[currentStepIndex].id) }
                                }
                                showEditStepDialog = false
                            }
                        }else{
                            Toast.makeText(context,"You are currently offline",Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Edit Step",fontFamily = FontFamily.Default,color=Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineItem(
    routine: Routine,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    connectivityViewModel: ConnectivityViewModel
) {
    val context= LocalContext.current
    val isConnected by connectivityViewModel.isConnected.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(ButtonTeal)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            ) {
                Text(text = routine.name, fontFamily = FontFamily.Default,style = MaterialTheme.typography.titleMedium,color= Color.White)
                Text(text = "Steps: ${routine.stepCount}", fontFamily = FontFamily.Default,style = MaterialTheme.typography.bodyMedium,color= Color.White)
            }
            Icon(
                Icons.Default.Delete,
                tint = Color.White,
                contentDescription = "delete",
                modifier = Modifier.clickable {
                    showDeleteDialog = true
                }
            )
        }
    }
    if (showDeleteDialog) {
        DeleteRoutineDialog(
            onCancel = { showDeleteDialog = false },
            onDelete = {
                if(isConnected) {
                    onDelete()
                    showDeleteDialog = false
                }else{
                    Toast.makeText(context,"You are currently offline",Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun DeleteRoutineDialog(onCancel:()->Unit,onDelete: () -> Unit){
    AlertDialog(
        containerColor = ButtonTeal,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Warning",
                tint = Color.Red
            )
        },
        onDismissRequest = { onCancel() },
        text = {
            Column {
                Text("Are you confirm to delete the routine?", fontFamily = FontFamily.Default,color = Color.White)
            }
        },
        confirmButton = {
            TextButton(onClick = { onDelete() }) {
                Text(text = "Yes", fontFamily = FontFamily.Default,color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = { onCancel() }) {
                Text("No", fontFamily = FontFamily.Default,color = Color.White)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoutineDialog(onDismiss: () -> Unit, onAddRoutine: (Routine, List<RoutineStep>) -> Unit) {
    var routineName by remember { mutableStateOf("") }
    val steps = remember { mutableListOf<RoutineStep>() }
    var stepTitle by remember { mutableStateOf("") }
    var stepDescription by remember { mutableStateOf("") }
    var stepDuration by remember { mutableStateOf("") }
    var stepIndex=1

    AlertDialog(onDismissRequest = onDismiss) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val localFocusManager= LocalFocusManager.current
        val keyboardActions = KeyboardActions(
            onNext = { localFocusManager.moveFocus(FocusDirection.Down) },
            onDone = {
                localFocusManager.clearFocus()
                keyboardController?.hide()
            }
        )
        val imeActionNext = ImeAction.Next
        val imeActionDone = ImeAction.Done
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ButtonTeal,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Add New Routine", fontFamily = FontFamily.Default,style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = routineName,
                    singleLine = true,
                    onValueChange = { routineName = it },
                    label = { Text("Routine Name",fontFamily = FontFamily.Default) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardActions = keyboardActions,
                    keyboardOptions = KeyboardOptions(imeAction = imeActionNext),
                    textStyle = TextStyle(color=Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Add Step", fontFamily = FontFamily.Default,style = MaterialTheme.typography.titleMedium, color = Color.White)
                OutlinedTextField(
                    value = stepTitle,
                    singleLine = true,
                    onValueChange = { stepTitle = it },
                    label = { Text("Step Title",fontFamily = FontFamily.Default) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardActions = keyboardActions,
                    keyboardOptions = KeyboardOptions(imeAction = imeActionNext),
                    textStyle = TextStyle(color=Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = stepDescription,
                    onValueChange = { stepDescription = it },
                    label = { Text("Step Description",fontFamily = FontFamily.Default) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardActions = keyboardActions,
                    textStyle = TextStyle(color=Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = stepDuration,
                    singleLine = true,
                    onValueChange = { stepDuration = it.filter { char -> char.isDigit() } },
                    label = { Text("Step Duration (minutes)",fontFamily = FontFamily.Default) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardActions = keyboardActions,
                    keyboardOptions = KeyboardOptions(imeAction = imeActionDone, keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(color=Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = {
                    if (stepTitle.isNotBlank() && stepDuration.isNotBlank()) {
                        steps.add(
                            RoutineStep(
                                id = UUID.randomUUID().toString(),
                                index = stepIndex,
                                title = stepTitle,
                                description = stepDescription,
                                durationMinutes = stepDuration.toInt()
                            )
                        )
                        stepIndex+=1
                        stepTitle = ""
                        stepDescription = ""
                        stepDuration = ""
                    }
                }) {
                    Text(text = "Add Step", fontFamily = FontFamily.Default,color = Color.Green)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel",fontFamily = FontFamily.Default)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        if (routineName.isNotBlank() && steps.isNotEmpty()) {
                            val newRoutine = Routine(id = UUID.randomUUID().toString(), name = routineName, stepCount = steps.size)
                            onAddRoutine(newRoutine, steps)
                        }
                    }) {
                        Text("Add Routine", fontFamily = FontFamily.Default,color = Color.Green)
                    }
                }
            }
        }
    }
}


