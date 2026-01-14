package com.example.ardrawing.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class to manage ARCore compatibility state in SharedPreferences
 * Checks once at app startup and caches the result
 */
object ARCorePreferences {
    private const val PREFS_NAME = "ardrawing_prefs"
    private const val KEY_AR_CORE_SUPPORTED = "ar_core_supported"
    private const val KEY_AR_CORE_CHECKED = "ar_core_checked"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Check if ARCore compatibility has been checked before
     */
    fun hasCheckedARCore(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_AR_CORE_CHECKED, false)
    }

    /**
     * Get cached ARCore support status
     * Returns null if not checked yet
     */
    fun isARCoreSupported(context: Context): Boolean? {
        val prefs = getSharedPreferences(context)
        return if (prefs.getBoolean(KEY_AR_CORE_CHECKED, false)) {
            prefs.getBoolean(KEY_AR_CORE_SUPPORTED, false)
        } else {
            null
        }
    }

    /**
     * Save ARCore compatibility status
     */
    fun setARCoreSupported(context: Context, isSupported: Boolean) {
        getSharedPreferences(context).edit()
            .putBoolean(KEY_AR_CORE_SUPPORTED, isSupported)
            .putBoolean(KEY_AR_CORE_CHECKED, true)
            .apply()
    }

    /**
     * Clear the cached ARCore status (force re-check on next launch)
     */
    fun clearARCoreStatus(context: Context) {
        getSharedPreferences(context).edit()
            .remove(KEY_AR_CORE_SUPPORTED)
            .remove(KEY_AR_CORE_CHECKED)
            .apply()
    }
}
