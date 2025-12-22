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
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ardrawing.data.local.entity.SavedDrawing
import com.example.ardrawing.ui.components.AppTopBar
import com.example.ardrawing.ui.utils.rememberAssetImagePainter
import com.example.ardrawing.ui.viewmodel.MyCreativeViewModel
import com.example.ardrawing.ui.viewmodel.TabType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCreativeScreen(
    viewModel: MyCreativeViewModel = viewModel(),
    onBackClick: () -> Unit,
    onDrawingClick: (SavedDrawing) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "My Creative",
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
            // Tab Navigation
            TabRow(
                selectedTabIndex = if (uiState.selectedTab == TabType.ALL_MEDIA) 0 else 1,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
                contentColor = Color.Black
            ) {
                Tab(
                    selected = uiState.selectedTab == TabType.ALL_MEDIA,
                    onClick = { viewModel.selectTab(TabType.ALL_MEDIA) },
                    text = {
                        Text(
                            text = "All Media",
                            fontWeight = if (uiState.selectedTab == TabType.ALL_MEDIA) FontWeight.Bold else FontWeight.Normal,
                            color = Color.Black
                        )
                    },
                    modifier = Modifier.background(
                        if (uiState.selectedTab == TabType.ALL_MEDIA) 
                            Color.LightGray.copy(alpha = 0.3f) 
                        else 
                            Color.White
                    )
                )
                Tab(
                    selected = uiState.selectedTab == TabType.SAVED,
                    onClick = { viewModel.selectTab(TabType.SAVED) },
                    text = {
                        Text(
                            text = "Saved",
                            fontWeight = if (uiState.selectedTab == TabType.SAVED) FontWeight.Bold else FontWeight.Normal,
                            color = Color.Black
                        )
                    },
                    modifier = Modifier.background(
                        if (uiState.selectedTab == TabType.SAVED) 
                            Color.LightGray.copy(alpha = 0.3f) 
                        else 
                            Color.White
                    )
                )
            }
            
            // Content Grid
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Black)
                }
            } else {
                val drawingsToShow = if (uiState.selectedTab == TabType.ALL_MEDIA) {
                    uiState.allDrawings
                } else {
                    uiState.savedDrawings
                }
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    items(drawingsToShow) { drawing ->
                        SavedDrawingItem(
                            drawing = drawing,
                            onClick = { onDrawingClick(drawing) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedDrawingItem(
    drawing: SavedDrawing,
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
            painter = rememberAssetImagePainter(drawing.imageAssetPath),
            contentDescription = drawing.templateName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

