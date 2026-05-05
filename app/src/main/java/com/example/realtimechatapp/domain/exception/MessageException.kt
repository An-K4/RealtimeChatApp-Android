package com.example.realtimechatapp.domain.exception

sealed class MessageException: Exception() {
    object ContactIdNotExistException: MessageException()
}