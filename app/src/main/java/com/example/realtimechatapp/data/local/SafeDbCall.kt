package com.example.realtimechatapp.data.local

import android.database.sqlite.SQLiteDiskIOException
import com.example.realtimechatapp.domain.exception.DatabaseException
import kotlinx.coroutines.CancellationException

suspend fun <T> safeDbCall(dbCall: suspend () -> T): T{
    return try {
        dbCall()
    } catch (e: Exception){
        throw when(e){
            is CancellationException -> throw e
            is SQLiteDiskIOException -> DatabaseException.OutOfSpaceException // out of memory
            else -> DatabaseException.LocalDataWriteException
        }
    }
}