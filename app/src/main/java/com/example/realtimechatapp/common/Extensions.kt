package com.example.realtimechatapp.common

import com.example.realtimechatapp.R
import com.example.realtimechatapp.domain.exception.AuthException
import com.example.realtimechatapp.domain.exception.DatabaseException
import com.example.realtimechatapp.domain.exception.FileException
import com.example.realtimechatapp.domain.exception.GroupException
import com.example.realtimechatapp.domain.exception.MessageException
import com.example.realtimechatapp.domain.exception.NetworkException
import timber.log.Timber
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Throwable.getErrorMessage(): UiText {
    return when (this) {
        // network exceptions
        is NetworkException.UnknownNetworkException -> UiText.StringResource(R.string.unknown_error)
        is NetworkException.NoInternetException -> UiText.StringResource(R.string.lost_connection_to_server)
        is NetworkException.ServerUnreachableException -> UiText.StringResource(R.string.server_unreachable)
        is NetworkException.ServerResponseException -> UiText.DynamicString(this.message)

        // auth exceptions
        is AuthException.UnauthorizedException -> UiText.StringResource(R.string.unauthorized_session)
        is AuthException.InvalidCredentialsException -> UiText.StringResource(R.string.incorrect_username_or_password)
        is AuthException.EmailAlreadyExistsException -> UiText.StringResource(R.string.email_already_exists)
        is AuthException.UsernameAlreadyExistsException -> UiText.StringResource(R.string.username_already_exists)
        is AuthException.UsernameLengthException -> UiText.StringResource(
            R.string.username_length_require,
            this.min,
            this.max
        )
        is AuthException.PasswordLengthException -> UiText.StringResource(
            R.string.password_length_require,
            this.minLength
        )
        is AuthException.InvalidEmailException -> UiText.StringResource(R.string.invalid_email_address)
        is AuthException.MissingAuthInfoException -> UiText.StringResource(R.string.missing_auth_info)
        is AuthException.PasswordNotMatchException -> UiText.StringResource(R.string.passwords_not_match)

        // database exceptions
        is DatabaseException.RecordNotFoundException -> UiText.StringResource(R.string.record_not_found)
        is DatabaseException.OutOfSpaceException -> UiText.StringResource(R.string.out_of_space)
        is DatabaseException.LocalDataWriteException -> UiText.StringResource(R.string.local_data_write_error)
        is DatabaseException.DataCorruptedException -> UiText.StringResource(R.string.database_corrupted)
        is DatabaseException.ConstraintViolationException -> UiText.StringResource(R.string.database_constraint_violation)

        // group exceptions
        is GroupException.GroupIdNotExistException -> UiText.StringResource(R.string.group_id_not_exist)

        // message exceptions
        is MessageException.ContactIdNotExistException -> UiText.StringResource(R.string.contact_id_not_exist)

        // file exceptions
        is FileException.FileNotFoundException -> UiText.StringResource(R.string.file_not_found)
        is FileException.CompressFileException -> UiText.StringResource(R.string.compress_avatar_fail)

        // other exceptions
        is IOException -> UiText.StringResource(R.string.lost_connection_to_server)

        else -> {
            Timber.e(this, "Lỗi: %s", this.message)
            UiText.StringResource(R.string.unknown_error)
        }
    }
}

fun String?.isoToLong(): Long {
    if (this.isNullOrBlank()) return System.currentTimeMillis()
    return try {
        Instant.parse(this).toEpochMilli()
    } catch (e: Exception) {
        Timber.e(e, "Failed to convert to Long: %s", this)
        System.currentTimeMillis()
    }
}

fun Long.formatToTime(toHourMinute: Boolean): String {
    return try {
        val instant = Instant.ofEpochMilli(this)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())

        val pattern = if (toHourMinute) "HH:mm" else "dd/MM/yyyy"
        val formatter = DateTimeFormatter.ofPattern(pattern)

        return zonedDateTime.format(formatter)
    } catch (e: Exception) {
        Timber.e(e, "Failed to parse timestamp: %s", this)
        "" // return null
    }
}

fun String?.formatToTime(toHourMinute: Boolean): String {
    if (this.isNullOrBlank()) return ""

    return this.isoToLong().formatToTime(toHourMinute)
}