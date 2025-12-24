package com.example.ardrawing.data.model

data class Category(
    val id: String,
    val name: String,
    val folderPath: String,
    val templates: List<DrawingTemplate>
) {
    /**
     * Formats category name for display (capitalizes first letter, replaces underscores with spaces)
     */
    val displayName: String
        get() = name.replaceFirstChar { it.uppercaseChar() }
            .replace("_", " ")
}

