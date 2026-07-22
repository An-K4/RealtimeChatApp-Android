package com.example.realtimechatapp.data.local.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.realtimechatapp.domain.exception.LocalStorageException
import com.example.realtimechatapp.domain.repository.AppLanguage
import com.example.realtimechatapp.domain.repository.LanguageManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

class LanguageManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : LanguageManager {

    companion object {
        private val APP_LANGUAGE = stringPreferencesKey("selected_language")
    }

    override val currentLanguage: Flow<AppLanguage> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            Timber.e(exception, "Lỗi đọc cấu hình ngôn ngữ")
            emit(emptyPreferences())
        } else {
            Timber.e(exception, "Đã xảy ra lỗi gì đó khi đọc cấu hình ngôn ngữ")
            throw exception
        }
    }.map { preferences ->
        val languageCode = preferences[APP_LANGUAGE] ?: getDeviceLanguage().code
        AppLanguage.fromCode(languageCode)
    }

    private fun getDeviceLanguage(): AppLanguage {
        return AppLanguage.fromCode(Locale.getDefault().language)
    }

    override suspend fun setCurrentLanguage(language: AppLanguage) {
        try {
            dataStore.edit { prefs ->
                prefs[APP_LANGUAGE] = language.code
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi ghi cấu hình ngôn ngữ mới")
            throw LocalStorageException.LocalDataWriteException
        }
    }
}