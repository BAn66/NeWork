package ru.kostenko.nework.error

import android.database.SQLException
import java.io.IOException

sealed class AppError(val code: String) : RuntimeException() {
    companion object {
        fun from(e: Throwable): AppError = when (e) {
            is AppError -> e
            is SQLException -> DbError
            is IOException -> NetworkError
            else -> UnknownError
        }
    }
}

class ApiError(code: String) : AppError(code)
data object NetworkError : AppError("error_network")
data object DbError : AppError("error_db")
data object UnknownError : AppError("error_unknown")