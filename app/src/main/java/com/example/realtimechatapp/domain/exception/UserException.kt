package com.example.realtimechatapp.domain.exception

sealed class UserException : Exception() {
    object FileNotFoundException : UserException()
    object CompressAvatarException : UserException()
}