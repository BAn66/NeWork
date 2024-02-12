package ru.kostenko.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.kostenko.nework.api.ApiService
import ru.kostenko.nework.model.FeedModelState
import ru.kostenko.nework.repository.UserRepository
import javax.inject.Inject

class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val ApiService: ApiService,
) : ViewModel() {

    private val _dataState = MutableLiveData(FeedModelState()) //Состояние
    val dataState: LiveData<FeedModelState>
        get() = _dataState

fun loadUsers() = viewModelScope.launch{
    try {
        _dataState.value = FeedModelState(loading = true)
        userRepository.getUsers()
        _dataState.value = FeedModelState()
    } catch (e: Exception) {
        _dataState.value = FeedModelState(error = true)
    }
}

}