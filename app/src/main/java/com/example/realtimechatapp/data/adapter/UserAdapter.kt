package com.example.realtimechatapp.data.adapter

import com.example.realtimechatapp.domain.model.User
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class UserAdapter: JsonDeserializer<User?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): User? {
        return when {
            json!!.isJsonPrimitive -> User(id = json.asString, fullName = "", avatar = "", username = "", email = "", createdAt = "")
            json.isJsonObject -> context?.deserialize(json, User::class.java)
            else -> null
        }
    }

}