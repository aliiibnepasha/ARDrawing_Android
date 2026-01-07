package com.example.ardrawing.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.data.model.Category
import com.example.ardrawing.R

@Composable
fun HomeScreenNew(
    onTemplateSelected: (DrawingTemplate) -> Unit = {},
    onSeeAll: (Category) -> Unit = {},
    onStartAR: () -> Unit = {},
    onPhotoToSketch: () -> Unit = {},
    onAICreate: () -> Unit = {},
    onProClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Augmented Reality") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header Image
            Image(
                painter = painterResource(id = R.drawable.home_ar_card), // Using existing drawable
                contentDescription = "Header Image",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Illustration upload button
            Button(
                onClick = onStartAR,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Add your illustration")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Photo to sketch, Create with AI, Explore from web buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onPhotoToSketch, modifier = Modifier.weight(1f)) {
                    Text("Photo to sketch")
                }

                Button(onClick = onAICreate, modifier = Modifier.weight(1f)) {
                    Text("Create with AI")
                }

                Button(onClick = onProClick, modifier = Modifier.weight(1f)) {
                    Text("Explore from web")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Categories Section
            Text("Categories", style = MaterialTheme.typography.headlineSmall)

            // Categories Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CategoryItem(title = "Anime")
                CategoryItem(title = "Anatomy")
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CategoryItem(title = "One Line")
                CategoryItem(title = "For Kids")
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CategoryItem(title = "Architecture")
                CategoryItem(title = "Aesthetics")
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CategoryItem(title = "Food")
                CategoryItem(title = "Cute")
            }
        }
    }
}

@Composable
fun CategoryItem(title: String) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Add, // Placeholder icon, replace with your own
            contentDescription = title,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(title)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenNewPreview() {
    HomeScreenNew()
}
