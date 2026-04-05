package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.common.NetworkUtils
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.local.database.LocalDatabase
import com.example.realtimechatapp.data.local.entity.toUser
import com.example.realtimechatapp.data.local.manager.TokenManager
import com.example.realtimechatapp.data.remote.api.AuthApi
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
                Timber.d("Đã xóa toàn bộ dữ liệu")

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

    override suspend fun uploadAvatar(file: File): Result<String?> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (networkChecker.isNetworkAvailable()) {
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
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
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
            Timber.d("Đã xóa toàn bộ dữ liệu")
            Result.success("Đăng xuất thành công!")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getMe(): Result<User> = withContext(Dispatchers.IO) {
        val currentUserId = currentUserManager.getCurrentUserId()
        val cachedUser =
            if (currentUserId.isNotEmpty()) userDao.getUserById(currentUserId) else null

        return@withContext try {
            if (networkChecker.isNetworkAvailable()) {
                val response = authApi.getMe()
                val userResponse = response.user

                currentUserManager.switchUser(userResponse.id)
                userDao.insertUser(userResponse.toUserEntity())

                val me = userDao.getUserById(userResponse.id)
                if (me != null) {
                    Result.success(me.toUser())
                } else {
                    Timber.d("Lỗi khi truy xuất db")
                    Result.success(userResponse.toUser())
                }
            } else {
                if (cachedUser != null) {
                    Result.success(cachedUser.toUser())
                } else {
                    Result.failure(Exception("Mất kết nối tới máy chủ"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

            if (cachedUser != null) {
                Timber.d("Lỗi trong quá trình gọi api, lấy dữ liệu cũ")
                Result.success(cachedUser.toUser())
            } else {
                Result.failure(e)
            }
        }
    }
}