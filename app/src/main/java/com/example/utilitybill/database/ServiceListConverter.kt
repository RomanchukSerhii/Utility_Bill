package com.example.utilitybill.database

import androidx.room.TypeConverter
import com.example.utilitybill.database.model.Service
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class ServiceListConverter {
    @TypeConverter
    fun fromString(value: String?): List<Service>? {
        val listType: Type = object : TypeToken<List<Service?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(services: List<Service?>?): String? {
        return Gson().toJson(services)
    }
}