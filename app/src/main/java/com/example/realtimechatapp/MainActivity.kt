package com.example.realtimechatapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.realtimechatapp.domain.repository.LanguageManager
import com.example.realtimechatapp.ui.navigation.AppNavigation
import com.example.realtimechatapp.ui.theme.RealtimeChatAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        var isLanguageLoaded = false

        lifecycleScope.launch {
            languageManager.setInitialLanguage()
            isLanguageLoaded = true
        }
        splashScreen.setKeepOnScreenCondition { !isLanguageLoaded }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            RealtimeChatAppTheme {
                AppNavigation()
            }
        }
    }
}