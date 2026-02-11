package com.example.realtimechatapp.common

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun getFileFromUri(context: Context, uri: Uri) : File?{
        return try {
            // val contentResolver = context.contentResolver

            val fileName = "temp_upload_${System.currentTimeMillis()}"
            val tempFile = File(context.cacheDir, fileName)

            // avoid memory leak: A resource failed to call close.
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream -> inputStream.copyTo(outputStream)
                }
            }

//            val inputStream: InputStream? = contentResolver.openInputStream(uri)
//            val outputStream = FileOutputStream(tempFile)
//
//            inputStream?.copyTo(outputStream)
//
//            inputStream?.close()
//            outputStream.close()

            tempFile
        } catch (e: Exception){
            e.printStackTrace()
            null
        }
    }
}