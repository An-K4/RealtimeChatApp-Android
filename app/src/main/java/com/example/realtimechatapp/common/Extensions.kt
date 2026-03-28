package com.example.realtimechatapp.common

import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

fun String?.isoToLong(): Long{
    if (this.isNullOrBlank()) return System.currentTimeMillis()
    return try {
        Instant.parse(this).toEpochMilli()
    } catch (e: Exception){
        Timber.e(e, "Failed to convert to Long: %s", this)
        System.currentTimeMillis()
    }
}

fun Long.formatToTime(toHourMinute: Boolean): String{
    return try {
        val instant = Instant.ofEpochMilli(this)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())

        val pattern = if(toHourMinute) "HH:mm" else "dd/MM/yyyy"
        val formatter = DateTimeFormatter.ofPattern(pattern)

        return zonedDateTime.format(formatter)
    } catch (e: Exception){
        Timber.e(e, "Failed to parse timestamp: %s", this)
        "" // return null
    }
}

fun String?.formatToTime(toHourMinute: Boolean): String {
    if (this.isNullOrBlank()) return ""

    return this.isoToLong().formatToTime(toHourMinute)
}