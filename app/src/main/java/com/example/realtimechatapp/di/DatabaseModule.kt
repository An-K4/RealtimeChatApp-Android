package com.example.realtimechatapp.di

import android.content.Context
import com.example.realtimechatapp.data.local.dao.ContactDao
import com.example.realtimechatapp.data.local.dao.GroupDao
import com.example.realtimechatapp.data.local.dao.MessageDao
import com.example.realtimechatapp.data.local.dao.ParticipantDao
import com.example.realtimechatapp.data.local.dao.UserDao
import com.example.realtimechatapp.data.local.database.Converters
import com.example.realtimechatapp.data.local.database.LocalDatabase
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideConverters(gson: Gson): Converters {
        return Converters(gson)
    }

    @Provides
    @Singleton
    fun provideLocalDatabase(
        @ApplicationContext context: Context,
        converters: Converters
    ): LocalDatabase{
        return LocalDatabase.getInstance(context, converters)
    }

    @Provides
    @Singleton
    fun provideContactDao(localDatabase: LocalDatabase): ContactDao = localDatabase.contactDao()

    @Provides
    @Singleton
    fun provideUserDao(localDatabase: LocalDatabase): UserDao = localDatabase.userDao()

    @Provides
    @Singleton
    fun provideGroupDao(localDatabase: LocalDatabase): GroupDao = localDatabase.groupDao()

    @Provides
    @Singleton
    fun provideParticipantDao(localDatabase: LocalDatabase): ParticipantDao = localDatabase.participantDao()

    @Provides
    @Singleton
    fun provideMessageDao(localDatabase: LocalDatabase): MessageDao = localDatabase.messageDao()
}