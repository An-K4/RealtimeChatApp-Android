package com.example.realtimechatapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.realtimechatapp.data.local.entity.GroupEntity
import com.example.realtimechatapp.data.local.pojo.GroupWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Query("UPDATE `groups` SET name = :groupName, avatar = :groupAvatar, description = :groupDescription WHERE id = :groupId")
    suspend fun updateGroupInfo(groupId: String, groupName: String, groupAvatar: String?, groupDescription: String?)

    @Query("UPDATE `groups` SET owner_id = :newOwnerId WHERE id = :groupId")
    suspend fun updateGroupOwner(groupId: String, newOwnerId: String)

    @Query("SELECT * FROM `groups` WHERE id = :groupId")
    fun observeGroupById(groupId: String): Flow<GroupWithDetails?>

    @Query("SELECT * FROM `groups`")
    fun getLocalGroups(): List<GroupWithDetails>

    @Transaction
    @Query("SELECT * FROM `groups` WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): GroupWithDetails?

    @Query("SELECT owner_id FROM `groups` WHERE id = :groupId")
    suspend fun getOwnerIdOfGroup(groupId: String): String?

    @Query("DELETE FROM `groups` WHERE id = :groupId")
    suspend fun deleteGroup(groupId: String)
}