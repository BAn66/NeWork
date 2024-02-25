package ru.kostenko.nework.viewmodel

import android.icu.text.SimpleDateFormat
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
import ru.kostenko.nework.repository.JobsRepositoryImpl
import java.util.Locale
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

    fun edit(job: Job) {
        edited.value = job
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

    fun formateDateString(str: String?):String{
        var newDate: String? = null
        if (str!=null) {
            val values = str.split("/")
            val day = values[0]
            val month = values[1]
            val year = values[2]
//        dd/mm/yyyy
            newDate = "$year-$month-$day'T'00:00:01.667'Z'"
        }
        else
            newDate = "1900-01-01T00:00:00Z"
        return newDate
    }

    fun startDate(date: String) {
        edited.value = edited.value?.copy(start = date)
    }

    fun endDate(date: String) {
        edited.value = edited.value?.copy(finish = date)
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