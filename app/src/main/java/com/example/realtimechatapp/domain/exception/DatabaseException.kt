package com.example.realtimechatapp.domain.exception

sealed class DatabaseException : Exception() {
    object RecordNotFoundException : DatabaseException()
    object OutOfSpaceException : DatabaseException()
    object LocalDataWriteException : DatabaseException()
}