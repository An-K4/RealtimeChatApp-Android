package com.example.realtimechatapp.data.remote

import com.example.realtimechatapp.domain.exception.NetworkException
import com.example.realtimechatapp.domain.repository.NetworkChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

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
                    val errorMessage = try {
                        JSONObject(errorString ?: "").getString("message")
                    } catch (e: Exception) {
                        null
                    }

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