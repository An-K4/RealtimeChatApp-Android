package com.example.realtimechatapp.data.local

import android.database.sqlite.SQLiteDiskIOException
import com.example.realtimechatapp.domain.exception.DatabaseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

suspend fun <T> safeDbCall(dbCall: suspend () -> T): T {
    return withContext(Dispatchers.IO) {
        try {
            dbCall()
        } catch (e: Exception) {
            Timber.e(e, "Chi tiết lỗi lưu db")

            throw when (e) {
                is CancellationException -> throw e
                is SQLiteDiskIOException -> DatabaseException.OutOfSpaceException // out of memory
                else -> DatabaseException.LocalDataWriteException
            }
        }
    }
}