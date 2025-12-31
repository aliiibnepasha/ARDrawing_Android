package com.example.ardrawing.data.repository

import android.content.Context
import com.example.ardrawing.data.model.Category
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.data.utils.AssetUtils

object CategoryRepository {
    private const val HOME_FOLDER = "home"
    
    /**
     * Gets all categories dynamically from the home folder in assets
     * Each subfolder in home becomes a category
     */
    fun getCategories(context: Context): List<Category> {
        val categories = mutableListOf<Category>()
        
        try {
            // Get all folders inside home folder
            val categoryFolders = AssetUtils.listFolders(context, HOME_FOLDER)
            
            // Create a category for each folder
            categoryFolders.forEachIndexed { index, folderName ->
                val folderPath = "$HOME_FOLDER/$folderName"
                val imageFiles = AssetUtils.listImageFiles(context, folderPath)
                
                // Create templates for each image in the category
                val templates = imageFiles.mapIndexed { imgIndex, filename ->
                    DrawingTemplate(
                        id = "${folderName}_${imgIndex + 1}",
                        name = filename.substringBeforeLast("."),
                        imageAssetPath = AssetUtils.getAssetPath(folderPath, filename)
                    )
                }
                
                if (templates.isNotEmpty()) {
                    categories.add(
                        Category(
                            id = folderName,
                            name = folderName,
                            folderPath = folderPath,
                            templates = templates
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Sort categories with custom order: hot trend first, then anime, cute, anatomy, etc.
        val customOrder = listOf("hot trend", "anime", "cute", "anatomy", "cartoon", "festival", "love", "nature", "people", "simple", "architecture", "objects", "asthetic")
        return categories.sortedBy { category ->
            val index = customOrder.indexOf(category.id.lowercase())
            if (index >= 0) index else Int.MAX_VALUE
        }
    }
    
    /**
     * Gets a category by ID
     */
    fun getCategoryById(context: Context, categoryId: String): Category? {
        return getCategories(context).find { it.id == categoryId }
    }
    
    /**
     * Gets all templates from a specific category
     */
    fun getTemplatesByCategory(context: Context, categoryId: String): List<DrawingTemplate> {
        return getCategoryById(context, categoryId)?.templates ?: emptyList()
    }
}

