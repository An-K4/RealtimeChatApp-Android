package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.data.local.dao.GroupContactDao
import com.example.realtimechatapp.data.local.entity.toGroupContact
import com.example.realtimechatapp.data.remote.GroupApi
import com.example.realtimechatapp.domain.model.GroupContact
import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.repository.NetworkChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupApi: GroupApi,
    private val groupContactDao: GroupContactDao,
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
}
