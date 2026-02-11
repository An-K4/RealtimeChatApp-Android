package com.example.realtimechatapp.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    // nen anh
    fun compressImageFile(file: File) : File{
        return try {
            // doc anh tu file
            val bitmap = BitmapFactory.decodeFile(file.path)
            val outputStream = FileOutputStream(file)

            // nen anh va ghi de chinh file goc
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