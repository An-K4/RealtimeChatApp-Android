package com.example.realtimechatapp.domain.repository

import android.net.Uri

interface MediaRepository {
    suspend fun upload(file: Uri): Result<String>
    suspend fun publicUpload(file: Uri): Result<String>
}