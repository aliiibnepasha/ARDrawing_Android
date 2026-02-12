package com.example.ardrawing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R

data class Language(
    val code: String,
    val name: String,
    val flagEmoji: String
)

@Composable
fun LanguageScreen(
    onDoneClick: (String) -> Unit
) {
    // List of languages requested
    val languages = remember {
        listOf(
            Language("es", "Spanish", "🇪🇸"),
            Language("fr", "French", "🇫🇷"),
            Language("ko", "Korean", "🇰🇷"),
            Language("en", "English", "🇺🇸"),
            Language("de", "German", "🇩🇪"),
            Language("pt", "Portuguese", "🇵🇹"),
            Language("pl", "Polish", "🇵🇱"),
            Language("nl", "Dutch", "🇳🇱"),
            Language("tl", "Filipino", "🇵🇭"),
            Language("id", "Indonesian", "🇮🇩"),
            Language("ja", "Japanese", "🇯🇵"),
            Language("it", "Italian", "🇮🇹"),
            Language("ms", "Malay", "🇲🇾"),
            Language("tr", "Turkish", "🇹🇷"),
            Language("zh", "Chinese", "🇨🇳"),
            Language("el", "Greek", "🇬🇷"),
            Language("vi", "Vietnamese", "🇻🇳"),
            Language("ar", "Arabic", "🇦🇪")
        )
    }

    var selectedLanguageCode by remember { mutableStateOf("en") }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "Language",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Text(
                    text = "Done",
                    fontSize = 16.sp,
                    color = colorResource(R.color.app_blue),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDoneClick(selectedLanguageCode) },
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        },
        containerColor = Color(0xFFF5F5F5) // Light gray background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(languages) { language ->
                LanguageItem(
                    language = language,
                    isSelected = language.code == selectedLanguageCode,
                    onClick = { selectedLanguageCode = language.code }
                )
            }
            // Bottom padding
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) colorResource(R.color.app_blue) else Color.White
    val textColor = if (isSelected) Color.White else Color(0xFF5A6B7C) // Dark Grayish Blue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flag Emoji
        Text(
            text = language.flagEmoji,
            fontSize = 28.sp
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Language Text
        Text(
            text = language.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
