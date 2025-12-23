package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun ColoringImageSelectionScreen(
    onImageSelected: (DrawingTemplate) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val templates = TemplateRepository.getTemplates(context)
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Select Image to Color",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            if (templates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No images available",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(templates) { template ->
                        ColoringTemplateItem(
                            template = template,
                            onClick = { onImageSelected(template) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColoringTemplateItem(
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

