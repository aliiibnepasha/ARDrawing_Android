package com.example.ardrawing.utils

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SharedPreferencesUtil(val context: Context) {

    inline fun <reified T> saveToPref(key: String, value: T?) {
        CoroutineScope(Dispatchers.IO).launch {
            context.getSharedPreferences("app_data", Context.MODE_PRIVATE).edit {
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}")
                }
            }
        }
    }

    inline fun <reified T> getFromPref(key: String, default: T): T {
        context.getSharedPreferences("app_data", Context.MODE_PRIVATE).apply {
            return when (default) {
                is String -> getString(key, default) as T
                is Int -> getInt(key, default) as T
                is Long -> getLong(key, default) as T
                is Float -> getFloat(key, default) as T
                is Boolean -> getBoolean(key, default) as T
                else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}")
            }
        }
    }

    /**
     * Clear all preferences
     */
    fun clearAll() {
        CoroutineScope(Dispatchers.IO).launch {
            context.getSharedPreferences("app_data", Context.MODE_PRIVATE).edit {
                clear()
            }
        }
    }

    /**
     * Remove a specific preference by key
     */
    fun remove(key: String) {
        CoroutineScope(Dispatchers.IO).launch {
            context.getSharedPreferences("app_data", Context.MODE_PRIVATE).edit {
                remove(key)
            }
        }
    }

    /**
     * Check if a preference exists
     */
    fun contains(key: String): Boolean {
        return context.getSharedPreferences("app_data", Context.MODE_PRIVATE).contains(key)
    }
}
