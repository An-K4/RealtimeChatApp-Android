package com.example.realtimechatapp.di

import com.example.realtimechatapp.data.repository.AuthRepositoryImpl
import com.example.realtimechatapp.data.repository.GroupRepositoryImpl
import com.example.realtimechatapp.data.repository.MessageRepositoryImpl
import com.example.realtimechatapp.domain.repository.AuthRepository
import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.repository.MessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(messageRepositoryImpl: MessageRepositoryImpl): MessageRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(groupRepository: GroupRepositoryImpl): GroupRepository
}