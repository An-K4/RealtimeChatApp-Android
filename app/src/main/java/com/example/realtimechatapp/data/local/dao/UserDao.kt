package com.example.realtimechatapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.realtimechatapp.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Update
    suspend fun updateUser(newUser: UserEntity)

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
    suspend fun getUsersInGroup(groupId: String): List<UserEntity>

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Transaction
    suspend fun upsertUser(newUser: UserEntity) {
        val existingUser = getUserById(newUser.id)

        if (existingUser == null) {
            insertUser(newUser)
        } else {
            val mergedUser = existingUser.copy(
                username = newUser.username.takeIf { it.isNotEmpty() } ?: existingUser.username,
                fullName = newUser.fullName.takeIf { it.isNotEmpty() } ?: existingUser.fullName,
                avatar = newUser.avatar ?: existingUser.avatar,
                email = newUser.email.takeIf { it.isNotEmpty() } ?: existingUser.email,
                updatedAt = System.currentTimeMillis() // Update modification time
            )
            updateUser(mergedUser)
        }
    }

    @Transaction
    suspend fun upsertUsers(users: List<UserEntity>) {
        users.forEach { user ->
            upsertUser(user)
        }
    }
}