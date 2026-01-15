package com.example.ardrawing.data.repository

import com.example.ardrawing.data.local.dao.FavoriteDao
import com.example.ardrawing.data.local.entity.Favorite
import kotlinx.coroutines.flow.Flow

class FavoriteRepository(
    private val favoriteDao: FavoriteDao
) {
    
    fun getAllFavorites(): Flow<List<Favorite>> = favoriteDao.getAllFavorites()
    
    suspend fun getFavoriteByPrompt(prompt: String): Favorite? = favoriteDao.getFavoriteByPrompt(prompt)
    
    suspend fun isFavorite(prompt: String): Boolean = favoriteDao.isFavorite(prompt)
    
    suspend fun insertFavorite(favorite: Favorite): Long = favoriteDao.insertFavorite(favorite)
    
    suspend fun deleteFavorite(favorite: Favorite) = favoriteDao.deleteFavorite(favorite)
    
    suspend fun deleteFavoriteByPrompt(prompt: String) = favoriteDao.deleteFavoriteByPrompt(prompt)
    
    suspend fun toggleFavorite(prompt: String): Boolean {
        val isCurrentlyFavorite = isFavorite(prompt)
        return if (isCurrentlyFavorite) {
            deleteFavoriteByPrompt(prompt)
            false
        } else {
            insertFavorite(Favorite(prompt = prompt))
            true
        }
    }
}
