package com.example.ardrawing.data.repository

import android.content.Context
import com.example.ardrawing.data.model.DrawingTemplate
import com.example.ardrawing.data.utils.AssetUtils

object TemplateRepository {
    private const val ASSETS_FOLDER = "bg_remove"
    
    /**
     * Gets all templates dynamically from the bg_remove folder in assets
     * Automatically loads all images found in the folder
     */
    fun getTemplates(context: Context): List<DrawingTemplate> {
        val imageFiles = AssetUtils.listImageFiles(context, ASSETS_FOLDER)
        
        // Loop through all images and create templates
        return imageFiles.mapIndexed { index, filename ->
            DrawingTemplate(
                id = (index + 1).toString(),
                name = filename.substringBeforeLast(".").replaceFirstChar { 
                    it.uppercaseChar() 
                },
                imageAssetPath = AssetUtils.getAssetPath(ASSETS_FOLDER, filename)
            )
        }
    }

    /**
     * Gets a template by ID
     * First checks bg_remove folder, then checks all categories
     */
    fun getTemplateById(context: Context, id: String): DrawingTemplate? {
        // First check bg_remove folder
        val templateFromBgRemove = getTemplates(context).find { it.id == id }
        if (templateFromBgRemove != null) {
            return templateFromBgRemove
        }
        
        // If not found, check categories (template IDs from categories are like "categoryName_index")
        // Try to find in all categories
        val categories = CategoryRepository.getCategories(context)
        for (category in categories) {
            val template = category.templates.find { it.id == id }
            if (template != null) {
                return template
            }
        }
        
        return null
    }
}

