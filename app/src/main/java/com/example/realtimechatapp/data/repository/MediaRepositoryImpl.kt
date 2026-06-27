package com.example.realtimechatapp.data.repository

import android.content.Context
import android.net.Uri
import com.example.realtimechatapp.common.FileUtils
import com.example.realtimechatapp.common.ImageUtils
import com.example.realtimechatapp.common.NetworkUtils
import com.example.realtimechatapp.data.remote.api.MediaApi
import com.example.realtimechatapp.data.remote.safeApiCall
import com.example.realtimechatapp.domain.repository.MediaRepository
import com.example.realtimechatapp.domain.repository.NetworkChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class MediaRepositoryImpl @Inject constructor(
    private val mediaApi: MediaApi,
    private val networkChecker: NetworkChecker,
    @ApplicationContext private val context: Context
) : MediaRepository {
    override suspend fun upload(file: Uri): Result<String> =
        performUpload(file = file, isPublic = false)

    override suspend fun publicUpload(file: Uri): Result<String> =
        performUpload(file = file, isPublic = true)

    private suspend fun performUpload(file: Uri, isPublic: Boolean): Result<String> {
        return try {
            val file = FileUtils.getFileFromUri(context, file)
            val compressedAvatar = ImageUtils.compressImageFile(file)

            val part = NetworkUtils.createPartFromFile("file", compressedAvatar)
            val uploadResult = safeApiCall(networkChecker) {
                if (isPublic) mediaApi.publicUpload(part) else mediaApi.upload(part)
            }
            val url = uploadResult.url
            Timber.d("Tải lên thành công, url là %s", url)
            Result.success(url)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Tải lên lỗi")
            Result.failure(e)
        }
    }
}