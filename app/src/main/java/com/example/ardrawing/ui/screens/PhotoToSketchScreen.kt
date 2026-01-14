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
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import com.example.ardrawing.R
import com.example.ardrawing.ui.components.WaterWaveBackground

@Composable
fun PhotoToSketchScreen(
    onBackClick: () -> Unit,
    onPhotoSelected: (Uri) -> Unit // Callback when preparation is done
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isPreparing by remember { mutableStateOf(false) }

    // Launcher for picking an image
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            isPreparing = true
        }
    }

    // Mock preparation delay
    LaunchedEffect(isPreparing) {
        if (isPreparing) {
            delay(3000) // Simulate generic "preparation" logic
            // In a real app, you might do processing here
            // For now, we stay on "Preparing..." until user action or just show it for a bit?
            // The prompt says "Preparing...", implied it leads somewhere. 
            // For this UI demo, I'll keep it in "Preparing" state or invoke callback?
            // I'll keep it in 'Preparing' state to match the second screenshot. 
            // The user can implement the transition to the next screen (Sketch Preview) later.
        }
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

            if (!isPreparing || selectedImageUri == null) {
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                ) {
                    Text(
                        text = "Upload Photo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))

            } else {
                // ================= PREPARING STATE =================
                Spacer(modifier = Modifier.weight(1f))

                Box(contentAlignment = Alignment.Center) {
                    // Main Image Container with Blue Border
                    Box(
                        modifier = Modifier
                            .size(300.dp) // Square container
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(BorderStroke(4.dp, Color(0xFF4285F4)), RoundedCornerShape(24.dp))
                            .padding(8.dp) // Inner padding between border and image
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = "Selected Photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Sparkle Icon (Top Right Overlay)
                    // We place it outside the bordered box slightly
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 12.dp, y = (-12).dp)
                            .size(48.dp)
                            .background(Color.White, CircleShape)
                            .border(BorderStroke(1.dp, Color(0xFFE0E0E0)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                         // Using AutoAwesome as a placeholder for the specific sparkle asset
                         // In a real scenario, export the SVG/XML from the design
                         Icon(
                             imageVector = Icons.Default.AutoAwesome,
                             contentDescription = "Magic",
                             tint = Color(0xFF4285F4),
                             modifier = Modifier.size(24.dp)
                         )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Preparing...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4285F4)
                )

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
