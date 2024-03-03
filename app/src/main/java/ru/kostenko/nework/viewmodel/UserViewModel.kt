package ru.kostenko.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.kostenko.nework.dto.User
import ru.kostenko.nework.model.FeedModelState
import ru.kostenko.nework.repository.UserRepositoryImpl
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepositoryImpl: UserRepositoryImpl,
) : ViewModel() {

    val data: LiveData<List<User>> =
        userRepositoryImpl.data
            .asLiveData(Dispatchers.Default)

  val dataTakeble: LiveData<List<User>> =
      userRepositoryImpl.data.map { list ->
          list.map { user ->
              user.copy(isTaken = true)
          }
      }.asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    private val _userIds = MutableLiveData<Set<Int>>()
    val userIds: LiveData<Set<Int>>
        get() = _userIds

    init {
        getUsers()
    }

    private fun getUsers() = viewModelScope.launch {
        _dataState.postValue(FeedModelState(loading = true))
        try {
            userRepositoryImpl.getUsers()
            _dataState.postValue(FeedModelState())
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun getUserById(id: Int) = viewModelScope.launch {
        _dataState.postValue(FeedModelState(loading = true))
        try {
            _user.value = userRepositoryImpl.getUserById(id)
            _dataState.postValue(FeedModelState())
        } catch (e: Exception) {
            _dataState.postValue(FeedModelState(error = true))
        }
    }

    fun getUsersIds(set: Set<Int>) =
        viewModelScope.launch { _userIds.value = set }

    fun getTakeble(): LiveData<List<User>> =
        userRepositoryImpl.data.map { list ->
            list.map { user ->
                user.copy(isTaken = true)
            }
        }.asLiveData(Dispatchers.Default)

}
