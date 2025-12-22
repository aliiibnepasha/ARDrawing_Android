package com.example.ardrawing.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ardrawing.data.model.Lesson
import com.example.ardrawing.data.repository.LessonRepository
import com.example.ardrawing.ui.components.AppTopBar
import com.example.ardrawing.ui.utils.ImageToLessonConverter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLessonFromImageScreen(
    onBackClick: () -> Unit,
    onLessonCreated: (Lesson) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var lessonName by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (lessonName.isEmpty() && uri != null) {
            lessonName = "My Lesson ${System.currentTimeMillis()}"
        }
    }
    
    // Camera launcher - need to create temp file first
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            selectedImageUri = tempPhotoUri
            if (lessonName.isEmpty()) {
                lessonName = "My Lesson ${System.currentTimeMillis()}"
            }
        }
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Create Lesson from Image",
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Preview
            if (selectedImageUri != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .border(2.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Select Image",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = "No image selected",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            // Image Selection Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Camera Button
                Button(
                    onClick = {
                        val photoFile = java.io.File(context.cacheDir, "lesson_photo_${System.currentTimeMillis()}.jpg")
                        val photoUri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile
                        )
                        tempPhotoUri = photoUri
                        cameraLauncher.launch(photoUri)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Take Photo", fontSize = 14.sp)
                }
                
                // Gallery Button
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose from Gallery", fontSize = 14.sp)
                }
            }
            
            // Lesson Name Input
            OutlinedTextField(
                value = lessonName,
                onValueChange = { lessonName = it },
                label = { Text("Lesson Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color.Gray
                )
            )
            
            // Create Lesson Button
            Button(
                onClick = {
                    if (selectedImageUri != null && lessonName.isNotEmpty()) {
                        scope.launch {
                            isProcessing = true
                            try {
                                // Process image and create lesson
                                val lesson = ImageToLessonConverter.createLessonFromImage(
                                    context = context,
                                    imageUri = selectedImageUri!!,
                                    lessonName = lessonName
                                )
                                // Save lesson to repository
                                LessonRepository.addCreatedLesson(lesson)
                                onLessonCreated(lesson)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isProcessing = false
                            }
                        }
                    }
                },
                enabled = selectedImageUri != null && lessonName.isNotEmpty() && !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Create Lesson",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

