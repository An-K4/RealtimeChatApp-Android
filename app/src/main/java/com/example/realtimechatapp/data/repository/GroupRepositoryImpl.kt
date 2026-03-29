package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.data.local.dao.GroupContactDao
import com.example.realtimechatapp.data.local.dao.GroupDao
import com.example.realtimechatapp.data.local.dao.GroupMessageDao
import com.example.realtimechatapp.data.local.dao.MemberDao
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.local.entity.toGroupContact
import com.example.realtimechatapp.data.local.pojo.toGroup
import com.example.realtimechatapp.data.local.pojo.toMessage
import com.example.realtimechatapp.data.remote.GroupApi
import com.example.realtimechatapp.domain.model.Group
import com.example.realtimechatapp.domain.model.GroupContact
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.repository.NetworkChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupApi: GroupApi,
    private val groupContactDao: GroupContactDao,
    private val groupMessageDao: GroupMessageDao,
    private val groupDao: GroupDao,
    private val memberDao: MemberDao,
    private val userDao: UserDao,
    private val networkChecker: NetworkChecker
): GroupRepository{
    override suspend fun getGroups(): Result<List<GroupContact>> = withContext(Dispatchers.IO) {
        val cachedGroups = groupContactDao.getGroupContact()

        return@withContext try {
            if (networkChecker.isNetworkAvailable()){
                val response = groupApi.getGroups()
                val responseGroups = response.groups.map { it.toContactEntity() }
                groupContactDao.insertAllContact(responseGroups)
                val groups = groupContactDao.getGroupContact().map { it.toGroupContact() }
                Timber.d(responseGroups.toString())
                Timber.d(groups.toString())
                Result.success(groups)
            } else {
                Timber.d("Mất kết nối, lấy trong cache")
                Result.success(cachedGroups.map { it.toGroupContact() })
            }
        } catch (e: Exception){
            if (cachedGroups.isNotEmpty()){
                Timber.d(e.getErrorMessage())
                Result.success(cachedGroups.map { it.toGroupContact() })
            } else {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    override suspend fun getGroupMessage(groupId: String): Result<List<Message>> = withContext(
        Dispatchers.IO
    ) {
        val cachedGroupMessages = groupMessageDao.getGroupMessages(groupId, 30, 0).map { it.toMessage() }

        return@withContext try {
            if (networkChecker.isNetworkAvailable()){
                val result = groupApi.getGroupMessage(groupId)
                val responseGroupMessages = result.groupMessages
                val responseSender = result.groupMessages.map { it.senderId.toUserEntity() }

                userDao.insertAllUser(responseSender)
                groupMessageDao.insertAllMessages(responseGroupMessages.map { it.toMessageEntity() })
                val groupMessages = groupMessageDao.getGroupMessages(groupId, 30, 0).map { it.toMessage() }
                Result.success(groupMessages)
            } else {
                Timber.d("Mất kết nối, lấy trong cache")
                Result.success(cachedGroupMessages)
            }
        } catch (e: Exception){
            if (cachedGroupMessages.isNotEmpty()){
                Timber.d("Lỗi khi gọi api, lấy trong cache")
                Result.success(cachedGroupMessages)
            } else {
                Timber.d(e.getErrorMessage())
                Result.failure(e)
            }
        }
    }

    override suspend fun getGroupInfo(groupId: String): Result<Group> = withContext(
        Dispatchers.IO
    ) {
        val cachedGroupInfo = groupDao.getGroupById(groupId)?.toGroup()

        return@withContext try {
            if (networkChecker.isNetworkAvailable()){
                // call api
                val result = groupApi.getGroupInfo(groupId)
                val responseGroup = result.group.toGroupEntity()
                val responseMembers = result.group.members.map { it.toMemberEntity(groupId) }

                // save to db
                groupDao.insertGroup(responseGroup)
                memberDao.insertAllMember(responseMembers)

                val groupInfo = groupDao.getGroupById(groupId)?.toGroup()

                if (groupInfo != null){
                    Timber.d(groupInfo.toString())
                    Result.success(groupInfo)
                } else {
                    Timber.d("Lỗi khi lưu db")
                    Result.failure(Exception("Lỗi tải thông tin nhóm"))
                }
            } else {
                if (cachedGroupInfo != null){
                    Timber.d("Không có kết nối mạng, lấy dữ liệu trong cache")
                    Result.success(cachedGroupInfo)
                } else {
                    Timber.d("Không có kết nối mạng")
                    Result.failure(Exception("Mất kết nối tới máy chủ"))
                }
            }
        } catch (e: Exception){
            if (cachedGroupInfo != null){
                Timber.d("Có lỗi khi kết nối máy chủ, lấy dữ liệu trong cache")
                Result.success(cachedGroupInfo)
            } else {
                Timber.d(e.getErrorMessage())
                Result.failure(Exception("Không tìm thấy nhóm"))
            }
        }
    }
}
