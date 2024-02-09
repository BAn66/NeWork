package ru.kostenko.nework.viewmodel


import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.kostenko.nework.error.NetworkError
import ru.kostenko.nework.error.UnknownError
import ru.kostenko.nework.repossitory.AuthResultCode
import ru.kostenko.nework.repossitory.PostRepositoryImpl
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: PostRepositoryImpl,
) : ViewModel() {

    suspend fun sendRequest(login: String, password: String): AuthResultCode {
        try {
            return repository.auth(login, password)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}
