package ru.kostenko.nework.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.dto.Job
import ru.kostenko.nework.model.FeedModelState
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
    private val edited = MutableLiveData(empty)
    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<List<Job>> = appAuth.authStateFlow.flatMapLatest { (myId, _) ->
        jobsRepository.data.map {
            it.map { job ->
                job.copy(
                    ownedByMe = userId.value == myId.toInt()
                )
            }
        }
    }

    private val _dataState = MutableLiveData(FeedModelState())

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

    fun removeMyJob(id: Int) {
        viewModelScope.launch {
            try {
                jobsRepository.deleteMyJobs(id)
                _dataState.value = FeedModelState()

            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)

            }
        }
    }
    fun setId(id: Int) {
        userId.value = id
    }

    fun save(
        name: String,
        position: String,
        start: String,
        finish: String,
        link: String?,

        ) {
            edited.value?.let {
            val jobCopy = it.copy(
                name = name.trim(),
                position = position.trim(),
                start = start,
                finish = finish,
                link = link?.trim()
            )
            viewModelScope.launch {
                try {
                    jobsRepository.setMyJob(jobCopy)
                    _dataState.value = FeedModelState()

                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)

                }
            }
        }
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