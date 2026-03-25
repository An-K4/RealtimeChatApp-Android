package com.example.realtimechatapp.data.remote

import com.example.realtimechatapp.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(private val tokenManager: TokenManager): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = runBlocking {
            tokenManager.token.first()
        }

        val response = if (token != null){
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }

        if (response.code == 401){
            val currentToken = runBlocking { tokenManager.token.first() }
            if (!currentToken.isNullOrEmpty()) runBlocking { tokenManager.deleteToken() }
        }

        return response
    }
}