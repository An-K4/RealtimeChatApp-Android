package com.example.realtimechatapp.domain.exception

sealed class AuthException: Exception() {
    object UnauthorizedException: AuthException() // token expired
    object InvalidCredentialsException: AuthException() // wrong username or password
    object EmailAlreadyExistsException: AuthException()
    object UsernameAlreadyExistsException: AuthException()
    data class UsernameLengthException(val min: Int, val max: Int): AuthException()
    data class PasswordLengthException(val minLength: Int): AuthException()
    object InvalidEmailException: AuthException()
    object MissingAuthInfoException: AuthException()
    object PasswordNotMatchException: AuthException()
}