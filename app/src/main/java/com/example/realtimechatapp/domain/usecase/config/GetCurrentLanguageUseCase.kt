package com.example.realtimechatapp.domain.usecase.config

import com.example.realtimechatapp.domain.repository.AppLanguage
import com.example.realtimechatapp.domain.repository.LanguageManager
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetCurrentLanguageUseCase @Inject constructor(
    private val languageManager: LanguageManager
) {
    operator fun invoke(): StateFlow<AppLanguage> = languageManager.currentLanguage
}