package com.example.realtimechatapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.realtimechatapp.data.local.entity.MemberEntity
import com.example.realtimechatapp.data.local.pojo.MemberWithDetails

@Dao
interface MemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMember(member: List<MemberEntity>)

    @Transaction
    @Query("SELECT * FROM members WHERE group_id = :groupId")
    suspend fun getGroupMembers(groupId: String): List<MemberWithDetails>

    @Query("DELETE FROM members WHERE group_id = :groupId")
    suspend fun deleteGroupMembers(groupId: String)

    @Transaction
    suspend fun syncGroupMembers(groupId: String, newMembers: List<MemberEntity>) {
        deleteGroupMembers(groupId)
        insertAllMember(newMembers)
    }
}