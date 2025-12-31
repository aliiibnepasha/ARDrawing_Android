package com.example.ardrawing

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class CropOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 4f // Thicker border for better visibility
    }

    private val cornerPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val shadowPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        alpha = 128
    }

    private val handlePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.WHITE
        alpha = 200
    }

    // Touch handling
    enum class TouchRegion {
        NONE, CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
        TOP_EDGE, BOTTOM_EDGE, LEFT_EDGE, RIGHT_EDGE
    }

    private var currentTouchRegion = TouchRegion.NONE
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val touchThreshold = 50f // pixels

    // Crop rectangle managed by this view
    var cropRect = RectF()
        set(value) {
            field = value
            invalidate()
        }

    fun updateCropRectangle(newRect: RectF) {
        cropRect = newRect
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val rect = cropRect
        if (rect.isEmpty) return

        val width = rect.width()
        val height = rect.height()

        // Draw semi-transparent overlay outside crop area
        val outerRect = RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat())
        val cropPath = Path().apply {
            addRect(rect, Path.Direction.CW)
        }
        val outerPath = Path().apply {
            addRect(outerRect, Path.Direction.CW)
            op(cropPath, Path.Op.DIFFERENCE)
        }
        canvas.drawPath(outerPath, shadowPaint)

        // Draw crop rectangle border
        canvas.drawRect(rect, paint)

        // Draw corner resize handles (larger for better UX)
        val cornerSize = 36f
        val handleSize = 24f

        // Corner handles
        drawHandle(canvas, rect.left, rect.top, cornerSize) // Top-left
        drawHandle(canvas, rect.right, rect.top, cornerSize) // Top-right
        drawHandle(canvas, rect.left, rect.bottom, cornerSize) // Bottom-left
        drawHandle(canvas, rect.right, rect.bottom, cornerSize) // Bottom-right

        // Edge handles (smaller)
        val midY = rect.centerY()
        val midX = rect.centerX()

        drawHandle(canvas, rect.left, midY, handleSize) // Left edge
        drawHandle(canvas, rect.right, midY, handleSize) // Right edge
        drawHandle(canvas, midX, rect.top, handleSize) // Top edge
        drawHandle(canvas, midX, rect.bottom, handleSize) // Bottom edge

        // Draw grid lines inside crop rectangle (more subtle)
        paint.strokeWidth = 1f
        paint.alpha = 120 // More subtle grid

        // Vertical lines
        for (i in 1..2) {
            val x = rect.left + (width * i / 3)
            canvas.drawLine(x, rect.top, x, rect.bottom, paint)
        }

        // Horizontal lines
        for (i in 1..2) {
            val y = rect.top + (height * i / 3)
            canvas.drawLine(rect.left, y, rect.right, y, paint)
        }

        // Reset paint
        paint.strokeWidth = 4f
        paint.alpha = 255
    }

    private fun drawHandle(canvas: Canvas, centerX: Float, centerY: Float, size: Float) {
        val halfSize = size / 2
        canvas.drawRect(centerX - halfSize, centerY - halfSize,
                       centerX + halfSize, centerY + halfSize, handlePaint)
        // Draw border
        canvas.drawRect(centerX - halfSize, centerY - halfSize,
                       centerX + halfSize, centerY + halfSize, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                currentTouchRegion = getTouchRegion(event.x, event.y)
                return currentTouchRegion != TouchRegion.NONE
            }
            MotionEvent.ACTION_MOVE -> {
                if (currentTouchRegion != TouchRegion.NONE) {
                    handleResize(event.x, event.y)
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                currentTouchRegion = TouchRegion.NONE
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getTouchRegion(x: Float, y: Float): TouchRegion {
        val rect = cropRect
        val threshold = touchThreshold

        // Check corners first
        if (abs(x - rect.left) < threshold && abs(y - rect.top) < threshold) {
            return TouchRegion.TOP_LEFT
        }
        if (abs(x - rect.right) < threshold && abs(y - rect.top) < threshold) {
            return TouchRegion.TOP_RIGHT
        }
        if (abs(x - rect.left) < threshold && abs(y - rect.bottom) < threshold) {
            return TouchRegion.BOTTOM_LEFT
        }
        if (abs(x - rect.right) < threshold && abs(y - rect.bottom) < threshold) {
            return TouchRegion.BOTTOM_RIGHT
        }

        // Check edges
        if (abs(x - rect.left) < threshold && y > rect.top && y < rect.bottom) {
            return TouchRegion.LEFT_EDGE
        }
        if (abs(x - rect.right) < threshold && y > rect.top && y < rect.bottom) {
            return TouchRegion.RIGHT_EDGE
        }
        if (abs(y - rect.top) < threshold && x > rect.left && x < rect.right) {
            return TouchRegion.TOP_EDGE
        }
        if (abs(y - rect.bottom) < threshold && x > rect.left && x < rect.right) {
            return TouchRegion.BOTTOM_EDGE
        }

        // Check center
        if (x > rect.left && x < rect.right && y > rect.top && y < rect.bottom) {
            return TouchRegion.CENTER
        }

        return TouchRegion.NONE
    }

    private fun handleResize(x: Float, y: Float) {
        val rect = RectF(cropRect)
        val dx = x - lastTouchX
        val dy = y - lastTouchY

        when (currentTouchRegion) {
            TouchRegion.TOP_LEFT -> {
                rect.left += dx
                rect.top += dy
            }
            TouchRegion.TOP_RIGHT -> {
                rect.right += dx
                rect.top += dy
            }
            TouchRegion.BOTTOM_LEFT -> {
                rect.left += dx
                rect.bottom += dy
            }
            TouchRegion.BOTTOM_RIGHT -> {
                rect.right += dx
                rect.bottom += dy
            }
            TouchRegion.LEFT_EDGE -> {
                rect.left += dx
            }
            TouchRegion.RIGHT_EDGE -> {
                rect.right += dx
            }
            TouchRegion.TOP_EDGE -> {
                rect.top += dy
            }
            TouchRegion.BOTTOM_EDGE -> {
                rect.bottom += dy
            }
            TouchRegion.CENTER -> {
                rect.offset(dx, dy)
            }
            else -> return
        }

        // Update crop rect
        cropRect = rect

        // Apply constraints from activity (square crop, image bounds)
        val activity = context as? CropActivity
        activity?.cropRect = rect
        activity?.constrainCropRect()

        lastTouchX = x
        lastTouchY = y
    }
}
