package com.shipthis.go.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonElement

/**
 * Type converters for Room database
 * Handles conversion of complex types that Room doesn't support natively
 */
class Converters {
    
    private val gson = Gson()
    
    /**
     * Converts the details: Any? field to a JSON string for storage
     */
    @TypeConverter
    fun fromDetails(details: Any?): String? {
        if (details == null) return null
        return try {
            gson.toJson(details)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Converts JSON string back to Any? for the details field
     */
    @TypeConverter
    fun toDetails(json: String?): Any? {
        if (json == null) return null
        return try {
            gson.fromJson(json, JsonElement::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

