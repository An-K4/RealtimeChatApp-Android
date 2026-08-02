package com.example.realtimechatapp.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.realtimechatapp.data.adapter.UserAdapter
import com.example.realtimechatapp.data.remote.api.AuthApi
import com.example.realtimechatapp.data.remote.AuthInterceptor
import com.example.realtimechatapp.data.remote.api.GroupApi
import com.example.realtimechatapp.data.remote.api.MediaApi
import com.example.realtimechatapp.data.remote.api.MessageApi
import com.example.realtimechatapp.data.remote.api.UserApi
import com.example.realtimechatapp.data.remote.dto.user.UserDto
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    // private const val BASE_URL = "http://192.168.1.171:3000"

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
    @javax.inject.Named("refreshClient")
    fun provideRefreshClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(35, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit{
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
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
    fun provideMediaApi(retrofit: Retrofit): MediaApi {
        return retrofit.create(MediaApi::class.java)
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

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // Sử dụng tối đa 25% RAM
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideBaseUrl(): String = BASE_URL
}