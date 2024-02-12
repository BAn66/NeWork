package ru.kostenko.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.kostenko.nework.api.ApiService
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.dao.UserDao
import ru.kostenko.nework.dto.FeedItem
import ru.kostenko.nework.entity.UserEntity
import ru.kostenko.nework.entity.toUserEntity
import ru.kostenko.nework.model.PhotoModel
import ru.kostenko.nework.error.*

import java.io.IOException
import java.util.Random
import javax.inject.Inject

interface UserRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun authentication(login: String, password: String): AuthResultCode
    suspend fun registration(login: String, password: String, name: String, avatar: PhotoModel): AuthResultCode
    suspend fun getUsers()
}

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val apiService: ApiService,
    private val appAuth: AppAuth
) : UserRepository {

    //плэйсхолдеры отключены для упрощения демонстрации Paging
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = true),
        pagingSourceFactory = { userDao.getPagingSource() },
        remoteMediator = UserRemoteMediator(
            apiService = apiService,
            postDao = userDao,
            postRemoteKeyDao = userRemoteKeyDao,
            appDb = appDb,
        )
    ).flow
        .map { pagingData ->
            pagingData.map(UserEntity::toDto)
        }

    override suspend fun authentication(login: String, password: String): AuthResultCode {
        try {
            val response = apiService.updateUser(login, password)
            return when (response.code()) {
                404 -> AuthResultCode.UserNotFound
                400 -> AuthResultCode.IncorrectPassword
                200 -> {
                    response.body()?.let {
                        appAuth.setAuth(id = it.id, token = it.token)
                    }
                    AuthResultCode.Success
                }

                else -> AuthResultCode.UnknownError
            }
        } catch (e: IOException) {
            throw NetworkError

        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun registration(login: String, password: String, name: String, avatar: PhotoModel): AuthResultCode {
        try {
            val part = MultipartBody.Part.createFormData("file", avatar.file.name, avatar.file.asRequestBody())
            val response = apiService.newUser(login, password, name, part)
            return when (response.code()) {
                403 -> AuthResultCode.UserAlreadyRegister
                415 -> AuthResultCode.WrongFormatMedia
                400 -> AuthResultCode.UserAlreadyRegister
                200 -> {
                    response.body()?.let {
                        appAuth.setAuth(id = it.id, token = it.token)
                    }
                    AuthResultCode.Success
                }
                else -> AuthResultCode.UnknownError
            }
        } catch (e: IOException) {
            throw NetworkError

        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getUsers() {
        try {
            userDao.getAll()
            val response = apiService.getUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val bodyRsponse = response.body() ?: throw ApiError(response.code(), response.message())
            val entityList = bodyRsponse.toUserEntity() //Превращаем ответ в лист с энтити
            userDao.insert(entityList)
        } catch (e: IOException) {
            e.printStackTrace()
            throw NetworkError

        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }


}

sealed interface AuthResultCode {
    data object Success : AuthResultCode
    data object IncorrectPassword : AuthResultCode
    data object UserNotFound : AuthResultCode
    data object UserAlreadyRegister : AuthResultCode
    data object WrongFormatMedia: AuthResultCode
    data object UnknownError : AuthResultCode
}

//    override suspend fun requestToken(login: String, password: String): Token {
//        try {
//            val response = apiService.updateUser(login, password)
//            statusCode = response.code()
//            Log.d("APIerrCODE", "requestToken Repository: ${statusCode}")
//            AuthResult.Success
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            return body
//
//        } catch (e: IOException) {
//            throw NetworkError
//
//        } catch (e: Exception) {
//            throw UnknownError
//        }
//    }


@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: ApiService,
    private val userDao: UserDao, //база для записи результатов
    private val postRemoteKeyDao: UserRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> { //обработка рефреша
                    if(postDao.isEmpty()) {
                        apiService.getLatest(state.config.initialLoadSize)
                    } else {
                        val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                        apiService.getAfter(id, state.config.pageSize)
                    }
                }

                LoadType.PREPEND -> { //Обработка скролла вверх(новая страница не будет загружаться, мы сделали специально true препенд выключен, false препенд включен.
                    // Так в большинстве приложений сейчас)
//                    delay(2500)
                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                    apiService.getAfter(id, state.config.pageSize)
                }

                LoadType.APPEND -> {//Обработка скролла вниз
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            //заполняем базу ключей данными которые приходят из сети используя транзакции
            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
//                        postRemoteKeyDao.clear()
                        if(postDao.isEmpty()){
                            postRemoteKeyDao.insert(
                                listOf(
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        body.first().id
                                    ),
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.BEFORE,
                                        body.last().id

                                    )
                                )
                            )
                        }
                        else{
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                )
                            )
                        }
                        //postDao.clear()
                    }

                    LoadType.PREPEND ->   //ветка недостижима если отключить препенд наверху
                    {//Обработка скролла вверх
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            )
                        )
                    }


                    LoadType.APPEND -> {//Обработка скролла вниз

                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id
                            )
                        )
                    }
                    else -> Unit
                }
                postDao.insert(body.toEntity())
            }

//            val nextKey = if (body.isEmpty()) null else body.last().id

//            postDao.insert(body.map(PostEntity::fromDto))//записываем тело ответа на запрос в БД

            return MediatorResult.Success(
                body.isEmpty()
            )
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}