package com.example.realtimechatapp.data.local.manager

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.realtimechatapp.di.ApplicationScope
import com.example.realtimechatapp.domain.repository.AppLanguage
import com.example.realtimechatapp.domain.repository.LanguageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

class LanguageManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationScope private val applicationScope: CoroutineScope
) : LanguageManager {

    companion object {
        private val APP_LANGUAGE = stringPreferencesKey("selected_language")
    }

    private val _currentLanguage = MutableStateFlow(getDeviceLanguage())
    override val currentLanguage: StateFlow<AppLanguage> = _currentLanguage

    private fun getDeviceLanguage(): AppLanguage {
        return AppLanguage.fromCode(Locale.getDefault().language)
    }

    private suspend fun readFromDataStore(): AppLanguage {
        return dataStore.data.first().let { AppLanguage.fromCode(it[APP_LANGUAGE]) }
    }

    override fun setCurrentLanguage(language: AppLanguage) {
        Timber.tag("LanguageDebug").d("2. Manager chuẩn bị set code: ${language.code}")

        val localeList = LocaleListCompat.forLanguageTags(language.code)
        Timber.tag("LanguageDebug").d("3. LocaleList tạo ra: ${localeList.toLanguageTags()}")

        AppCompatDelegate.setApplicationLocales(localeList)
        _currentLanguage.value = language

        applicationScope.launch {
            dataStore.edit { prefs ->
                prefs[APP_LANGUAGE] = language.code
            }
        }
    }

    override suspend fun setInitialLanguage() {
        val initialLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locales = AppCompatDelegate.getApplicationLocales()
            if (locales.isEmpty) readFromDataStore()
            else AppLanguage.fromCode(locales[0]?.language)
        } else {
            readFromDataStore()
        }
        _currentLanguage.value = initialLanguage

        val localeList = LocaleListCompat.forLanguageTags(initialLanguage.code)
        if (AppCompatDelegate.getApplicationLocales() != localeList){
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }
}