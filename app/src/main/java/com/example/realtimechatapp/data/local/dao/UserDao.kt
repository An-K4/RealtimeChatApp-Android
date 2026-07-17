package com.example.realtimechatapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.realtimechatapp.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(newUser: UserEntity)

    @Query("""
        SELECT * FROM users 
        WHERE id != :id 
        AND id IN (SELECT id FROM contacts WHERE is_group = 0)
    """)
    suspend fun getAllContactUsersExcept(id: String): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    // REMEMBER TO ADD TRANSACTION ANNOTATION
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