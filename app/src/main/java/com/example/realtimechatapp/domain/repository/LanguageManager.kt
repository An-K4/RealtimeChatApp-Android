package com.example.realtimechatapp.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface LanguageManager {
    val currentLanguage: StateFlow<AppLanguage>
    fun setCurrentLanguage(language: AppLanguage)
    suspend fun setInitialLanguage()
}

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    VIETNAMESE("vi", "Tiếng Việt");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            // fallback matches with default strings.xml
            return entries.find { it.code == code } ?: ENGLISH
        }
    }
}