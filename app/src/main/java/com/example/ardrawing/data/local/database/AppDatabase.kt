package com.example.ardrawing.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ardrawing.data.local.dao.MyAlbumDao
import com.example.ardrawing.data.local.entity.Favorite
import com.example.ardrawing.data.local.entity.MyAlbumImage
import com.example.ardrawing.data.local.entity.SavedDrawing

@Database(
    entities = [SavedDrawing::class, Favorite::class, MyAlbumImage::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun savedDrawingDao(): SavedDrawingDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun myAlbumDao(): MyAlbumDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ar_drawing_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

