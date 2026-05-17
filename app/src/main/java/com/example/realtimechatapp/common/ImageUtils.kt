package com.example.realtimechatapp.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.realtimechatapp.domain.exception.FileException
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    // compress img
    fun compressImageFile(file: File): File {
        try {
            // get img from file
            val bitmap = BitmapFactory.decodeFile(file.path) ?: throw FileException.CompressFileException
            val outputStream = FileOutputStream(file)

            // compress and overwrite original file
            outputStream.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                outputStream.flush()
            }

            return file
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi nén ảnh")
            throw FileException.CompressFileException
        }
    }
}