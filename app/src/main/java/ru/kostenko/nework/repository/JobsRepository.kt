package ru.kostenko.nework.repository

import kotlinx.coroutines.flow.Flow
import ru.kostenko.nework.dto.Job

interface JobsRepository {
    val data: Flow<List<Job>>
    suspend fun getMyJobs()
    suspend fun getJobs(id: Int)
    suspend fun deleteMyJobs(id: Int)
    suspend fun setMyJob(job: Job)
    suspend fun clearDb()
}