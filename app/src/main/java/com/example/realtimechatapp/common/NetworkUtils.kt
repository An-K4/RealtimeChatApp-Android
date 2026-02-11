package com.example.realtimechatapp.common

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object NetworkUtils {

    fun createPartFromFile(partName: String, file: File): MultipartBody.Part{
        // tu dong nhan dien la image
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestBody)
    }
}