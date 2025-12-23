package com.example.ardrawing.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Custom canvas component for coloring with flood fill algorithm
 */
@Composable
fun ColoringCanvas(
    originalBitmap: Bitmap?,
    selectedColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onBitmapChanged: (Bitmap) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var coloredBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var displayBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    
    // Initialize bitmap
    LaunchedEffect(originalBitmap) {
        if (originalBitmap != null) {
            coloredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            displayBitmap = coloredBitmap?.asImageBitmap()
        }
    }
    
    // Update display when color changes
    LaunchedEffect(coloredBitmap) {
        coloredBitmap?.let {
            displayBitmap = it.asImageBitmap()
            onBitmapChanged(it)
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(selectedColor, coloredBitmap, canvasSize) {
                detectTapGestures { tapOffset ->
                    coloredBitmap?.let { bitmap ->
                        if (canvasSize.width > 0 && canvasSize.height > 0) {
                            val imageWidth = bitmap.width.toFloat()
                            val imageHeight = bitmap.height.toFloat()
                            val canvasWidth = canvasSize.width
                            val canvasHeight = canvasSize.height
                            
                            // Calculate scaling (same as in draw scope)
                            val scaleX = canvasWidth / imageWidth
                            val scaleY = canvasHeight / imageHeight
                            val scale = minOf(scaleX, scaleY)
                            
                            val scaledWidth = imageWidth * scale
                            val scaledHeight = imageHeight * scale
                            val offsetX = (canvasWidth - scaledWidth) / 2
                            val offsetY = (canvasHeight - scaledHeight) / 2
                            
                            // Convert tap coordinates to bitmap coordinates
                            val bitmapX = ((tapOffset.x - offsetX) / scale).toInt()
                            val bitmapY = ((tapOffset.y - offsetY) / scale).toInt()
                            
                            // Check if tap is within bitmap bounds
                            if (bitmapX >= 0 && bitmapX < bitmap.width && 
                                bitmapY >= 0 && bitmapY < bitmap.height) {
                                coroutineScope.launch {
                                    val filledBitmap = withContext(Dispatchers.Default) {
                                        floodFill(
                                            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true),
                                            x = bitmapX,
                                            y = bitmapY,
                                            newColor = selectedColor
                                        )
                                    }
                                    coloredBitmap = filledBitmap
                                }
                            }
                        }
                    }
                }
            }
    ) {
        canvasSize = size
        displayBitmap?.let { bitmap ->
            val imageWidth = bitmap.width.toFloat()
            val imageHeight = bitmap.height.toFloat()
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            // Calculate scaling to fit image in canvas while maintaining aspect ratio
            val scaleX = canvasWidth / imageWidth
            val scaleY = canvasHeight / imageHeight
            val scale = minOf(scaleX, scaleY)
            
            val scaledWidth = imageWidth * scale
            val scaledHeight = imageHeight * scale
            val offsetX = (canvasWidth - scaledWidth) / 2
            val offsetY = (canvasHeight - scaledHeight) / 2
            
            drawImage(
                image = bitmap,
                dstOffset = IntOffset(offsetX.toInt(), offsetY.toInt()),
                dstSize = IntSize(scaledWidth.toInt(), scaledHeight.toInt())
            )
        }
    }
}


/**
 * Flood fill algorithm to fill connected areas with the same color
 */
private fun floodFill(
    bitmap: Bitmap,
    x: Int,
    y: Int,
    newColor: androidx.compose.ui.graphics.Color
): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    
    // Convert Compose Color to Android Color
    val targetColorInt = android.graphics.Color.argb(
        (newColor.alpha * 255).toInt(),
        (newColor.red * 255).toInt(),
        (newColor.green * 255).toInt(),
        (newColor.blue * 255).toInt()
    )
    
    // Get the color at the tapped point
    val targetPixel = bitmap.getPixel(x.coerceIn(0, width - 1), y.coerceIn(0, height - 1))
    
    // If the color is already the same, no need to fill
    if (targetPixel == targetColorInt) {
        return bitmap
    }
    
    // Use a queue-based flood fill algorithm
    val queue = LinkedList<Pair<Int, Int>>()
    queue.add(Pair(x, y))
    
    val visited = Array(height) { BooleanArray(width) }
    val originalColor = targetPixel
    
    val paint = Paint().apply {
        color = targetColorInt
        style = Paint.Style.FILL
    }
    
    val canvas = Canvas(bitmap)
    
    while (queue.isNotEmpty()) {
        val (currentX, currentY) = queue.poll()
        
        if (currentX < 0 || currentX >= width || currentY < 0 || currentY >= height) {
            continue
        }
        
        if (visited[currentY][currentX]) {
            continue
        }
        
        val pixelColor = bitmap.getPixel(currentX, currentY)
        
        // Check if pixel matches the original color (within tolerance for anti-aliasing)
        if (!isColorSimilar(pixelColor, originalColor, tolerance = 30)) {
            continue
        }
        
        visited[currentY][currentX] = true
        
        // Fill the pixel
        canvas.drawPoint(currentX.toFloat(), currentY.toFloat(), paint)
        
        // Add neighbors to queue
        queue.add(Pair(currentX + 1, currentY))
        queue.add(Pair(currentX - 1, currentY))
        queue.add(Pair(currentX, currentY + 1))
        queue.add(Pair(currentX, currentY - 1))
    }
    
    return bitmap
}

/**
 * Check if two colors are similar (within tolerance)
 * This helps with anti-aliased edges
 */
private fun isColorSimilar(color1: Int, color2: Int, tolerance: Int): Boolean {
    val r1 = Color.red(color1)
    val g1 = Color.green(color1)
    val b1 = Color.blue(color1)
    val a1 = Color.alpha(color1)
    
    val r2 = Color.red(color2)
    val g2 = Color.green(color2)
    val b2 = Color.blue(color2)
    val a2 = Color.alpha(color2)
    
    return (kotlin.math.abs(r1 - r2) <= tolerance &&
            kotlin.math.abs(g1 - g2) <= tolerance &&
            kotlin.math.abs(b1 - b2) <= tolerance &&
            kotlin.math.abs(a1 - a2) <= tolerance)
}

