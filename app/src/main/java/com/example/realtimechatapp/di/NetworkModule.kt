package com.example.realtimechatapp.di

import com.example.realtimechatapp.data.adapter.UserAdapter
import com.example.realtimechatapp.data.remote.AuthApi
import com.example.realtimechatapp.data.remote.AuthInterceptor
import com.example.realtimechatapp.data.remote.GroupApi
import com.example.realtimechatapp.data.remote.MessageApi
import com.example.realtimechatapp.data.remote.UserApi
import com.example.realtimechatapp.data.remote.dto.UserDto
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "https://realtimechatapp-android-backend.onrender.com"

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(35, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit{
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(provideGson()))
            .baseUrl(BASE_URL)
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMessageApi(retrofit: Retrofit): MessageApi {
        return retrofit.create(MessageApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGroupApi(retrofit: Retrofit): GroupApi{
        return retrofit.create(GroupApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi{
        return retrofit.create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson{
        return GsonBuilder()
            .registerTypeAdapter(UserDto::class.java, UserAdapter())
            .create()
    }
}