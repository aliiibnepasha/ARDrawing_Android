package com.example.ardrawing.data.repository

import android.content.Context

/**
 * Simple local storage for user's uploaded album images.
 * Stores a set of URI strings in SharedPreferences.
 */
object MyAlbumRepository {

    private const val PREFS_NAME = "my_album_prefs"
    private const val KEY_URIS = "album_uris"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getImages(context: Context): List<String> {
        val set = prefs(context).getStringSet(KEY_URIS, emptySet()) ?: emptySet()
        // Keep order stable by sorting by string (simple, deterministic)
        return set.toList().sorted()
    }

    fun addImage(context: Context, uriString: String) {
        val prefs = prefs(context)
        val current = prefs.getStringSet(KEY_URIS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(uriString)
        prefs.edit().putStringSet(KEY_URIS, current).apply()
    }
}

