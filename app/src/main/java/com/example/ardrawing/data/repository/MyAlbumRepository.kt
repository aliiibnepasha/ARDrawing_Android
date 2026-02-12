package com.example.ardrawing.data.repository

import com.example.ardrawing.data.local.dao.MyAlbumDao
import com.example.ardrawing.data.local.entity.MyAlbumImage
import kotlinx.coroutines.flow.Flow

class MyAlbumRepository(
    private val myAlbumDao: MyAlbumDao
) {
    fun getAllImages(): Flow<List<MyAlbumImage>> = myAlbumDao.getAllImages()

    suspend fun addImage(uriString: String) {
        val image = MyAlbumImage(uri = uriString)
        myAlbumDao.insertImage(image)
    }

    suspend fun deleteImage(image: MyAlbumImage) {
        myAlbumDao.deleteImage(image)
    }

    suspend fun deleteAllImages() {
        myAlbumDao.deleteAllImages()
    }
}

