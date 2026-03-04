package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.common.NetworkUtils
import com.example.realtimechatapp.data.remote.UserApi
import com.example.realtimechatapp.data.remote.dto.ChangePasswordRequestDto
import com.example.realtimechatapp.data.remote.dto.UpdateProfileRequestDto
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.UserRepository
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi
): UserRepository {
    override suspend fun updateProfile(
        fullName: String,
        email: String
    ): Result<User> {
        return try {
            val response = userApi.updateProfile(UpdateProfileRequestDto(fullName, email))
            Timber.log(1, response.toString())
            Result.success(response.user.toUser())
        } catch (e: Exception){
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateAvatar(file: File): Result<String> {
        return try {
            val part = NetworkUtils.createPartFromFile("avatar", file)
            val updateResponse = userApi.updateAvatar(part)
            val url = updateResponse.url
            Result.success(url)
        } catch (e: Exception){
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): Result<String> {
        return try {
            val response = userApi.changePassword(ChangePasswordRequestDto(oldPassword, newPassword))
            Result.success(response.message)
        } catch (e: Exception){
            e.printStackTrace()
            Result.failure(e)
        }
    }
}