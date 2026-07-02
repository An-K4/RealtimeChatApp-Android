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
    suspend fun insertAllUsers(users: List<UserEntity>)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Update
    suspend fun updateUser(newUser: UserEntity)

    @Query("UPDATE users SET avatar = :newAvatar WHERE id = :userId")
    suspend fun updateAvatar(newAvatar: String?, userId: String)

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("""
        SELECT * FROM users 
        WHERE id != :id 
        AND id IN (SELECT id FROM contacts WHERE is_group = 0)
    """)
    suspend fun getAllContactUsersExcept(id: String): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("""
        SELECT * FROM users
        WHERE id IN (
            SELECT user_id FROM members WHERE group_id = :groupId
        )"""
    )
    suspend fun getGroupMember(groupId: String): List<UserEntity>

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun observeAllUser(): Flow<List<UserEntity>>
}