package ru.kostenko.nework.authorization

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.kostenko.nework.api.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(
        AuthState(
            prefs.getLong("id", 0L),
            prefs.getString("token", null)
        )
    )

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authState.value = AuthState(id, token)
        with(prefs.edit()) {//запись в префс айди пользователя и токена для авторизации
            putLong("id", id)
            putString("token", token)
            commit()
        }
    }

    @Synchronized
    fun removeAuth() {
        _authState.value = AuthState(0, null) //запись в префс нулевых значений авторизации
        with(prefs.edit()) {
            clear()
            commit()
        }
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint { //Не стандартный способ добрать до аписервиса из зависимостей HILTa
        fun getApiService(): ApiService
    }


    val authStateFlow: StateFlow<AuthState> = _authState.asStateFlow()
}

data class AuthState(val id: Long = 0, val token: String? = null)