package com.example.realtimechatapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class RealtimeChatApp : Application(){
    override fun onCreate() {
        super.onCreate()

        // setup timber
        Timber.plant(Timber.DebugTree())
    }
}