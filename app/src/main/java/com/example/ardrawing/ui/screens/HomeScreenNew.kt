package com.example.ardrawing.ui.screens

import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R
import com.example.ardrawing.data.model.Category
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.data.utils.AssetUtils
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.example.ardrawing.ui.components.WaterWaveBackground
import com.example.ardrawing.utils.GalleryUtils
import com.example.ardrawing.ui.utils.rememberAssetImagePainter
import com.example.ardrawing.LaunchActivity

@Composable
fun HomeScreenNew(
    currentRoute: String? = null,
    onTemplateSelected: (DrawingTemplate) -> Unit = {},
    onSeeAll: (Category) -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onStartAR: () -> Unit = {},
    onPhotoToSketch: () -> Unit = {},
    onAICreate: () -> Unit = {},
    onTextToImage: () -> Unit = {},
    onCustomText: () -> Unit = {},
    onProClick: () -> Unit = {},
    onExplore: () -> Unit = {},
    onAddIllustration: (String) -> Unit = {} // Simple ID (not used, URI stored in LaunchActivity)
) {
    var selectedTab by remember { mutableStateOf(0) }

    val context = LocalContext.current
    
    // Gallery launcher for "Add your illustration"
    val galleryLauncher = GalleryUtils.rememberGalleryLauncher { uri ->
        if (uri != null) {
            // Store URI in LaunchActivity to avoid route issues
            LaunchActivity.galleryImageUri = uri.toString()
            // Navigate with simple "gallery" ID
            onAddIllustration("gallery")
        }
    }

    // Wrap with Box to put Water Animation behind everything
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background Animation
        WaterWaveBackground()

        // 2. Foreground Content
    Scaffold(
            containerColor = Color.Transparent, // Transparent to show water background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { 
                HomeHeader(
                    currentRoute = currentRoute,
                    selectedTab = selectedTab
                ) 
            }
            item { 
                TabSwitcher(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                ) 
            }

            if (selectedTab == 0) {
                // Image Tab Content
                    item { 
                        IllustrationCard {
                            // Open gallery when clicked
                            GalleryUtils.openGallery(galleryLauncher)
                        }
                    }
                    item { ActionCardsRow(onPhotoToSketch, onAICreate, onExplore) }
                item { CategoriesSection(onCategoryClick = onCategoryClick) }
            } else {
                // Text Tab Content
                    item { TextTabContent(onTextToImage, onCustomText) }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun HomeHeader(currentRoute: String? = null, selectedTab: Int = 0) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(
                id = if (selectedTab == 1) R.drawable.text_avtr else R.drawable.home_avtr
            ),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "Welcome to",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Augmented Reality",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun TabSwitcher(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(4.dp)
    ) {
        TabButton(
            text = "Image",
            isSelected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            text = "Text",
            isSelected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF4285F4) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Black,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun IllustrationCard(onClick: () -> Unit) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val strokeWidth = with(density) { 2.dp.toPx() }
    val cornerRadius = with(density) { 24.dp.toPx() }
    val stroke = Stroke(width = strokeWidth, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    val primaryColor = Color(0xFF4285F4)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .drawBehind {
                drawRoundRect(color = primaryColor, style = stroke, cornerRadius = CornerRadius(cornerRadius))
            }
            .clickable(onClick = onClick) // Make it clickable to open gallery
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                 Image(
                     painter = painterResource(id = R.drawable.add_illustration),
                     contentDescription = "Add illustration",
                modifier = Modifier.size(32.dp)
                 )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Add your illustration", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF1E293B))
        }
    }
}

@Composable
fun ActionCardsRow(onPhotoToSketch: () -> Unit, onAICreate: () -> Unit, onExplore: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ActionCard(
            title = "Photo to\nsketch",
            buttonText = "Upload",
            iconRes = R.drawable.photo_to_sketch,
            onClick = onPhotoToSketch,
            modifier = Modifier.weight(1f)
        )
         ActionCard(
            title = "Create\nwith AI",
            buttonText = "Start",
            iconRes = R.drawable.create_with_ai,
            onClick = onAICreate,
            modifier = Modifier.weight(1f)
        )
         ActionCard(
            title = "Explore\nfrom web",
            buttonText = "Browse",
            iconRes = R.drawable.explore_from_web,
            onClick = onExplore,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActionCard(title: String, buttonText: String, iconRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(150.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

             Image(
                 painter = painterResource(id = iconRes),
                 contentDescription = null,
                 modifier = Modifier.size(32.dp)
             )
        Text(
            title, 
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = Color.Black
        )
        
        Button(
            onClick = onClick,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxWidth().height(32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
        ) {
            Text(buttonText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun CategoriesSection(onCategoryClick: (String) -> Unit) {
    val context = LocalContext.current

    Column {
        Text("Categories", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        // Load categories from assets/categories folder
        val categoryFolders = AssetUtils.listFolders(context, "categories")

        // Map categories folder names to home folder names for CategoryRepository compatibility
        val categoryMapping = mapOf(
            "aesthetics" to "Asthectic",
            "anatomy" to "Anatomy",
            "anime" to "Anime",
            "cartoon" to "Cartoon",
            "cute" to "Cute"
        )

        val categories = categoryFolders.map { folderName ->
            // Convert folder name to display name (capitalize first letter of each word)
            val displayName = folderName.split(" ").joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
            // Get the mapped category ID for navigation (use home folder name)
            val mappedCategoryId = categoryMapping[folderName] ?: folderName
            Triple(displayName, folderName, mappedCategoryId)
        }
        
        categories.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowItems.forEach { (name, folderName, mappedCategoryId) ->
                    CategoryItemNew(
                        name = name,
                        folderName = folderName,
                        onClick = { onCategoryClick(mappedCategoryId) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Add spacer if only one item in the row
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CategoryItemNew(name: String, folderName: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Get the first image from the category folder
    val categoryImages = AssetUtils.listImageFiles(context, "categories/$folderName")
    val firstImagePath = if (categoryImages.isNotEmpty()) {
        "categories/$folderName/${categoryImages.first()}"
    } else {
        null
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
            // Arrow above the image
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF4285F4),
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.End)
            )

            if (firstImagePath != null) {
                Image(
                    painter = rememberAssetImagePainter(firstImagePath),
                    contentDescription = name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                // Fallback if no images found
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
        }
    }
}

@Composable
fun TextTabContent(onTextToImage: () -> Unit, onCustomText: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Two Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextActionCard(
                title = "Write text to\ncreate image",
                subtitle = "Generate Art",
                iconRes = R.drawable.magic_pen, // Placeholder
                backgroundColor = Color.White,
                iconBackgroundColor = Color(0xFFE3F2FD), // Light Blue
                iconTint = Color(0xFF4285F4),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTextToImage() }
            )
            
            TextActionCard(
                title = "Create custom\ntext to draw",
                subtitle = "Generate Art",
                iconRes = R.drawable.text_icon, // Placeholder
                backgroundColor = Color.White,
                iconBackgroundColor = Color(0xFFE3F2FD),
                iconTint = Color(0xFF4285F4),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onCustomText() }
            )
        }

        // Inspiration Section
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Today's Inspiration",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            ) {
                 // Daily Image
                 Image(
                     painter = painterResource(R.drawable.daily_img),
                     contentDescription = "Today's Inspiration",
                     modifier = Modifier
                         .fillMaxSize(),
                     contentScale = ContentScale.Crop
                 )
                 
                // Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(topEnd = 16.dp)) // Design: Tab style
                        .background(Color(0xFF4285F4))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Lilly Close-Up",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun TextActionCard(
    title: String,
    subtitle: String,
    iconRes: Int,
    backgroundColor: Color,
    iconBackgroundColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
     Column(
        modifier = modifier
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {

         Image(
                 painter = painterResource(id = iconRes),
                 contentDescription = null,
             modifier = Modifier.size(40.dp)
             )

         Column {
             Text(
                 text = title,
                 fontSize = 15.sp, // Slightly increased size
                 fontWeight = FontWeight.SemiBold, // Reduced from Bold
                 color = Color(0xFF1C1C1C), // Slightly softer black
                 lineHeight = 22.sp 
             )
             Spacer(modifier = Modifier.height(4.dp))
             Text(
                 text = subtitle,
                 fontSize = 12.sp,
                 color = Color(0xFF7D7D7D) // Softer gray
             )
         }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenNewPreview() {
    HomeScreenNew(
        onCategoryClick = {}
    )
}
