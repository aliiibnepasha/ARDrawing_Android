package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.net.Uri
import com.example.ardrawing.R
import com.example.ardrawing.data.local.database.AppDatabase
import com.example.ardrawing.data.repository.SavedDrawingRepository
import com.example.ardrawing.ui.viewmodel.MyCreativeViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAlbumScreen(
    viewModel: MyCreativeViewModel,
    onBackClick: () -> Unit,
    onImageClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val uploadedImages = uiState.uploadedImages
    
    Scaffold(
        containerColor = Color.White,
        topBar = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.back_arrow_ic),
                contentDescription = "Back",
                modifier = Modifier
                    .size(32.dp)
                        .clickable { onBackClick() }
            )

            Text(
                text = "My Album",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
                
                Spacer(modifier = Modifier.size(32.dp))
            }
        }
    ) { paddingValues ->
        if (uploadedImages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No images yet",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                Text(
                        text = "Upload photos from My Creative screen",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(uploadedImages) { imageUri ->
                    AlbumImageItem(
                        imageUri = imageUri,
                        onClick = { onImageClick(imageUri) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumImageItem(
    imageUri: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(if (imageUri.startsWith("/")) File(imageUri) else Uri.parse(imageUri))
                .build(),
            contentDescription = "Album Image",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
    }
}
