package com.example.realtimechatapp.ui.screens.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.domain.repository.AppLanguage
import com.example.realtimechatapp.domain.usecase.config.GetCurrentLanguageUseCase
import com.example.realtimechatapp.domain.usecase.config.SetLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val getCurrentLanguageUseCase: GetCurrentLanguageUseCase,
    private val setLanguageUseCase: SetLanguageUseCase
): ViewModel() {
    data class MoreScreenState(
        val supportedLanguages: List<AppLanguage> = AppLanguage.entries,
        val isDarkMode: Boolean = false,
        val isLoading: Boolean = false,
    )

    val currentLanguage: StateFlow<AppLanguage> = getCurrentLanguageUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = getCurrentLanguageUseCase().value
    )

    private val _moreScreenState = MutableStateFlow(MoreScreenState())
    val moreScreenState = _moreScreenState.asStateFlow()

    fun changeLanguage(language: AppLanguage){
        Timber.tag("LanguageDebug").d("1. ViewModel nhận lệnh đổi sang: ${language.displayName} (Code: ${language.code})")
        setLanguageUseCase(language)
    }
}