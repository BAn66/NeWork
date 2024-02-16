package ru.kostenko.nework.viewmodel


import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.kostenko.nework.error.NetworkError
import ru.kostenko.nework.error.UnknownError
import ru.kostenko.nework.model.PhotoModel
import ru.kostenko.nework.repository.AuthResultCode
import ru.kostenko.nework.repository.UserRepositoryImpl
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepositoryImpl,
) : ViewModel() {

    private val _photo = MutableLiveData<PhotoModel?>(null)  //Для картинок
    val photo: LiveData<PhotoModel?>
        get() = _photo

    suspend fun sendRequest(login: String, password: String): AuthResultCode {
        try {
            return repository.authentication(login, password)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
    suspend fun sendRequest(login: String, password: String, name: String): AuthResultCode {
        try {
            val photoModel = _photo.value
            if (photoModel != null) {
                repository.registration(login, password, name, photoModel)
            }
            return repository.authentication(login, password)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    fun setPhoto(uri: Uri, file: File) {
        _photo.value = PhotoModel(uri, file)
    }
}
