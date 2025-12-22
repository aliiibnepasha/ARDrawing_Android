package com.example.ardrawing.data.local.converter

import androidx.room.TypeConverter
import com.example.ardrawing.data.model.LessonStep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LessonStepConverter {
    private val gson = Gson()
    
    @TypeConverter
    fun fromStepsList(steps: List<LessonStep>): String {
        return gson.toJson(steps)
    }
    
    @TypeConverter
    fun toStepsList(stepsString: String): List<LessonStep> {
        val listType = object : TypeToken<List<LessonStep>>() {}.type
        return gson.fromJson(stepsString, listType)
    }
}

