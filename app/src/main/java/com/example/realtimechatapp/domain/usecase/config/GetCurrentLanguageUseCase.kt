package com.example.realtimechatapp.domain.usecase.config

import com.example.realtimechatapp.domain.repository.AppLanguage
import com.example.realtimechatapp.domain.repository.LanguageManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentLanguageUseCase @Inject constructor(
    private val languageManager: LanguageManager
) {
    operator fun invoke(): Flow<AppLanguage> = languageManager.currentLanguage
}