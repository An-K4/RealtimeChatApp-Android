package com.example.realtimechatapp.common

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

fun Throwable.getErrorMessage(): String{
    return when(this){
        is IOException -> "Mất kết nối tới máy chủ"
        is HttpException -> {
            try {
                val errorJsonString = response()?.errorBody()?.string()

                if(!errorJsonString.isNullOrEmpty()){
                    val jsonObject = JSONObject(errorJsonString)
                    jsonObject.getString("message")
                } else {
                    "Lỗi không xác định"
                }
            } catch (e: Exception){
                "Lỗi khi phân tích phản hồi trả về từ máy chủ: " + e.getErrorMessage()
            }
        }
        else -> this.message?:"Đã có lỗi xảy ra"
    }
}