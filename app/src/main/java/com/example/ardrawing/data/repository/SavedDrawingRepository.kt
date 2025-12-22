package com.example.ardrawing.data.repository

import com.example.ardrawing.data.local.dao.SavedDrawingDao
import com.example.ardrawing.data.local.entity.SavedDrawing
import kotlinx.coroutines.flow.Flow

class SavedDrawingRepository(
    private val savedDrawingDao: SavedDrawingDao
) {
    
    fun getAllDrawings(): Flow<List<SavedDrawing>> = savedDrawingDao.getAllDrawings()
    
    fun getSavedDrawings(): Flow<List<SavedDrawing>> = savedDrawingDao.getSavedDrawings()
    
    suspend fun getDrawingById(id: Long): SavedDrawing? = savedDrawingDao.getDrawingById(id)
    
    suspend fun insertDrawing(drawing: SavedDrawing): Long = savedDrawingDao.insertDrawing(drawing)
    
    suspend fun updateDrawing(drawing: SavedDrawing) = savedDrawingDao.updateDrawing(drawing)
    
    suspend fun deleteDrawing(drawing: SavedDrawing) = savedDrawingDao.deleteDrawing(drawing)
    
    suspend fun deleteDrawingById(id: Long) = savedDrawingDao.deleteDrawingById(id)
    
    suspend fun updateSavedStatus(id: Long, isSaved: Boolean) = 
        savedDrawingDao.updateSavedStatus(id, isSaved)
}

