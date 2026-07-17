package com.example.realtimechatapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.realtimechatapp.data.local.entity.MemberEntity
import com.example.realtimechatapp.data.local.pojo.MemberWithDetails

@Dao
interface MemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMember(member: List<MemberEntity>)

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Transaction
    @Query("SELECT * FROM members WHERE group_id = :groupId AND user_id = :userId")
    suspend fun getMemberById(groupId: String, userId: String): MemberWithDetails?

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