package com.example.realtimechatapp.data.local.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.realtimechatapp.domain.exception.LocalStorageException
import com.example.realtimechatapp.domain.repository.ThemeManager
import com.example.realtimechatapp.domain.repository.ThemeMode
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class ThemeManagerImpl @Inject constructor(private val dataStore: DataStore<Preferences>
): ThemeManager {
    override suspend fun setThemeMode(themeMode: ThemeMode) {
        try {
            dataStore.edit { preferences ->
                preferences[THEME_MODE] = themeMode.name
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi ghi cấu hình giao diện mới")
            throw LocalStorageException.LocalDataWriteException
        }
    }

    override val themeMode = dataStore.data.catch { exception ->
        if (exception is IOException) {
            Timber.e(exception, "Lỗi đọc cấu hình giao diện")
            emit(emptyPreferences())
        } else {
            Timber.e(exception, "Đã xảy ra lỗi gì đó khi đọc cấu hình giao diện")
            throw exception
        }
    }.map { preferences ->
        val themeModeString = preferences[THEME_MODE] ?: ThemeMode.LIGHT.name
        ThemeMode.entries.find { it.name == themeModeString } ?: ThemeMode.LIGHT
    }

    companion object{
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}