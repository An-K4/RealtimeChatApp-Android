package com.example.realtimechatapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.realtimechatapp.data.local.entity.ParticipantEntity

@Dao
interface ParticipantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: ParticipantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllParticipant(participants: List<ParticipantEntity>)

    @Update
    suspend fun updateParticipant(participant: ParticipantEntity)

    @Query("SELECT * FROM participants WHERE group_id = :groupId AND user_id = :userId")
    suspend fun getParticipant(groupId: String, userId: String): ParticipantEntity?

    @Query("SELECT * FROM participants WHERE group_id = :groupId")
    suspend fun getGroupParticipant(groupId: String): List<ParticipantEntity>

    @Query("SELECT * FROM participants WHERE user_id = :userId")
    suspend fun getUserParticipant(userId: String): List<ParticipantEntity>

    @Query(
        """
            UPDATE participants
            SET last_read_timestamp = :timestamp
            WHERE group_id = :groupId AND user_id = :userId
        """
    )
    suspend fun updateLastRead(groupId: String, userId: String, timestamp: Long)

    @Query("DELETE FROM participants WHERE group_id = :groupId AND user_id = :userId")
    suspend fun deleteParticipant(groupId: String, userId: String)
}