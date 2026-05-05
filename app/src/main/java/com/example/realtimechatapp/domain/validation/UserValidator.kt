package com.example.realtimechatapp.domain.validation

import com.example.realtimechatapp.domain.exception.UserException
import java.io.File

object UserValidator {
    fun validateFile(file: File?) {
        if (file == null) {
            throw UserException.FileNotFoundException
        }
    }

    fun validateCompressedAvatar(compressedAvatar: File?) {
        if (compressedAvatar == null){
            throw UserException.CompressAvatarException
        }
    }
}