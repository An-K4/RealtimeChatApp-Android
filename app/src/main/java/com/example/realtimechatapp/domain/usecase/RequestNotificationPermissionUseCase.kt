package com.example.realtimechatapp.domain.usecase

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class RequestNotificationPermissionUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(activity: ComponentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            
            when {
                ContextCompat.checkSelfPermission(context, permission) 
                    == PackageManager.PERMISSION_GRANTED -> {
                    // Already granted
                    Timber.d("Notification permission already granted")
                }
                else -> {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(permission),
                        REQUEST_CODE_NOTIFICATION_PERMISSION
                    )
                }
            }
        }
    }
    
    companion object {
        const val REQUEST_CODE_NOTIFICATION_PERMISSION = 1001
    }
}
