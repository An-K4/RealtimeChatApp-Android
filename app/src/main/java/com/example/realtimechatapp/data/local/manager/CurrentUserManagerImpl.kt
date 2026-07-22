package com.example.realtimechatapp.data.local.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.realtimechatapp.domain.exception.LocalStorageException
import com.example.realtimechatapp.domain.repository.CurrentUserManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class CurrentUserManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : CurrentUserManager {
    companion object {
        private val CURRENT_USER_ID = stringPreferencesKey("current_user_id")
    }

    override suspend fun getCurrentUserId(): String? {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                Timber.e(exception, "Lỗi đọc ID người dùng từ bộ nhớ")
                emit(emptyPreferences())
            } else {
                Timber.e(exception, "Lỗi lấy id người dùng hiện tại")
                throw exception
            }
        }.map { preferences ->
            preferences[CURRENT_USER_ID]
        }.first()
    }

    override suspend fun switchUser(userId: String) {
        try {
            dataStore.edit { preferences ->
                preferences[CURRENT_USER_ID] = userId
            }
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception

            Timber.e(exception, "Lỗi ghi id người dùng hiện tại")
            throw LocalStorageException.LocalDataWriteException
        }
    }

    override fun observeCurrentUser(): Flow<String?> {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                throw LocalStorageException.LocalDataReadException
            } else {
                Timber.e(exception, "Lỗi luồng đọc id người dùng hiện tại")
                throw exception
            }
        }.map { preferences ->
            preferences[CURRENT_USER_ID]
        }
    }
}