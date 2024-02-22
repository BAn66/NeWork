package ru.kostenko.nework.repository

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.kostenko.nework.api.ApiService
import ru.kostenko.nework.dao.JobDao
import ru.kostenko.nework.dto.Job
import ru.kostenko.nework.entity.JobEntity
import ru.kostenko.nework.entity.toDto
import ru.kostenko.nework.entity.toEntity
import ru.kostenko.nework.error.ApiError
import ru.kostenko.nework.error.NetworkError
import java.io.IOException
import javax.inject.Inject

class JobsRepositoryImpl @Inject constructor(
    private val jobDao: JobDao,
    private val apiService: ApiService,
) : JobsRepository {
    override val data: Flow<List<Job>> = jobDao.getAllJobs()
        .map { it.toDto() }
        .flowOn(Dispatchers.Default)

    private val _data = MutableLiveData<List<Job>>()


    override suspend fun getMyJobs() {
        try {
            jobDao.getAllJobs()
            val response = apiService.getMyJobs()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insertListJobs(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError()
        }
    }

    override suspend fun getJobs(id: Int) {
        try {
            jobDao.clear()
            val response = apiService.getJobs(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            _data.postValue(body)
            jobDao.insertListJobs(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError()
        }
    }

    override suspend fun deleteMyJobs(id: Int) {
        try {
            val response = apiService.deleteMyJobs(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            jobDao.deleteJobById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError()
        }
    }

    override suspend fun setMyJob(job: Job) {
        try {
            val response = apiService.setMyJob(job)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insertJob(JobEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun clearDb() {
        jobDao.clear()
    }
}
