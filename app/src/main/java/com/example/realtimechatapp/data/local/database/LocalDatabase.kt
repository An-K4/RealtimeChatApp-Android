package com.example.realtimechatapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.realtimechatapp.data.local.dao.GroupContactDao
import com.example.realtimechatapp.data.local.dao.GroupDao
import com.example.realtimechatapp.data.local.dao.GroupMessageDao
import com.example.realtimechatapp.data.local.dao.MessageContactDao
import com.example.realtimechatapp.data.local.dao.MessageDao
import com.example.realtimechatapp.data.local.dao.ParticipantDao
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.local.entity.ContactEntity
import com.example.realtimechatapp.data.local.entity.GroupEntity
import com.example.realtimechatapp.data.local.entity.MessageEntity
import com.example.realtimechatapp.data.local.entity.ParticipantEntity
import com.example.realtimechatapp.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

@Database(
    entities = [
        ContactEntity::class,
        UserEntity::class,
        GroupEntity::class,
        ParticipantEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LocalDatabase: RoomDatabase() {

    abstract fun messageContactDao(): MessageContactDao
    abstract fun groupContactDao(): GroupContactDao
    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    abstract fun participantDao(): ParticipantDao
    abstract fun messageDao(): MessageDao
    abstract fun groupMessageDao(): GroupMessageDao

    companion object{
        @Volatile
        private var INSTANCE: LocalDatabase? = null

        fun getInstance(context: Context, converters: Converters): LocalDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalDatabase::class.java,
                    "local_database.db"
                )
                    .addTypeConverter(converters)
                    .build()

//                    .setQueryExecutor(Dispatchers.IO.asExecutor())
//                    .setTransactionExecutor(Dispatchers.IO.asExecutor())
//                    Let Room set threads freely

                INSTANCE = instance
                instance
            }
        }
    }
}