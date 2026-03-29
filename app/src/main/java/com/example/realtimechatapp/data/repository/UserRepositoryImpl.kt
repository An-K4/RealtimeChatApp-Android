package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.common.NetworkUtils
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.remote.UserApi
import com.example.realtimechatapp.data.remote.dto.ChangePasswordRequestDto
import com.example.realtimechatapp.data.remote.dto.UpdateProfileRequestDto
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.CurrentUserManager
import com.example.realtimechatapp.domain.repository.NetworkChecker
import com.example.realtimechatapp.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val networkChecker: NetworkChecker,
    private val currentUserManager: CurrentUserManager
): UserRepository {
    override suspend fun updateProfile(
        fullName: String,
        email: String
    ): Result<User> {
        return try {
            if (networkChecker.isNetworkAvailable()){
                val response = userApi.updateProfile(UpdateProfileRequestDto(fullName, email))
                Timber.d("Update Profile Success: %s", response.user.fullName)
                userDao.updateUser(response.user.toUserEntity())
                Result.success(response.user.toUser())
            } else {
                Result.failure(Exception("Mất kết nối tới máy chủ"))
            }
        } catch (e: Exception){
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateAvatar(file: File): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (networkChecker.isNetworkAvailable()){
                val part = NetworkUtils.createPartFromFile("avatar", file)
                val updateResponse = userApi.updateAvatar(part)
                val url = updateResponse.url
                val userId = currentUserManager.getCurrentUserId()
                userDao.updateAvatar(url, userId)
                Result.success(url)
            } else {
                Result.failure(Exception("Mất kết nối tới máy chủ"))
            }
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
            if (networkChecker.isNetworkAvailable()){
                val response = userApi.changePassword(ChangePasswordRequestDto(oldPassword, newPassword))
                Result.success(response.message)
            } else {
                Result.failure(Exception("Mất kết nối tới máy chủ"))
            }
        } catch (e: Exception){
            e.printStackTrace()
            Result.failure(e)
        }
    }
}