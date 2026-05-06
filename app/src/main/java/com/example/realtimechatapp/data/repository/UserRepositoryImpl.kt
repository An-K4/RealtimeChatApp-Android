package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.common.NetworkUtils
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.remote.api.UserApi
import com.example.realtimechatapp.data.remote.dto.ChangePasswordRequestDto
import com.example.realtimechatapp.data.remote.dto.UpdateProfileRequestDto
import com.example.realtimechatapp.data.remote.safeApiCall
import com.example.realtimechatapp.data.local.safeDbCall
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.CurrentUserManager
import com.example.realtimechatapp.domain.repository.NetworkChecker
import com.example.realtimechatapp.domain.repository.UserRepository
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val networkChecker: NetworkChecker,
    private val currentUserManager: CurrentUserManager
) : UserRepository {
    override suspend fun updateProfile(
        fullName: String,
        email: String
    ): Result<User> {
        return try {
            val response = safeApiCall(networkChecker) {
                userApi.updateProfile(UpdateProfileRequestDto(fullName, email))
            }
            Timber.d("Cập nhật thông tin thành công: %s", response.user.fullName)
            safeDbCall { userDao.updateUser(response.user.toUserEntity()) }
            Result.success(response.user.toUser())
        } catch (e: Exception) {
            Timber.e(e, "Cập nhật thông tin thất bại")
            Result.failure(e)
        }
    }

    override suspend fun updateAvatar(file: File): Result<String> {
        return try {
            val part = NetworkUtils.createPartFromFile("avatar", file)
            val updateResponse = safeApiCall(networkChecker) { userApi.updateAvatar(part) }
            val url = updateResponse.url
            val userId = currentUserManager.getCurrentUserId()
            safeDbCall { userDao.updateAvatar(url, userId) }
            Result.success(url)
        } catch (e: Exception) {
            Timber.e(e, "Cập nhật thông avatar thất bại")
            Result.failure(e)
        }
    }

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            safeApiCall(networkChecker) {
                userApi.changePassword(ChangePasswordRequestDto(oldPassword, newPassword))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Đổi mật khẩu thất bại")
            Result.failure(e)
        }
    }
}