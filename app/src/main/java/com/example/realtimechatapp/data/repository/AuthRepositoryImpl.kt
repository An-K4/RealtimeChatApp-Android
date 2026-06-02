package com.example.realtimechatapp.data.repository

import android.content.Context
import android.net.Uri
import com.example.realtimechatapp.common.FileUtils
import com.example.realtimechatapp.common.ImageUtils
import com.example.realtimechatapp.common.NetworkUtils
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.local.database.LocalDatabase
import com.example.realtimechatapp.data.local.entity.toUser
import com.example.realtimechatapp.data.local.manager.TokenManagerImpl
import com.example.realtimechatapp.data.remote.api.AuthApi
import com.example.realtimechatapp.data.remote.dto.auth.LoginRequestDto
import com.example.realtimechatapp.data.remote.dto.auth.SignupRequestDto
import com.example.realtimechatapp.data.remote.safeApiCall
import com.example.realtimechatapp.data.local.safeDbCall
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.AuthRepository
import com.example.realtimechatapp.domain.repository.CurrentUserManager
import com.example.realtimechatapp.domain.repository.NetworkChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val tokenManager: TokenManagerImpl,
    private val currentUserManager: CurrentUserManager,
    private val networkChecker: NetworkChecker,
    private val localDatabase: LocalDatabase,
    @ApplicationContext private val context: Context
) : AuthRepository {
    override suspend fun login(
        username: String,
        password: String
    ): Result<User> {
        return try {
            val response = safeApiCall(networkChecker) {
                authApi.login(LoginRequestDto(username, password))
            }

            // clear all old data
            // clearAllTables() is not a suspend function, so it will block the current thread
            // It needs a separate withContext(Dispatchers.IO) instead of being wrapped in safeDbCall
            safeDbCall { localDatabase.clearAllTables() }
            Timber.d("Đã xóa toàn bộ dữ liệu")

            val user = response.user.toUser()
            tokenManager.saveToken(response.token)
            currentUserManager.switchUser(response.user.id)
            Timber.e("Chuẩn bị chèn người dùng vào db")
            safeDbCall { userDao.insertUser(response.user.toUserEntity()) }
            Result.success(user)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Đăng nhập lỗi")
            Result.failure(e)
        }
    }

    override suspend fun uploadAvatar(file: Uri): Result<String?> {
        return try {
            val file = FileUtils.getFileFromUri(context, file)
            val compressedAvatar = ImageUtils.compressImageFile(file)

            val part = NetworkUtils.createPartFromFile("avatar", compressedAvatar)
            val uploadResult = safeApiCall(networkChecker) {
                authApi.uploadAvatar(part)
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

    override suspend fun signup(
        username: String,
        password: String,
        fullName: String,
        email: String,
        avatar: String?
    ): Result<Unit> {
        return try {
            safeApiCall(networkChecker) {
                authApi.signup(SignupRequestDto(username, password, fullName, email, avatar))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Đăng ký lỗi")
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            safeApiCall(networkChecker) { authApi.logout() }

            tokenManager.deleteToken()
            currentUserManager.switchUser("")
            safeDbCall { localDatabase.clearAllTables() }
            Timber.d("Đã xóa toàn bộ dữ liệu")
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Đăng xuất lỗi")
            Result.failure(e)
        }
    }

    override suspend fun getMe(): Result<User> {
        val currentUserId = currentUserManager.getCurrentUserId()
        val cachedUser =
            if (currentUserId.isNotEmpty()) userDao.getUserById(currentUserId) else null

        return try {
            val response = safeApiCall(networkChecker) {
                authApi.getMe()
            }
            val userResponse = response.user

            currentUserManager.switchUser(userResponse.id)
            safeDbCall { userDao.insertUser(userResponse.toUserEntity()) }

            val me = safeDbCall { userDao.getUserById(userResponse.id) }
            if (me != null) {
                Result.success(me.toUser())
            } else {
                Timber.d("Lỗi khi truy xuất db")
                Result.success(userResponse.toUser())
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lấy thông tin cá nhân lỗi")

            if (cachedUser != null) {
                Timber.d("Lấy thông tin cá nhân cũ")
                Result.success(cachedUser.toUser())
            } else {
                Result.failure(e)
            }
        }
    }
}