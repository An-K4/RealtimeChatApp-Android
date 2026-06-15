package com.example.realtimechatapp.data.repository

import android.content.Context
import android.net.Uri
import com.example.realtimechatapp.common.FileUtils
import com.example.realtimechatapp.common.ImageUtils
import com.example.realtimechatapp.common.NetworkUtils
import com.example.realtimechatapp.common.isoToLong
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.local.entity.UserEntity
import com.example.realtimechatapp.data.local.entity.toUser
import com.example.realtimechatapp.data.remote.api.UserApi
import com.example.realtimechatapp.data.remote.dto.user.ChangePasswordRequestDto
import com.example.realtimechatapp.data.remote.dto.user.UpdateProfileRequestDto
import com.example.realtimechatapp.data.remote.safeApiCall
import com.example.realtimechatapp.data.local.safeDbCall
import com.example.realtimechatapp.domain.model.SearchResult
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.CurrentUserManager
import com.example.realtimechatapp.domain.repository.NetworkChecker
import com.example.realtimechatapp.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val networkChecker: NetworkChecker,
    private val currentUserManager: CurrentUserManager,
    @ApplicationContext private val context: Context
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
            if (e is CancellationException) throw e

            Timber.e(e, "Cập nhật thông tin thất bại")
            Result.failure(e)
        }
    }

    override suspend fun updateAvatar(avatar: Uri): Result<String> {
        return try {
            val file = FileUtils.getFileFromUri(context, avatar)
            val compressFile = ImageUtils.compressImageFile(file)

            val part = NetworkUtils.createPartFromFile("avatar", compressFile)
            val updateResponse = safeApiCall(networkChecker) { userApi.updateAvatar(part) }
            val url = updateResponse.url
            val userId = currentUserManager.getCurrentUserId()
            safeDbCall { userDao.updateAvatar(url, userId) }
            Result.success(url)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

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
            if (e is CancellationException) throw e

            Timber.e(e, "Đổi mật khẩu thất bại")
            Result.failure(e)
        }
    }

    override suspend fun performSearch(query: String): Result<SearchResult> {
        return try {
            val response = safeApiCall(networkChecker) { userApi.performSearch(query) }
            val responseUsers = response.users.map { it.toUser() }
            val responseGroups = response.groups.map { it.toGroup() }

            Result.success(SearchResult(responseUsers, responseGroups))
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Tìm kiếm lỗi")
            Result.failure(e)
        }
    }

    override suspend fun performSearchUsers(query: String): Result<SearchResult> {
        return try {
            val response = safeApiCall(networkChecker) { userApi.performSearchUsers(query) }
            val responseUsers = response.users.map { it.toUser() }

            Result.success(SearchResult(responseUsers))
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Tìm kiếm lỗi")
            Result.failure(e)
        }
    }

    override suspend fun saveNewUserInfo(newUser: User): Result<Unit> {
        return try {
            val newUserEntity = UserEntity(
                id = newUser.id,
                username = newUser.username,
                fullName = newUser.fullName,
                email = newUser.email,
                avatar = newUser.avatar,
                createdAt = newUser.createdAt.isoToLong()
            )

            safeDbCall { userDao.insertUser(newUserEntity) }
            Result.success(Unit)
        } catch (e: Exception){
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi lưu thông tin người dùng mới")
            Result.failure(e)
        }
    }

    override suspend fun getLocalUser(): Result<List<User>> {
        return try {
            val localUserEntity = safeDbCall { userDao.getAllUsers() }
            val localUsers = localUserEntity.map { it.toUser() }
            Result.success(localUsers)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi lấy danh sách người dùng cục bộ")
            Result.failure(e)
        }
    }
}