package com.learning.meditation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.learning.meditation.constants.getChipsData
import com.learning.meditation.ui.theme.ButtonTeal
import com.learning.meditation.ui.theme.TextWhite


@Composable
fun ChipDetailScreen(index: String, navController: NavController) {
    val accentColor = Color(0xfff4d35e)

    val context= LocalContext.current
    val currentChip= getChipsData(context)[index.toInt()]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "back",
                tint = accentColor,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        navController.popBackStack(route = "homeContent",inclusive = false)
                    },
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = currentChip.name,
                fontFamily = FontFamily.SansSerif,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Divider(color = accentColor, thickness = 1.dp)

        Spacer(modifier = Modifier.size(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState(), enabled = true)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(ButtonTeal),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Text(
                    text = currentChip.intro,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            SectionHeader(title = "Symptoms of ${currentChip.name}", color = accentColor)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(ButtonTeal),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ){
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    currentChip.symptoms.forEachIndexed { _,element ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(accentColor, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = element,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(24.dp))

            SectionHeader(title = "Diagnosis of ${currentChip.name}", color = accentColor)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors=CardDefaults.cardColors(ButtonTeal),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ){
                Text(
                    text = currentChip.diagnosis,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    color = TextWhite,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.size(24.dp))

            SectionHeader(title = "Treatments of ${currentChip.name}", color = accentColor)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors=CardDefaults.cardColors(ButtonTeal),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ){
                Text(
                    text = currentChip.treatmentIntro,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    color = TextWhite,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))

            currentChip.treatments.forEachIndexed { i, treatment ->
                Card(
                modifier = Modifier.fillMaxWidth()
                    .padding(8.dp),
                colors=CardDefaults.cardColors(ButtonTeal),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)){
                    Text(
                        text = "${i + 1}. ${treatment.name}",
                        fontFamily = FontFamily.SansSerif,
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp),
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp,top=8.dp,end=8.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = treatment.description,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        color = TextWhite,
                        modifier = Modifier.padding(start = 8.dp,end=8.dp, bottom = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        fontFamily = FontFamily.SansSerif,
        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
        textDecoration = TextDecoration.Underline,
        color = color,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

