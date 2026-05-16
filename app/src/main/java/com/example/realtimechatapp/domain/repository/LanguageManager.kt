package com.example.realtimechatapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface LanguageManager {
    val currentLanguage: Flow<AppLanguage>
    suspend fun setCurrentLanguage(language: AppLanguage)
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