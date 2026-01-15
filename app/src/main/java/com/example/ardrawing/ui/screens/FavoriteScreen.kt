package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ardrawing.R
import com.example.ardrawing.data.local.database.AppDatabase
import com.example.ardrawing.data.repository.FavoriteRepository
import com.example.ardrawing.ui.components.WaterWaveBackground
import com.example.ardrawing.utils.FavoriteImageUtils
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.graphics.BitmapFactory

@Composable
fun FavoriteScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Database setup
    val database = remember { AppDatabase.getDatabase(context) }
    val favoriteRepository = remember { FavoriteRepository(database.favoriteDao()) }
    
    // Observe favorites
    val favorites by favoriteRepository.getAllFavorites().collectAsState(initial = emptyList())
    
    // Wrap with Box to put Water Animation behind everything
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background Animation
        WaterWaveBackground()
        
        // 2. Foreground Content
        Scaffold(
            containerColor = Color.Transparent, // Transparent to show water background
            topBar = {
                FavoriteHeader()
            }
        ) { paddingValues ->
            if (favorites.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Sad Face Icon
                        Icon(
                            imageVector = Icons.Outlined.SentimentDissatisfied,
                            contentDescription = null,
                            tint = Color(0xFFA0C1F8), // Light blue tint
                            modifier = Modifier.size(64.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No Favorites Added",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6F7E95) // Grey text
                        )
                    }
                }
            } else {
                // Favorites Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favorites) { favorite ->
                        FavoriteImageCard(
                            favorite = favorite,
                            onDeleteClick = {
                                scope.launch {
                                    // Delete image file
                                    favorite.imagePath?.let { FavoriteImageUtils.deleteFavoriteImage(it) }
                                    favoriteRepository.deleteFavorite(favorite)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteImageCard(
    favorite: com.example.ardrawing.data.local.entity.Favorite,
    onDeleteClick: () -> Unit
) {
    // Load image bitmap
    val imageBitmap = remember(favorite.imagePath) {
        favorite.imagePath?.let { FavoriteImageUtils.loadFavoriteImage(it) }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Square cards
            .clickable { /* Can add navigation to view/edit */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image
            if (imageBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = imageBitmap.asImageBitmap(),
                    contentDescription = favorite.prompt,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = favorite.prompt.take(20),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            
            // Favorite Icon (Top Right)
            Image(
                painter = painterResource(R.drawable.my_fav_blue_ic),
                contentDescription = "Favorite",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clickable { onDeleteClick() }
            )
        }
    }
}

@Composable
fun FavoriteHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.home_avtr),
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
