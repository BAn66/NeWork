package ru.kostenko.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.dto.Job
import ru.kostenko.nework.model.FeedModelState
import ru.kostenko.nework.repository.JobsRepository
import ru.kostenko.nework.repository.JobsRepositoryImpl
import javax.inject.Inject


private val empty = Job(
    id = 0,
    name = "",
    position = "",
    start = "",
    finish = "",
    link = "",
    ownedByMe = false,
)

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val jobsRepository: JobsRepositoryImpl,
    appAuth: AppAuth,
) : ViewModel() {
    private val userId = MutableLiveData<Int>()

    val data: Flow<List<Job>> = appAuth.authStateFlow.flatMapLatest { (myId, _) ->
        jobsRepository.data.map {
//            JobModel()
            it.map { job ->
                job.copy(
                    ownedByMe = userId.value == myId.toInt()
                )
            }
        }
    }

    private val _dataState = MutableLiveData(FeedModelState()) //Состояние
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    fun getJobs(id: Int) = viewModelScope.launch {
        _dataState.postValue(FeedModelState(loading = true))
        try {
            jobsRepository.getJobs(id)
            _dataState.postValue(FeedModelState())
        } catch (e: Exception) {
            e.printStackTrace()
            _dataState.postValue(FeedModelState(error = true))
        }
    }

    fun saveMyJob() {}
    fun editMyJob() {}
    fun removeMyJob(id: Int) {}
    fun setId(id: Int) {
        userId.value = id
    }

    fun clearJobs() {
        viewModelScope.launch {
            try {
                jobsRepository.clearDb()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }
}