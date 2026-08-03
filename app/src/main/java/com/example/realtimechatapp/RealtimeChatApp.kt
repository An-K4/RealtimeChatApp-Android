package com.example.realtimechatapp

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.realtimechatapp.domain.repository.ActiveConversationManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class RealtimeChatApp : Application(), ImageLoaderFactory {

    @Inject
    lateinit var imageLoader: ImageLoader
    
    @Inject
    lateinit var activeConversationManager: ActiveConversationManager
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // setup timber
        Timber.plant(Timber.DebugTree())
        
        // Clear active conversation on fresh app start to prevent stale state
        applicationScope.launch {
            activeConversationManager.clearActiveConversation()
            Timber.d("Cleared active conversation on app start")
        }
    }

    override fun newImageLoader(): ImageLoader {
        return imageLoader
    }
}