package com.example.realtimechatapp.data.remote

import com.example.realtimechatapp.domain.exception.NetworkException
import com.example.realtimechatapp.domain.repository.NetworkChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException


private val ERROR_MESSAGE_KEYS = listOf("error", "message", "msg", "detail")
suspend fun <T> safeApiCall(networkChecker: NetworkChecker, apiCall: suspend () -> T): T {
    // wrap api call in IO thread here instead of in repos
    return withContext(Dispatchers.IO) {
        try {
            if (!networkChecker.isNetworkAvailable()) {
                throw NetworkException.NoInternetException
            }

            apiCall()
        } catch (e: Exception) {
            throw when (e) {
                is HttpException -> {
                    val errorString = e.response()?.errorBody()?.string()
                    val errorMessage = extractErrorMessage(errorString)

                    if (errorMessage != null) {
                        NetworkException.ServerResponseException(errorMessage)
                    } else {
                        NetworkException.UnknownNetworkException
                    }
                }

                // suddenly lost connection
                is IOException -> NetworkException.NoInternetException
                else -> e
            }
        }
    }
}

private fun extractErrorMessage(errorBody: String?): String? {
    if (errorBody.isNullOrBlank()) return null
    return try {
        val json = JSONObject(errorBody)
        ERROR_MESSAGE_KEYS.firstNotNullOfOrNull { key ->
            json.optString(key, "").takeIf { it.isNotBlank() }
        }
    } catch (e: Exception) {
        null
    }
}