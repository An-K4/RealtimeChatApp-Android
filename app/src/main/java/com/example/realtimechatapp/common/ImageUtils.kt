package com.example.realtimechatapp.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    // compress img
    fun compressImageFile(file: File) : File{
        return try {
            // get img from file
            val bitmap = BitmapFactory.decodeFile(file.path)
            val outputStream = FileOutputStream(file)

            // compress and overwrite original file
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

            outputStream.flush()
            outputStream.close()

            file
        } catch (e: Exception){
            e.printStackTrace()
            file
        }
    }
}