package ru.kostenko.nework.authorization

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow(
        AuthState(
            prefs.getLong("id", 0L),
            prefs.getString("token", null)
        )
    )

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authState.value = AuthState(id, token)
        with(prefs.edit()) {
            putLong("id", id)
            putString("token", token)
            commit()
        }
    }

    @Synchronized
    fun removeAuth() {
        _authState.value = AuthState(0, null)
        with(prefs.edit()) {
            clear()
            commit()
        }
    }

    val authStateFlow: StateFlow<AuthState> = _authState.asStateFlow()
}

data class AuthState(val id: Long = 0, val token: String? = null)