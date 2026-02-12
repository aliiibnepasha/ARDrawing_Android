package com.example.ardrawing.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ardrawing.data.local.entity.MyAlbumImage
import kotlinx.coroutines.flow.Flow

@Dao
interface MyAlbumDao {
    @Query("SELECT * FROM my_album_images ORDER BY timestamp DESC")
    fun getAllImages(): Flow<List<MyAlbumImage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: MyAlbumImage)

    @Delete
    suspend fun deleteImage(image: MyAlbumImage)

    @Query("DELETE FROM my_album_images")
    suspend fun deleteAllImages()
}
