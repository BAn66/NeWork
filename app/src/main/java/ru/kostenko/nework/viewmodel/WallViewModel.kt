package ru.kostenko.nework.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.dto.FeedItem
import ru.kostenko.nework.dto.Post
import ru.kostenko.nework.model.FeedModelState
import ru.kostenko.nework.repository.WallRepositoryImpl
import javax.inject.Inject

@HiltViewModel
class WallViewModel @Inject constructor(
    private val repository: WallRepositoryImpl,
    private val appAuth: AppAuth,
) : ViewModel() {
    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getWallPosts(userId: Int): Flow<PagingData<FeedItem>> =appAuth
            .authStateFlow.flatMapLatest { (myId, _) ->
                repository.getWallPosts(userId).map { pagingData ->
                    pagingData.map { post ->
                        if (post is Post) {
                            post.copy(ownedByMe = post.authorId.toLong() == myId)
                        } else {
                            post
                        }
                    }
                }
            }.flowOn(Dispatchers.Default)

    fun likePostById(authorId: Int, id: Int, likedByMe: Boolean) {
        viewModelScope.launch {
            try {
                repository.likePostById(authorId, id, likedByMe)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun likeMyPostById(id: Int, likedByMe: Boolean) {
        viewModelScope.launch {
            try {
                repository.likeMyPostById( id, likedByMe)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun clearWall(){
        viewModelScope.launch {
            try {
                repository.clearDb()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

}