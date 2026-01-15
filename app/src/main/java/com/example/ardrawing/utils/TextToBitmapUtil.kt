package com.example.ardrawing.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

object TextToBitmapUtil {
    /**
     * Convert text to bitmap with specified font properties
     */
    fun textToBitmap(
        text: String,
        fontSize: Float = 64f,
        fontFamily: FontFamily = FontFamily.Default,
        fontWeight: FontWeight = FontWeight.Normal,
        fontStyle: FontStyle = FontStyle.Normal,
        textColor: Color = Color.Black,
        backgroundColor: Color = Color.Transparent,
        padding: Int = 40,
        maxWidth: Int = 800
    ): Bitmap {
        // Create paint for text measurement
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = fontSize
            this.color = textColor.toArgb()
            this.textAlign = Paint.Align.LEFT
            this.typeface = getTypeface(fontFamily, fontWeight, fontStyle)
        }
        
        // Measure text dimensions
        val textLines = text.split("\n")
        var maxLineWidth = 0f
        val lineHeights = mutableListOf<Float>()
        
        textLines.forEach { line ->
            val lineWidth = paint.measureText(line)
            if (lineWidth > maxLineWidth) {
                maxLineWidth = lineWidth
            }
            val lineHeight = paint.fontMetrics.let { it.descent - it.ascent }
            lineHeights.add(lineHeight)
        }
        
        // Calculate bitmap dimensions
        val totalHeight = lineHeights.sum() + (textLines.size - 1) * fontSize * 0.2f
        val bitmapWidth = (maxLineWidth + padding * 2).toInt().coerceAtMost(maxWidth)
        val bitmapHeight = (totalHeight + padding * 2).toInt()
        
        // Create bitmap
        val bitmap = Bitmap.createBitmap(
            bitmapWidth,
            bitmapHeight,
            Bitmap.Config.ARGB_8888
        )
        
        val canvas = Canvas(bitmap)
        
        // Draw background
        if (backgroundColor != Color.Transparent) {
            canvas.drawColor(backgroundColor.toArgb())
        } else {
            canvas.drawColor(android.graphics.Color.TRANSPARENT)
        }
        
        // Draw text lines
        var yPos = padding + paint.fontMetrics.let { -it.ascent }
        textLines.forEachIndexed { index, line ->
            canvas.drawText(line, padding.toFloat(), yPos, paint)
            yPos += lineHeights[index] + fontSize * 0.2f
        }
        
        return bitmap
    }
    
    /**
     * Convert Compose FontFamily to Android Typeface
     */
    private fun getTypeface(
        fontFamily: FontFamily,
        fontWeight: FontWeight,
        fontStyle: FontStyle
    ): Typeface {
        val typefaceStyle = when {
            fontStyle == FontStyle.Italic && fontWeight >= FontWeight.Bold -> 
                android.graphics.Typeface.BOLD_ITALIC
            fontStyle == FontStyle.Italic -> 
                android.graphics.Typeface.ITALIC
            fontWeight >= FontWeight.Bold -> 
                android.graphics.Typeface.BOLD
            else -> 
                android.graphics.Typeface.NORMAL
        }
        
        return when {
            fontFamily == FontFamily.Serif -> Typeface.create(Typeface.SERIF, typefaceStyle)
            fontFamily == FontFamily.SansSerif -> Typeface.create(Typeface.SANS_SERIF, typefaceStyle)
            fontFamily == FontFamily.Monospace -> Typeface.create(Typeface.MONOSPACE, typefaceStyle)
            fontFamily == FontFamily.Cursive -> Typeface.create(Typeface.DEFAULT, typefaceStyle)
            else -> Typeface.create(Typeface.DEFAULT, typefaceStyle)
        }
    }
}
