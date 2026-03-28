package com.example.realtimechatapp.data.local.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.example.realtimechatapp.data.local.entity.ParticipantRole
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

@ProvidedTypeConverter
class Converters @Inject constructor(private val gson: Gson){
    @TypeConverter
    fun fromStringList(list: List<String>?): String?{
        if (list == null) return "[]"
        return gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(data: String?): List<String>?{
        if (data == null || data == "[]") return emptyList()
        val type = object : TypeToken<List<String>>(){}.type
        return gson.fromJson(data, type) ?: emptyList()
    }

    @TypeConverter
    fun fromParticipantRole(role: ParticipantRole): String = role.name

    @TypeConverter
    fun toParticipantRole(role: String): ParticipantRole = ParticipantRole.valueOf(role)
}