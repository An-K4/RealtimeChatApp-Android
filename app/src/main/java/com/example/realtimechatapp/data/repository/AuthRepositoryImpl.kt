package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.local.database.LocalDatabase
import com.example.realtimechatapp.data.local.entity.toUser
import com.example.realtimechatapp.data.local.manager.TokenManagerImpl
import com.example.realtimechatapp.data.remote.api.AuthApi
import com.example.realtimechatapp.data.remote.dto.auth.LoginRequestDto
import com.example.realtimechatapp.data.remote.dto.auth.LogoutRequestDto
import com.example.realtimechatapp.data.remote.dto.auth.SignupRequestDto
import com.example.realtimechatapp.data.remote.safeApiCall
import com.example.realtimechatapp.data.local.safeDbCall
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.ActiveConversationManager
import com.example.realtimechatapp.domain.repository.AuthRepository
import com.example.realtimechatapp.domain.repository.CurrentUserManager
import com.example.realtimechatapp.domain.repository.NetworkChecker
import kotlinx.coroutines.flow.first
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
    private val activeConversationManager: ActiveConversationManager,
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
            tokenManager.saveToken(response.accessToken)
            tokenManager.saveRefreshToken(response.refreshToken)
            currentUserManager.switchUser(response.user.id)
            Timber.d("Chuẩn bị chèn người dùng vào db")
            safeDbCall { userDao.insertUser(response.user.toUserEntity()) }
            Result.success(user)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Đăng nhập lỗi")
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
            // Lấy refresh token từ DataStore
            val refreshToken = tokenManager.refreshToken.first()
            
            // Nếu có refresh token, gọi backend để blacklist
            if (!refreshToken.isNullOrEmpty()) {
                safeApiCall(networkChecker) { 
                    authApi.logout(LogoutRequestDto(refreshToken)) 
                }
                Timber.d("Backend logout thành công")
            } else {
                // Silent: Không có refresh token, chỉ cleanup local
                Timber.w("Logout: Refresh token not found, cleanup local only")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            
            // Dù backend lỗi, vẫn coi như logout thành công
            Timber.e(e, "Logout API failed, but will cleanup local data")
            Result.success(Unit)
        } finally {
            // LUÔN LUÔN cleanup local data, dù có lỗi hay không
            try {
                tokenManager.deleteToken()
                tokenManager.deleteRefreshToken()
                currentUserManager.switchUser("")
                activeConversationManager.clearActiveConversation()
                safeDbCall { localDatabase.clearAllTables() }
                Timber.d("Đã xóa toàn bộ dữ liệu local")
            } catch (cleanupError: Exception) {
                Timber.e(cleanupError, "Cleanup local data failed")
            }
        }
    }

    override suspend fun getMe(): Result<User> {
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

            val currentUserId = currentUserManager.getCurrentUserId()
            val cachedUser =
                if (currentUserId != null) userDao.getUserById(currentUserId) else null
            if (cachedUser != null) {
                Timber.d("Lấy thông tin cá nhân cũ")
                Result.success(cachedUser.toUser())
            } else {
                Result.failure(e)
            }
        }
    }
}