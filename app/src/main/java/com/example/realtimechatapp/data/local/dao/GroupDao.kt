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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGroup(groups: List<GroupEntity>)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Transaction
    @Query("SELECT * FROM `groups` WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): GroupWithDetails?

    @Query("SELECT owner_id FROM `groups` WHERE id = :groupId")
    suspend fun getOwnerIdOfGroup(groupId: String): String?

    @Transaction
    @Query("""
        SELECT * FROM `groups`
        WHERE id IN (
            SELECT group_id FROM members WHERE user_id = :userId
        )
        ORDER BY updated_at DESC
    """)
    fun observeUserGroups(userId: String): Flow<List<GroupWithDetails>>
}