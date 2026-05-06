package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.data.local.dao.GroupContactDao
import com.example.realtimechatapp.data.local.dao.GroupDao
import com.example.realtimechatapp.data.local.dao.GroupMessageDao
import com.example.realtimechatapp.data.local.dao.MemberDao
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.local.entity.toGroupContact
import com.example.realtimechatapp.data.local.pojo.toGroup
import com.example.realtimechatapp.data.local.pojo.toMessage
import com.example.realtimechatapp.data.remote.api.GroupApi
import com.example.realtimechatapp.data.remote.safeApiCall
import com.example.realtimechatapp.data.local.safeDbCall
import com.example.realtimechatapp.domain.exception.DatabaseException
import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.model.GroupContact
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.repository.NetworkChecker
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class GroupRepositoryImpl @Inject constructor(
    private val groupApi: GroupApi,
    private val groupContactDao: GroupContactDao,
    private val groupMessageDao: GroupMessageDao,
    private val groupDao: GroupDao,
    private val memberDao: MemberDao,
    private val userDao: UserDao,
    private val networkChecker: NetworkChecker
) : GroupRepository {
    override suspend fun getGroups(): Result<List<GroupContact>> {
        val cachedGroups = safeDbCall { groupContactDao.getGroupContact() }

        return try {
            val response = safeApiCall(networkChecker) { groupApi.getGroups() }
            val responseGroups = response.groups.map { it.toContactEntity() }
            safeDbCall { groupContactDao.insertAllContact(responseGroups) }
            val groups =
                safeDbCall { groupContactDao.getGroupContact().map { it.toGroupContact() } }
            Timber.d(responseGroups.toString())
            Timber.d(groups.toString())
            Result.success(groups)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi lấy danh sách nhóm")

            if (cachedGroups.isNotEmpty()) {
                Timber.d("Lỗi, lấy danh sách nhóm trong cache")
                Result.success(cachedGroups.map { it.toGroupContact() })
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getGroupMessage(groupId: String): Result<List<Message>> {
        val cachedGroupMessages = safeDbCall {
            groupMessageDao.getGroupMessages(groupId, 30, 0).map { it.toMessage() }
        }

        return try {
            val result = safeApiCall(networkChecker) { groupApi.getGroupMessage(groupId) }
            val responseGroupMessages = result.groupMessages
            val responseSender =
                result.groupMessages.map { it.senderId.toUserEntity() }.distinctBy { it.id }

            safeDbCall {
                // debug - before
                Timber.d("${userDao.getUserCount()}")
                Timber.d(responseSender.toString())
                Timber.d("${responseSender.size}")

                safeDbCall { userDao.insertAllUsers(responseSender) }

                // debug - after
                Timber.d("${userDao.getUserCount()}")
                Timber.d(responseSender.toString())
                Timber.d("${responseSender.size}")

                safeDbCall {
                    groupMessageDao.insertAllMessages(responseGroupMessages.map { it.toMessageEntity() })
                }
            }
            val groupMessages =
                safeDbCall {
                    groupMessageDao.getGroupMessages(groupId, 30, 0).map { it.toMessage() }
                }
            Result.success(groupMessages)
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi lấy tin nhắn nhóm")

            if (cachedGroupMessages.isNotEmpty()) {
                Timber.d("Lỗi, lấy danh sách tin nhắn nhóm trong cache")
                Result.success(cachedGroupMessages)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getGroupInfo(groupId: String): Result<Group> {
        val cachedGroupInfo = safeDbCall { groupDao.getGroupById(groupId)?.toGroup() }

        return try {
            // call api
            val result = safeApiCall(networkChecker) {
                groupApi.getGroupInfo(groupId)
            }
            val responseGroup = result.group.toGroupEntity()
            val responseMembers = result.group.members.map { it.toMemberEntity(groupId) }

            // save to db
            safeDbCall {
                groupDao.insertGroup(responseGroup)
                memberDao.insertAllMember(responseMembers)
            }

            val groupInfo = safeDbCall { groupDao.getGroupById(groupId)?.toGroup() }

            if (groupInfo != null) {
                Timber.d(groupInfo.toString())
                Result.success(groupInfo)
            } else {
                Timber.d("Lỗi khi lưu db")
                Result.failure(DatabaseException.RecordNotFoundException)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            Timber.e(e, "Lỗi lấy thông tin nhóm")

            if (cachedGroupInfo != null) {
                Timber.d("Có lỗi khi kết nối máy chủ, lấy dữ liệu trong cache")
                Result.success(cachedGroupInfo)
            } else {
                Result.failure(e)
            }
        }
    }
}
