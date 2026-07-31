package com.example.realtimechatapp.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.example.realtimechatapp.domain.exception.FileException
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    // compress img with EXIF orientation handling
    fun compressImageFile(file: File): File {
        try {
            // 1. Read EXIF orientation before decoding
            val exif = ExifInterface(file.path)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            // 2. Decode bitmap from file
            val bitmap = BitmapFactory.decodeFile(file.path) ?: throw FileException.CompressFileException

            // 3. Rotate bitmap according to EXIF orientation
            val rotatedBitmap = rotateBitmap(bitmap, orientation)

            // 4. Compress and save with correct orientation
            val outputStream = FileOutputStream(file)
            outputStream.use {
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                outputStream.flush()
            }

            // 5. Clean up original bitmap if different from rotated
            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }

            return file
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi nén ảnh")
            throw FileException.CompressFileException
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.preScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(-90f)
                matrix.preScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_NORMAL, ExifInterface.ORIENTATION_UNDEFINED -> return bitmap
        }

        return try {
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )
            rotatedBitmap
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi xoay ảnh")
            bitmap // Return original if rotation fails
        }
    }
}