package com.example.ardrawing.ui.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ARViewModel : ViewModel() {
    private var _anchorBitmap = mutableStateOf<Bitmap?>(null)
    val anchorBitmap: Bitmap? get() = _anchorBitmap.value
    
    fun setAnchorBitmap(bitmap: Bitmap) {
        _anchorBitmap.value = bitmap
    }
    
    fun clearAnchorBitmap() {
        _anchorBitmap.value = null
    }
}

