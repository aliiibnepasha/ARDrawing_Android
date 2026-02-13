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
import com.example.ardrawing.utils.FavoriteImageUtils
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.ardrawing.ui.components.ProfileHeader
import com.example.ardrawing.ui.components.WaterWaveBackground

@Composable
fun FavoriteScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Database setup
    val database = remember { AppDatabase.getDatabase(context) }
    val favoriteRepository = remember { FavoriteRepository(database.favoriteDao()) }

    // Observe favorites
    val favorites by favoriteRepository.getAllFavorites().collectAsState(initial = emptyList())

    // Wrap with Box to put Water Animation behind evÏerything
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        // 1. Background Animation
        WaterWaveBackground()

        // 2. Foreground Content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ProfileHeader (Same for both states, placed at top of column)
            ProfileHeader(
                avatarRes = R.drawable.text_avtr,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 20.dp)
            )
            
            Spacer(modifier = Modifier.height(10.dp))

            if (favorites.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Sad Face Icon
                        Image(
                            painter = painterResource(R.drawable.fav_icon),
                            contentDescription = null,
                            modifier = Modifier.size(50.dp)
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
                        .padding(horizontal = 20.dp), // Updated from 16.dp to 20.dp
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 0.dp, bottom = 80.dp) // Reset top padding
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
                    .size(32.dp)
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    //.clickable { onDeleteClick() }
            )
        }
    }
}

