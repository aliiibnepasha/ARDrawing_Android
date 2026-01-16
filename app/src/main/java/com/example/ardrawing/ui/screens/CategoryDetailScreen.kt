package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R
import com.example.ardrawing.data.model.Category
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.data.repository.CategoryRepository
import com.example.ardrawing.ui.components.AppTopBar
import com.example.ardrawing.ui.screens.TemplateItem
import com.example.ardrawing.ui.utils.rememberAssetImagePainter
import com.example.ardrawing.utils.GalleryUtils
import com.example.ardrawing.LaunchActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryId: String,
    onTemplateSelected: (DrawingTemplate) -> Unit,
    onBackClick: () -> Unit,
    onAddIllustration: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val category = CategoryRepository.getCategoryById(context, categoryId)
    
    // Gallery launcher for "Add your illustration"
    val galleryLauncher = GalleryUtils.rememberGalleryLauncher { uri ->
        if (uri != null) {
            // Store URI in LaunchActivity for later retrieval
            LaunchActivity.galleryImageUri = uri.toString()
            // Navigate to DrawingModeSelectionScreen with a generic ID for gallery
            onAddIllustration("gallery")
        }
    }
    
    Scaffold(
        containerColor = Color.White,
        topBar = {
            // Custom Top Bar with Centered Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                Image(
                    painter = painterResource(R.drawable.back_arrow_ic),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onBackClick() }
                )
                
                // Centered Title
                Text(
                    text = category?.displayName ?: "Category",
                    fontSize = 20.sp, // Slightly larger
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                // Invisible spacer to balance the back button and keep title centered
                Spacer(modifier = Modifier.size(40.dp))
            }
        }
    ) { paddingValues ->
        if (category == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Category not found",
                    color = Color.Gray
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // TABS ROW
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp) // Spacing between tabs
                ) {
                    // "All" Tab (Active)
                    CategoryTabItem(text = "All", isSelected = true)
                    
                    // Other Tabs (Inactive)
                    CategoryTabItem(text = "Free", isSelected = false)
                    CategoryTabItem(text = "PRO", isSelected = false)
                    CategoryTabItem(text = "Popular", isSelected = false)
                }
                
                // GRID
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. "Add your illustration" Card (First Item)
                    item {
                        AddIllustrationCard(onClick = { GalleryUtils.openGallery(galleryLauncher) })
                    }
                    
                    // 2. Template Items
                    items(category.templates) { template ->
                        TemplateItem(
                            template = template,
                            onClick = { onTemplateSelected(template) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTabItem(text: String, isSelected: Boolean) {
    if (isSelected) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(colorResource(R.color.app_blue)) // Blue
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        Text(
            text = text,
            color = Color(0xFF1C1C1C),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(vertical = 6.dp) // Align vertically with pill
        )
    }
}

@Composable
private fun AddIllustrationCard(onClick: () -> Unit) {
    val stroke = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    val borderColor = colorResource(R.color.app_blue)
    
    Box(
        modifier = Modifier
            .aspectRatio(1f) // Square card
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FBFF)) // Very light blue bg
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = stroke
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
                )
            }
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
                Image(
                    painter = painterResource(id = R.drawable.add_illustration),
                    contentDescription = "Add illustration",
                    modifier = Modifier.size(32.dp)
                )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Add your\nillustration",
                fontSize = 12.sp,
                color = Color(0xFF1C1C1C),
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

