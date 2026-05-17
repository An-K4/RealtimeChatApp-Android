package com.example.realtimechatapp.domain.exception

sealed class FileException: Exception() {
    object FileNotFoundException : FileException()
    object CompressFileException : FileException()
}