package ru.kostenko.nework.authorization

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.kostenko.nework.api.ApiService
import ru.kostenko.nework.dto.PushToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
)
    {
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
            sendPushToken()
        }

        @Synchronized
        fun removeAuth() {
            _authState.value = AuthState(0, null) //запись в префс нулевых значений авторизации
            with(prefs.edit()) {
                clear()
                commit()
            }
            sendPushToken()
        }

        @InstallIn(SingletonComponent::class)
        @EntryPoint
        interface AppAuthEntryPoint{ //Не стандартный способ добрать до аписервиса из зависимостей HILTa
            fun getApiService(): ApiService
        }

        fun sendPushToken(token: String? = null) { //PUSHes // запускается при каком либо изменении авторизации (добавил в методах выше)
            //отправка токена отсюда
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val pushToken = PushToken(token ?: FirebaseMessaging.getInstance().token.await())
                    val entryPoint =
//                        EntryPointAccessors.fromApplication(context, SendPushTokenWorker.AppAuthEntryPoint::class.java) //запускаем воркер для постоянного запроса токена
                    EntryPointAccessors.fromApplication(context, SendPushTokenWorker.AppAuthEntryPoint::class.java)
                    entryPoint.getApiService().sendPushToken(pushToken)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val authStateFlow: StateFlow<AuthState> = _authState.asStateFlow()
}

data class AuthState(val id: Long = 0L, val token: String? = null)