package com.example.realtimechatapp.di

import com.example.realtimechatapp.data.local.manager.CurrentUserManagerImpl
import com.example.realtimechatapp.data.local.manager.LanguageManagerImpl
import com.example.realtimechatapp.data.local.manager.ThemeManagerImpl
import com.example.realtimechatapp.data.local.manager.TokenManagerImpl
import com.example.realtimechatapp.domain.repository.NetworkChecker
import com.example.realtimechatapp.data.repository.AuthRepositoryImpl
import com.example.realtimechatapp.data.repository.GroupRepositoryImpl
import com.example.realtimechatapp.data.repository.MediaRepositoryImpl
import com.example.realtimechatapp.data.repository.MessageRepositoryImpl
import com.example.realtimechatapp.data.repository.NetworkCheckerImpl
import com.example.realtimechatapp.data.repository.SocketRepositoryImpl
import com.example.realtimechatapp.data.repository.UserRepositoryImpl
import com.example.realtimechatapp.domain.repository.AuthRepository
import com.example.realtimechatapp.domain.repository.CurrentUserManager
import com.example.realtimechatapp.domain.repository.GroupRepository
import com.example.realtimechatapp.domain.repository.LanguageManager
import com.example.realtimechatapp.domain.repository.MediaRepository
import com.example.realtimechatapp.domain.repository.MessageRepository
import com.example.realtimechatapp.domain.repository.SocketRepository
import com.example.realtimechatapp.domain.repository.ThemeManager
import com.example.realtimechatapp.domain.repository.TokenManager
import com.example.realtimechatapp.domain.repository.UserRepository
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
    abstract fun bindMediaRepository(mediaRepositoryImpl: MediaRepositoryImpl): MediaRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(messageRepositoryImpl: MessageRepositoryImpl): MessageRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(groupRepository: GroupRepositoryImpl): GroupRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindSocketRepository(socketRepositoryImpl: SocketRepositoryImpl): SocketRepository

    @Binds
    @Singleton
    abstract fun bindNetworkChecker(networkCheckerImpl: NetworkCheckerImpl): NetworkChecker

    @Binds
    @Singleton
    abstract fun bindCurrentUserManager(currentUserManagerImpl: CurrentUserManagerImpl): CurrentUserManager

    @Binds
    @Singleton
    abstract fun bindTokenManager(tokenManagerImpl: TokenManagerImpl): TokenManager

    @Binds
    @Singleton
    abstract fun bindLanguageManager(languageManagerImpl: LanguageManagerImpl): LanguageManager

    @Binds
    @Singleton
    abstract fun bindThemeManager(themeManagerImpl: ThemeManagerImpl): ThemeManager
}