package com.example.realtimechatapp

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.realtimechatapp.domain.repository.ThemeMode
import com.example.realtimechatapp.ui.navigation.AppNavigation
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Locale

@AndroidEntryPoint
class MainActivity() : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    @SuppressLint("LocalContextResourcesRead")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { mainViewModel.mainViewModelState.value.isLoading }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val mainViewModelState by mainViewModel.mainViewModelState.collectAsStateWithLifecycle()
            val configuration = Configuration(LocalConfiguration.current).apply {
                val locale = Locale(mainViewModelState.currentLanguage.code)
                Locale.setDefault(locale)
                setLocale(locale)
            }

            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            Timber.d("Ngôn ngữ hiện tại: ${mainViewModelState.currentLanguage.displayName}")

            val isDarkTheme = when (mainViewModelState.currentTheme) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            if (!mainViewModelState.isLoading) {
                CompositionLocalProvider(LocalConfiguration provides configuration) {
                    RealtimeChatAppTheme(isDarkTheme) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}