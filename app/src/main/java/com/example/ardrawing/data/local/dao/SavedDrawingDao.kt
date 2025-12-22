package com.example.ardrawing.data.local.dao

import androidx.room.*
import com.example.ardrawing.data.local.entity.SavedDrawing
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedDrawingDao {
    
    @Query("SELECT * FROM saved_drawings ORDER BY createdAt DESC")
    fun getAllDrawings(): Flow<List<SavedDrawing>>
    
    @Query("SELECT * FROM saved_drawings WHERE isSaved = 1 ORDER BY createdAt DESC")
    fun getSavedDrawings(): Flow<List<SavedDrawing>>
    
    @Query("SELECT * FROM saved_drawings WHERE id = :id")
    suspend fun getDrawingById(id: Long): SavedDrawing?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrawing(drawing: SavedDrawing): Long
    
    @Update
    suspend fun updateDrawing(drawing: SavedDrawing)
    
    @Delete
    suspend fun deleteDrawing(drawing: SavedDrawing)
    
    @Query("DELETE FROM saved_drawings WHERE id = :id")
    suspend fun deleteDrawingById(id: Long)
    
    @Query("UPDATE saved_drawings SET isSaved = :isSaved WHERE id = :id")
    suspend fun updateSavedStatus(id: Long, isSaved: Boolean)
}

