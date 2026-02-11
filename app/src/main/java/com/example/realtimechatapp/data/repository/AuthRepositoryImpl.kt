package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.common.NetworkUtils
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.data.remote.AuthApi
import com.example.realtimechatapp.data.remote.dto.LoginRequestDto
import com.example.realtimechatapp.data.remote.dto.SignupRequestDto
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.AuthRepository
import okhttp3.MultipartBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val api: AuthApi) : AuthRepository {
    override suspend fun login(
        username: String,
        password: String
    ): Result<User> {
        return try {
            val response = api.login(LoginRequestDto(username, password))
            val user = response.user.toUser()
            Result.success(user)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun uploadAvatar(file: File): Result<String?> {
        return try {
            val part = NetworkUtils.createPartFromFile("avatar", file)
            val uploadResult = api.uploadAvatar(part)
            val url = uploadResult.url
            Timber.d("Upload thành công, url là %s", url)
            Result.success(url)
        } catch (e: Exception) {
            Timber.d("Upload lỗi: %s", e.getErrorMessage())
            Result.failure(e)
        }
    }

    override suspend fun signup(
        username: String,
        password: String,
        fullName: String,
        email: String,
        avatar: String?
    ): Result<String> {
        return try {
            val response = api.signup(SignupRequestDto(username, password, fullName, email, avatar))
            Result.success(response.message)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun logout(token: String): Result<String> {
        return try {
            val response = api.logout(token)
            Result.success(response.message)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}