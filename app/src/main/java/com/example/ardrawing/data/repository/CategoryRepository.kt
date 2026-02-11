package com.example.ardrawing.data.repository

import android.content.Context
import com.example.ardrawing.data.model.Category
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.data.utils.AssetUtils

object CategoryRepository {
    private const val HOME_FOLDER = "home"
    private const val CATEGORIES_FOLDER = "categories"
    
    /**
     * Gets all categories dynamically from the home and categories folders in assets
     */
    fun getCategories(context: Context): List<Category> {
        val allCategories = mutableListOf<Category>()
        
        // Scan both folders
        allCategories.addAll(scanFolder(context, HOME_FOLDER))
        allCategories.addAll(scanFolder(context, CATEGORIES_FOLDER))
        
        // Remove duplicates if any (prioritizing the first occurrence, which is HOME_FOLDER)
        val distinctCategories = allCategories.distinctBy { it.id }

        // Sort categories with custom order: hot trend first, then anime, cute, anatomy, etc.
        val customOrder = listOf("hot trend", "anime", "cute", "anatomy", "cartoon", "festival", "love", "nature", "people", "simple", "architecture", "objects", "asthetic")
        return distinctCategories.sortedBy { category ->
            val index = customOrder.indexOf(category.id.lowercase())
            if (index >= 0) index else Int.MAX_VALUE
        }
    }

    private fun scanFolder(context: Context, rootFolder: String): List<Category> {
        val categories = mutableListOf<Category>()
        try {
            // Get all folders inside root folder
            val categoryFolders = AssetUtils.listFolders(context, rootFolder)
            
            // Create a category for each folder
            categoryFolders.forEach { folderName ->
                val folderPath = "$rootFolder/$folderName"
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
        return categories
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

