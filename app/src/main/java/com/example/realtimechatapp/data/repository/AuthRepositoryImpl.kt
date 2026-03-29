package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.common.NetworkUtils
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.local.database.LocalDatabase
import com.example.realtimechatapp.data.local.entity.toUser
import com.example.realtimechatapp.data.local.manager.TokenManager
import com.example.realtimechatapp.data.remote.AuthApi
import com.example.realtimechatapp.data.remote.dto.LoginRequestDto
import com.example.realtimechatapp.data.remote.dto.SignupRequestDto
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.AuthRepository
import com.example.realtimechatapp.domain.repository.CurrentUserManager
import com.example.realtimechatapp.domain.repository.NetworkChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val tokenManager: TokenManager,
    private val currentUserManager: CurrentUserManager,
    private val networkChecker: NetworkChecker,
    private val localDatabase: LocalDatabase
) : AuthRepository {
    override suspend fun login(
        username: String,
        password: String
    ): Result<User> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (networkChecker.isNetworkAvailable()) {
                val response = authApi.login(LoginRequestDto(username, password))

                // clear all old data
                localDatabase.clearAllTables()

                val user = response.user.toUser()
                tokenManager.saveToken(response.token)
                currentUserManager.switchUser(response.user.id)
                userDao.insertUser(response.user.toUserEntity())
                Result.success(user)
            } else {
                Result.failure(Exception("Mất kết nối tới máy chủ"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun uploadAvatar(file: File): Result<String?> {
        return try {
            if (networkChecker.isNetworkAvailable()){
                val part = NetworkUtils.createPartFromFile("avatar", file)
                val uploadResult = authApi.uploadAvatar(part)
                val url = uploadResult.url
                Timber.d("Upload thành công, url là %s", url)
                Result.success(url)
            } else {
                Result.failure(Exception("Mất kết nối tới máy chủ"))
            }
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
            val response = authApi.signup(
                SignupRequestDto(username, password, fullName, email, avatar)
            )
            Result.success(response.message)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            try {
                if (networkChecker.isNetworkAvailable()) {
                    authApi.logout()
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            tokenManager.deleteToken()
            currentUserManager.switchUser("")
            localDatabase.clearAllTables()
            Result.success("Đăng xuất thành công!")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getMe(): Result<User> {
        return try {
            if (networkChecker.isNetworkAvailable()) {
                val response = authApi.getMe()
                currentUserManager.switchUser(response.user.id)
                userDao.insertUser(response.user.toUserEntity())
                Result.success(response.user.toUser())
            } else {
                if (currentUserManager.getCurrentUserId().isEmpty()) {
                    Result.failure(Exception("Mất kết nối tới máy chủ"))
                } else {
                    val cachedUser = userDao.getUserById(currentUserManager.getCurrentUserId())
                    if (cachedUser != null) {
                        Result.success(cachedUser.toUser())
                    } else {
                        Result.failure(Exception("Không tìm thấy dữ liệu người dùng"))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

            val currentId = currentUserManager.getCurrentUserId()
            val localUser = if (currentId.isNotEmpty()) userDao.getUserById(currentId) else null
            if (localUser != null) {
                Result.success(localUser.toUser())
            } else {
                Result.failure(Exception(e))
            }
        }
    }
}