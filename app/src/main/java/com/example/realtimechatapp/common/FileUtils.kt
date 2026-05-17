package com.example.realtimechatapp.common

import android.content.Context
import android.net.Uri
import com.example.realtimechatapp.domain.exception.FileException
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun getFileFromUri(context: Context, uri: Uri): File {
        try {
            // val contentResolver = context.contentResolver

            val fileName = "temp_upload_${System.currentTimeMillis()}"
            val tempFile = File(context.cacheDir, fileName)

            // avoid memory leak: A resource failed to call close.
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw FileException.FileNotFoundException

            return tempFile
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi tải file từ uri")
            throw FileException.FileNotFoundException
        }
    }
}