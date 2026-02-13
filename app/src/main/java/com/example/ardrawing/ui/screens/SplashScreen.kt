package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R

@Composable
fun SplashScreen() {
    val splashFont = FontFamily(Font(R.font.just_hand))

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Full Screen Background Image
        Image(
            painter = painterResource(id = R.drawable.splash_img),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Text Section on Top
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "AUGMENTED REALITY",
                    fontSize = 30.sp,
                    fontFamily = splashFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 38.sp
                )
                Text(
                    text = "DRAWING",
                    fontSize = 30.sp,
                    fontFamily = splashFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 54.sp
                )
            }
        }
    }
}
