package com.example.realtimechatapp.domain.exception

sealed class GroupException : Exception() {
    object GroupIdNotExistException : GroupException()
    object GroupNameBlankException : GroupException()
    object GroupMemberSizeException : GroupException()
}