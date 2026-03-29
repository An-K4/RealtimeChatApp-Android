package com.example.realtimechatapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.realtimechatapp.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUser(users: List<UserEntity>)

    @Update
    suspend fun updateUser(newUser: UserEntity)

    @Query("UPDATE users SET avatar = :newAvatar WHERE id = :userId")
    suspend fun updateAvatar(newAvatar: String?, userId: String)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("""
        SELECT * FROM users
        WHERE id IN (
            SELECT user_id FROM participants WHERE group_id = :groupId
        )"""
    )
    suspend fun getGroupMember(groupId: String): List<UserEntity>

    @Query("""
        UPDATE users SET is_online = :isOnline, last_seen = :lastSeen
        WHERE id = :userId
    """)
    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean, lastSeen: Long)

    @Query("SELECT * FROM users WHERE is_online = 1")
    fun observeOnlineUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun observeAllUser(): Flow<List<UserEntity>>
}