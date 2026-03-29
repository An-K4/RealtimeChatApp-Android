package com.example.realtimechatapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.realtimechatapp.data.local.dao.GroupContactDao
import com.example.realtimechatapp.data.local.dao.GroupDao
import com.example.realtimechatapp.data.local.dao.GroupMessageDao
import com.example.realtimechatapp.data.local.dao.MessageContactDao
import com.example.realtimechatapp.data.local.dao.MessageDao
import com.example.realtimechatapp.data.local.dao.MemberDao
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
    fun provideMessageContactDao(localDatabase: LocalDatabase): MessageContactDao = localDatabase.messageContactDao()

    @Provides
    @Singleton
    fun provideGroupContactDao(localDatabase: LocalDatabase): GroupContactDao = localDatabase.groupContactDao()

    @Provides
    @Singleton
    fun provideUserDao(localDatabase: LocalDatabase): UserDao = localDatabase.userDao()

    @Provides
    @Singleton
    fun provideGroupDao(localDatabase: LocalDatabase): GroupDao = localDatabase.groupDao()

    @Provides
    @Singleton
    fun provideParticipantDao(localDatabase: LocalDatabase): MemberDao = localDatabase.participantDao()

    @Provides
    @Singleton
    fun provideMessageDao(localDatabase: LocalDatabase): MessageDao = localDatabase.messageDao()

    @Provides
    @Singleton
    fun provideGroupMessageDao(localDatabase: LocalDatabase): GroupMessageDao = localDatabase.groupMessageDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences>{
        return PreferenceDataStoreFactory.create (
            produceFile = {
                context.preferencesDataStoreFile("user_prefs")
            }
        )
    }
}