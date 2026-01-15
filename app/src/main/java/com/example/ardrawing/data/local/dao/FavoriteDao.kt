package com.example.ardrawing.data.local.dao

import androidx.room.*
import com.example.ardrawing.data.local.entity.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    
    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<Favorite>>
    
    @Query("SELECT * FROM favorites WHERE prompt = :prompt LIMIT 1")
    suspend fun getFavoriteByPrompt(prompt: String): Favorite?
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE prompt = :prompt)")
    suspend fun isFavorite(prompt: String): Boolean
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite): Long
    
    @Delete
    suspend fun deleteFavorite(favorite: Favorite)
    
    @Query("DELETE FROM favorites WHERE prompt = :prompt")
    suspend fun deleteFavoriteByPrompt(prompt: String)
    
    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavorites()
}
