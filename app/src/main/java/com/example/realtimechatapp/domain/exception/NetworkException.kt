package com.example.realtimechatapp.domain.exception

import java.io.IOException

sealed class NetworkException : IOException() {
    object UnknownNetworkException : NetworkException()
    object NoInternetException : NetworkException()
    object ServerUnreachableException : NetworkException()
    data class ServerResponseException(override val message: String) : NetworkException()
}