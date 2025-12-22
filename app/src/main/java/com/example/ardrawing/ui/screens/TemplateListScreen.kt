package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.data.repository.TemplateRepository
import com.example.ardrawing.ui.components.AppTopBar
import com.example.ardrawing.ui.utils.rememberAssetImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateListScreen(
    onTemplateSelected: (DrawingTemplate) -> Unit,
    onStartLessonClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val templates = TemplateRepository.getTemplates(context)
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "AR Drawing",
                showBackButton = false
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartLessonClick,
                containerColor = Color(0xFF4CAF50),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Start Lesson",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            items(templates) { template ->
                TemplateItem(
                    template = template,
                    onClick = { onTemplateSelected(template) }
                )
            }
        }
    }
}

@Composable
fun TemplateItem(
    template: DrawingTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Image(
            painter = rememberAssetImagePainter(template.imageAssetPath),
            contentDescription = template.name,
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
            contentScale = ContentScale.Fit
        )
    }
}

