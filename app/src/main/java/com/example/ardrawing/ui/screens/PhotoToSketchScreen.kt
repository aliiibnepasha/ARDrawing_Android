package com.example.ardrawing.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.ardrawing.R
import com.example.ardrawing.data.repository.TextToImageRepository
import com.example.ardrawing.ui.components.WaterWaveBackground
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import kotlin.math.roundToInt
import kotlin.math.abs

@Composable
fun PhotoToSketchScreen(
    onBackClick: () -> Unit,
    onSketchGenerated: (Bitmap) -> Unit // Callback when sketch is generated
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val textToImageRepository = remember { TextToImageRepository() }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var generatedSketchBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Toggle this to use a dummy image (selected image) instead of calling the API
    val useDummyImage = true

    // Launcher for picking an image
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            isGenerating = true
            errorMessage = null
            generatedSketchBitmap = null


            // Start generating sketch immediately when image is selected
            scope.launch {
                if (useDummyImage) {
                    // Simulate a short delay to mimic processing
                    kotlinx.coroutines.delay(2000)
                    try {
                        // Use the selected image as the "sketch"
                        val bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            android.graphics.BitmapFactory.decodeStream(inputStream)
                        }
                        if (bitmap != null) {
                            generatedSketchBitmap = bitmap
                        } else {
                            errorMessage = "Failed to load image"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error loading dummy image: ${e.message}"
                    } finally {
                        isGenerating = false
                    }
                } else {
                    // Real API Call
                    val result = textToImageRepository.convertPhotoToSketch(context, uri)
                    isGenerating = false

                    result.onSuccess { bitmap ->
                        generatedSketchBitmap = bitmap
                        // Don't auto-navigate - let user see the result first
                    }.onFailure { error ->
                        // Always show user-friendly message, ignore technical error details
                        errorMessage = "Failed to generate sketch"
                    }
                }
            }
        }
    }

    // Show progress dialog when generating
    if (isGenerating) {
        AlertDialog(
            onDismissRequest = { /* Don't allow dismiss while generating */ },
            title = {
                Text(
                    text = "Generating Sketch",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    CircularProgressIndicator(
                        color = colorResource(R.color.app_blue),
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Converting your photo to sketch...",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This may take a few seconds",
                        fontSize = 12.sp,
                        color = Color.Gray.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {},
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Wrap with Box to put Water Animation behind everything
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background Animation
        WaterWaveBackground()

        // 2. Foreground Content
        Scaffold(
            containerColor = Color.Transparent, // Transparent to show water background
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.back_arrow_ic),
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onBackClick() }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedImageUri == null) {
                    // ================= INSTRUCTION STATE =================
                    Text(
                        text = "Upload a face photo",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Some photo requirements",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Requirements List
                    RequirementItem(
                        text = "One person in the photo",
                        hasCheck = true,
                        hasCross = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    RequirementItem(
                        text = "Clear face and close to camera",
                        hasCheck = true,
                        hasCross = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    RequirementItem(
                        text = "Face forward, no profile shots",
                        hasCheck = true,
                        hasCross = true
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.app_blue))
                    ) {
                        Text(
                            text = "Upload Photo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(40.dp))

                } else {
                    // ================= RESULT/ERROR STATE =================
                    Spacer(modifier = Modifier.weight(1f))

                    if (errorMessage != null) {
                        // ================= ERROR STATE =================
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Error Icon
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFEBEE))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(72.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Failed to Generate Sketch",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = errorMessage!!,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Retry Button
                            Button(
                                onClick = {
                                    selectedImageUri?.let { uri ->
                                        isGenerating = true
                                        errorMessage = null
                                        scope.launch {
                                            if (useDummyImage) {
                                                // Simulate a short delay to mimic processing
                                                kotlinx.coroutines.delay(2000)
                                                try {
                                                    // Use the selected image as the "sketch"
                                                    val bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                        val inputStream = context.contentResolver.openInputStream(uri)
                                                        android.graphics.BitmapFactory.decodeStream(inputStream)
                                                    }
                                                    if (bitmap != null) {
                                                        generatedSketchBitmap = bitmap
                                                    } else {
                                                        errorMessage = "Failed to load image"
                                                    }
                                                } catch (e: Exception) {
                                                    errorMessage = "Error loading dummy image: ${e.message}"
                                                } finally {
                                                    isGenerating = false
                                                }
                                            } else {
                                                val result = textToImageRepository.convertPhotoToSketch(context, uri)
                                                isGenerating = false
                                                result.onSuccess { bitmap ->
                                                    generatedSketchBitmap = bitmap
                                                }.onFailure { error ->
                                                    errorMessage = error.message ?: "Failed to generate sketch"
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.app_blue))
                            ) {
                                Text(
                                    text = "Try Again",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Back Button
                            TextButton(
                                onClick = {
                                    selectedImageUri = null
                                    errorMessage = null
                                    generatedSketchBitmap = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Choose Different Photo",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        // ================= SUCCESS STATE =================
                        Box(contentAlignment = Alignment.Center) {
                            // Main Image Container with Blue Border
                            Box(
                                modifier = Modifier
                                    .size(300.dp) // Square container
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.White)
                                    .border(BorderStroke(4.dp, colorResource(R.color.app_blue)), RoundedCornerShape(24.dp))
                                    .padding(8.dp) // Inner padding between border and image
                            ) {
                                if (generatedSketchBitmap != null) {
                                    // Show generated sketch
                                    Image(
                                        bitmap = generatedSketchBitmap!!.asImageBitmap(),
                                        contentDescription = "Generated Sketch",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Show original image
                                    Image(
                                        painter = rememberAsyncImagePainter(selectedImageUri),
                                        contentDescription = "Selected Photo",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            // Sparkle Icon (Top Right Overlay)
                            if (generatedSketchBitmap != null) {
                                // Sparkle Icon (Draggable on Border)
                                if (generatedSketchBitmap != null) {
                                    var boxSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
                                    var starPosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
                                    var isInitialized by remember { mutableStateOf(false) }

                                    // Initialize star position to top-right corner once box size is known
                                    LaunchedEffect(boxSize) {
                                        if (boxSize != androidx.compose.ui.geometry.Size.Zero && !isInitialized) {
                                            starPosition = androidx.compose.ui.geometry.Offset(boxSize.width, 0f)
                                            isInitialized = true
                                        }
                                    }

                                    // Invisible Overlay to capture size of the parent container
                                    // We use matchParentSize so this overlay matches the 300dp box exactly
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .onSizeChanged { size ->
                                                boxSize = size.toSize()
                                            }
                                    )

                                    if (isInitialized) {
                                        Box(
                                            modifier = Modifier
                                                .offset { IntOffset(starPosition.x.roundToInt(), starPosition.y.roundToInt()) }
                                                .offset(x = (-24).dp, y = (-24).dp) // Center the 48dp box on the point
                                                .size(48.dp)
                                                // Removed background/border to make it just the star as requested?
                                                // User code snippet showed just Image, but also "moveable hu".
                                                // I'll keep a touch target area but maybe make it transparent?
                                                // User said "hum ny start ko move karna hai... do it fixed".
                                                // The snippet was: Image(painterResource(R.drawable.move_star)...)
                                                // I'll put the Image inside this draggable box.
                                                .pointerInput(boxSize) {
                                                    detectDragGestures { change, dragAmount ->
                                                        change.consume()

                                                        // Calculate new potential position
                                                        val newX = (starPosition.x + dragAmount.x).coerceIn(0f, boxSize.width)
                                                        val newY = (starPosition.y + dragAmount.y).coerceIn(0f, boxSize.height)

                                                        // Snap to nearest border
                                                        // Distances to edges
                                                        val distLeft = newX
                                                        val distRight = boxSize.width - newX
                                                        val distTop = newY
                                                        val distBottom = boxSize.height - newY

                                                        val minDist = minOf(distLeft, distRight, distTop, distBottom)

                                                        starPosition = when (minDist) {
                                                            distLeft -> androidx.compose.ui.geometry.Offset(0f, newY)
                                                            distRight -> androidx.compose.ui.geometry.Offset(boxSize.width, newY)
                                                            distTop -> androidx.compose.ui.geometry.Offset(newX, 0f)
                                                            else -> androidx.compose.ui.geometry.Offset(newX, boxSize.height)
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(R.drawable.move_star),
                                                contentDescription = "star",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }

                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (generatedSketchBitmap != null) {
                            Text(
                                text = "Sketch Generated Successfully!",
                                fontSize = 18.sp,

                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.app_blue)
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Use To Draw Button
                            Button(
                                onClick = {
                                    generatedSketchBitmap?.let { bitmap ->
                                        onSketchGenerated(bitmap)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.app_blue))
                            ) {
                                Text(
                                    text = "Use To Draw",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                text = "Processing...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.app_blue)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1.3f))
                }
            }
        }
    }
}

@Composable
fun RequirementItem(text: String, hasCheck: Boolean, hasCross: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        if (hasCheck) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Background Box (Clipped)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF0F0F0))
                )
                // Icon (Unclipped)
                Image(
                    painter = painterResource(R.drawable.green_check),
                    contentDescription = "Valid",
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = 8.dp, y = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (hasCross) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Background Box (Clipped)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF0F0F0))
                )
                // Icon (Unclipped)
                Image(
                    painter = painterResource(R.drawable.red_check),
                    contentDescription = "Invalid",
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = 8.dp, y = 8.dp)
                )
            }
        }
    }
}
