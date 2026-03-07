package com.example.realtimechatapp.data.adapter

import com.example.realtimechatapp.data.remote.dto.UserDto
import com.example.realtimechatapp.domain.model.User
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class UserAdapter: JsonDeserializer<UserDto?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): UserDto? {
        return when {
            json!!.isJsonPrimitive -> UserDto(id = json.asString, username = "", fullName = "", email = "", avatar = "", createdAt = "")
            json.isJsonObject -> {
                // use deserialize causes infinity recursive!

                val obj = json.asJsonObject
                UserDto(
                    id = obj.get("_id")?.asString ?: obj.get("id")?.asString ?: "",
                    fullName = obj.get("fullName")?.asString ?: "",
                    avatar = obj.get("avatar")?.asString ?: "",
                    username = obj.get("username")?.asString ?: "",
                    email = obj.get("email")?.asString ?: "",
                    createdAt = obj.get("createdAt")?.asString ?: ""
                )
            }
            else -> null
        }
    }
}