package com.example.realtimechatapp.data.local.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.realtimechatapp.domain.exception.LocalStorageException
import com.example.realtimechatapp.domain.repository.TokenManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class TokenManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
): TokenManager {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    override suspend fun saveToken(token: String){
        try {
            dataStore.edit { prefs ->
                prefs[TOKEN_KEY] = token
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi lưu token")
            throw LocalStorageException.LocalDataWriteException
        }
    }

    override val token: Flow<String?> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            Timber.e(exception, "Lỗi đọc token")
            emit(emptyPreferences())
        } else {
            Timber.e(exception, "Đã xảy ra lỗi gì đó khi đọc token")
            throw exception
        }
    }.map { prefs ->
        prefs[TOKEN_KEY]
    }

    override suspend fun deleteToken(){
        try {
            dataStore.edit { prefs ->
                prefs.remove(TOKEN_KEY)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi xóa token")
            throw LocalStorageException.LocalDataWriteException
        }
    }
}