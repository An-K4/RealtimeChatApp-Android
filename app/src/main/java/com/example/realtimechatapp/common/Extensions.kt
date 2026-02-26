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

fun String?.formatToTime(toHourMinute: Boolean): String {
    if (this.isNullOrBlank()) return ""

    return try {
        // parse string ISO 8601 (E.g: "2024-05-21T10:15:30.000Z") to Instant
        val instant = Instant.parse(this)

        // convert Instant to device timezone
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())

        if (toHourMinute){
            // format to "HH:mm"
            zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } else {
            // format to "dd/MM/yyyy"
            zonedDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
    } catch (e: Exception) {
        // log
        Timber.e(e, "Failed to parse timestamp: %s", this)
        "" // return null
    }
}