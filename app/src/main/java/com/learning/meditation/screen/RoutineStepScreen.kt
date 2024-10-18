package com.learning.meditation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.learning.meditation.dataclass.RoutineStep
import com.learning.meditation.ui.theme.ButtonBlue
import com.learning.meditation.ui.theme.deepBlue
import com.learning.meditation.viewmodel.RoutineViewModel

@Composable
fun RoutineStepDisplay(step: RoutineStep, viewModel: RoutineViewModel, onComplete: () -> Unit) {
    val remainingTime by viewModel.remainingTime.collectAsState()
    val totalTime = step.durationMinutes * 60
    val isStart by viewModel.isTimerRunning.collectAsState()

    LaunchedEffect(step) {
        if(remainingTime==0) {
            viewModel.setRemainingTime(totalTime)
        }
    }

    LaunchedEffect(remainingTime) {
        if (remainingTime == 0 && isStart) {
            viewModel.resetTimer()
            onComplete()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = step.title,
            fontFamily = FontFamily.Default,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = step.description,
            fontFamily = FontFamily.Default,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp),
            color= Color.White
        )

        CircularTimer(
            remainingTime = remainingTime,
            totalTime = totalTime,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            if(!isStart){
                Button(
                    onClick = {
                        viewModel.startTimer(totalTime)
                    },
                    colors = ButtonDefaults.buttonColors(ButtonBlue)
                ) {
                    Text("Start",fontFamily = FontFamily.Default,color= Color.White)
                }
            }
            else{
                Button(
                    onClick = {
                        viewModel.stopTimer()
                    },
                    colors = ButtonDefaults.buttonColors(ButtonBlue))
                {
                    Text("Stop",fontFamily = FontFamily.Default,color= Color.White)
                }
            }
            Button(
                onClick = {
                    viewModel.resetTimer()
                    onComplete()
                },
                colors = ButtonDefaults.buttonColors(ButtonBlue)
            ) {
                Text(text = "Complete Step",fontFamily = FontFamily.Default, color = Color.White)
            }
        }

    }
}


@Composable
fun CircularTimer(
    remainingTime: Int,
    totalTime: Int,
    modifier: Modifier = Modifier)
{
    val progress = remainingTime.toFloat() / totalTime.toFloat()
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(200.dp)) {
            drawCircle(color = deepBlue, radius = size.minDimension / 2)

            drawArc(
                color= Color.Green,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                size = Size(size.width, size.height),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Text(text = formatTime(remainingTime),fontFamily = FontFamily.Default, style = MaterialTheme.typography.displaySmall,color= Color.White)

    }
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}


