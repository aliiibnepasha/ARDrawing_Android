package com.example.ardrawing.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ardrawing.data.local.entity.SavedDrawing
import com.example.ardrawing.data.repository.SavedDrawingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MyCreativeUiState(
    val allDrawings: List<SavedDrawing> = emptyList(),
    val savedDrawings: List<SavedDrawing> = emptyList(),
    val selectedTab: TabType = TabType.ALL_MEDIA,
    val isLoading: Boolean = false,
    val error: String? = null,
    val uploadedImages: List<String> = emptyList() // List of image URIs
)

enum class TabType {
    ALL_MEDIA,
    SAVED
}

class MyCreativeViewModel(
    private val repository: SavedDrawingRepository,
    private val albumRepository: MyAlbumRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MyCreativeUiState())
    val uiState: StateFlow<MyCreativeUiState> = _uiState.asStateFlow()
    
    init {
        loadDrawings()
        loadAlbumImages()
    }
    
    private fun loadDrawings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                combine(
                    repository.getAllDrawings(),
                    repository.getSavedDrawings()
                ) { allDrawings, savedDrawings ->
                    Pair(allDrawings, savedDrawings)
                }.collect { (allDrawings, savedDrawings) ->
                    _uiState.value = _uiState.value.copy(
                        allDrawings = allDrawings,
                        savedDrawings = savedDrawings,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun loadAlbumImages() {
        viewModelScope.launch {
            albumRepository.getAllImages().collect { images ->
                _uiState.value = _uiState.value.copy(
                    uploadedImages = images.map { it.uri }
                )
            }
        }
    }
    
    fun selectTab(tabType: TabType) {
        _uiState.value = _uiState.value.copy(selectedTab = tabType)
    }
    
    fun deleteDrawing(drawing: SavedDrawing) {
        viewModelScope.launch {
            try {
                repository.deleteDrawing(drawing)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun toggleSavedStatus(drawing: SavedDrawing) {
        viewModelScope.launch {
            try {
                repository.updateSavedStatus(drawing.id, !drawing.isSaved)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun addUploadedImage(imageUri: String) {
        viewModelScope.launch {
            try {
                albumRepository.addImage(imageUri)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun getUploadedImages(): List<String> {
        return _uiState.value.uploadedImages
    }
    
    companion object {
        fun provideFactory(
            repository: SavedDrawingRepository,
            albumRepository: MyAlbumRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MyCreativeViewModel(repository, albumRepository) as T
                }
            }
        }
    }
}

