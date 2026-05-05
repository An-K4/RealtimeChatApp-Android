package com.example.realtimechatapp.domain.validation

import com.example.realtimechatapp.domain.exception.AuthException

object AuthValidator {
    fun validateUsername(username: String) {
        if (username.length < 3 || username.length > 16) {
            throw AuthException.UsernameLengthException(min = 3, max = 16)
        }
    }

    fun validatePassword(password: String) {
        if (password.length < 6) {
            throw AuthException.PasswordLengthException(minLength = 6)
        }
    }

    fun validatePasswordMatch(password: String, passwordRetype: String) {
        if(password != passwordRetype){
            throw AuthException.PasswordNotMatchException
        }
    }

    fun validateSignUp(username: String, password: String, passwordRetype: String, fullName: String, email: String){
        if(username.isBlank() || password.isBlank() || passwordRetype.isBlank() || fullName.isBlank() || email.isBlank()){
            throw AuthException.MissingAuthInfoException
        }

        validateUsername(username)
        validatePassword(password)
        validatePasswordMatch(password, passwordRetype)

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z0-9]{2,}$".toRegex()
        if (!email.matches(emailRegex)){
            throw AuthException.InvalidEmailException
        }
    }

    fun validateLogin(username: String, password: String){
        if(username.isBlank() || password.isBlank()){
            throw AuthException.MissingAuthInfoException
        }

        validateUsername(username)
        validatePassword(password)
    }
}