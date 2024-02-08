package ru.kostenko.nework.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.dto.Token
import ru.kostenko.nework.repossitory.PostRepository
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth,
) : ViewModel() {
    fun sendRequest(login: String, password: String) {
        viewModelScope.launch {
            try {
                val token: Token = repository.requestToken(login, password)
                appAuth.setAuth(token.id, token.token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}