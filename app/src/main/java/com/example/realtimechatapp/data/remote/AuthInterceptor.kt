package com.example.realtimechatapp.data.remote

import com.example.realtimechatapp.data.local.manager.TokenManagerImpl
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManagerImpl,
    private val baseUrl: String,
    private val gson: Gson,
    @Named("refreshClient") private val refreshClient: OkHttpClient
) : Interceptor {

    private var isRefreshing = false

    @Synchronized
    private fun refreshToken(): RefreshResult {
        // Prevent multiple concurrent refresh attempts
        if (isRefreshing) {
            return RefreshResult.AlreadyRefreshing
        }

        isRefreshing = true

        try {
            val refreshToken = runBlocking { tokenManager.refreshToken.first() }

            if (refreshToken.isNullOrEmpty()) {
                Timber.d("No refresh token available")
                return RefreshResult.NoRefreshToken
            }

            // Create manual refresh request
            val requestBody = gson.toJson(mapOf("refreshToken" to refreshToken))
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$baseUrl/auth/refresh")
                .post(requestBody)
                .build()

            val response = refreshClient.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val refreshResponse = gson.fromJson(responseBody, RefreshResponse::class.java)

                    // Save new tokens
                    runBlocking {
                        tokenManager.saveToken(refreshResponse.accessToken)
                        tokenManager.saveRefreshToken(refreshResponse.refreshToken)
                    }

                    Timber.d("Token refresh successful")
                    return RefreshResult.Success(refreshResponse.accessToken)
                }
            }

            Timber.e("Token refresh failed: ${response.code}")
            return RefreshResult.Failure

        } catch (e: Exception) {
            Timber.e(e, "Token refresh error")
            return RefreshResult.Failure
        } finally {
            isRefreshing = false
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = runBlocking {
            tokenManager.token.first()
        }

        val response = if (token != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }

        // Handle 401 Unauthorized
        if (response.code == 401) {
            response.close()

            val refreshResult = refreshToken()

            return when (refreshResult) {
                is RefreshResult.Success -> {
                    // Retry original request with new access token
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer ${refreshResult.newAccessToken}")
                        .build()
                    chain.proceed(newRequest)
                }

                RefreshResult.Failure, RefreshResult.NoRefreshToken -> {
                    // Refresh failed, delete both tokens
                    runBlocking {
                        tokenManager.deleteToken()
                        tokenManager.deleteRefreshToken()
                    }
                    Timber.d("Token refresh failed, tokens deleted")
                    // Return a new 401 response since original was closed
                    chain.proceed(originalRequest)
                }

                RefreshResult.AlreadyRefreshing -> {
                    // Another thread is refreshing, return 401
                    chain.proceed(originalRequest)
                }
            }
        }

        return response
    }

    // Helper classes for refresh response
    private data class RefreshResponse(
        val message: String,
        val accessToken: String,
        val refreshToken: String
    )

    private sealed class RefreshResult {
        data class Success(val newAccessToken: String) : RefreshResult()
        object Failure : RefreshResult()
        object NoRefreshToken : RefreshResult()
        object AlreadyRefreshing : RefreshResult()
    }
}