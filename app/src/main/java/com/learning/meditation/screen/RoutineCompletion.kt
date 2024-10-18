package com.learning.meditation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.learning.meditation.ui.theme.ButtonBlue

@Composable
fun RoutineCompletionSummary(onBack:()->Unit,onRestart:()->Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Routine Complete!",
            fontFamily = FontFamily.Default,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = "Congratulations you've successfully completed your routine.",
            fontFamily = FontFamily.Default,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp),
            color= Color.White
        )
        Row(
            modifier = Modifier
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {onBack()},
                colors = ButtonDefaults.buttonColors(ButtonBlue))
            {
                Text("Back To Routines",fontFamily = FontFamily.Default,color= Color.White)
            }
            Button(
                onClick = {onRestart()},
                colors = ButtonDefaults.buttonColors(ButtonBlue),
                modifier = Modifier.padding(start=8.dp)
            ) {
                Text(text = "Restart",fontFamily = FontFamily.Default,color= Color.White)
            }
        }
    }
}
