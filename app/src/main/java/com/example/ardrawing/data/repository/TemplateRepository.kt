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
     */
    fun getTemplateById(context: Context, id: String): DrawingTemplate? {
        return getTemplates(context).find { it.id == id }
    }
}

