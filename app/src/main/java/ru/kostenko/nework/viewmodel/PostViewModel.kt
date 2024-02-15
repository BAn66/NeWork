package ru.kostenko.nework.viewmodel

import android.net.Uri
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.dto.AttachmentType
import ru.kostenko.nework.dto.FeedItem
import ru.kostenko.nework.dto.MediaModel
import ru.kostenko.nework.dto.Post
import ru.kostenko.nework.error.UnknownError
import ru.kostenko.nework.model.FeedModelState
import ru.kostenko.nework.repository.PostRepository
import ru.kostenko.nework.repository.PostRepositoryImpl
import ru.kostenko.nework.util.SingleLiveEvent
import java.io.File
import java.io.InputStream
import java.time.OffsetDateTime
import javax.inject.Inject

private val empty = Post(
    id = 0,
    author = "",
    authorAvatar = null,
    authorId = 0,
    authorJob = "",
    content = "",
    coords = null,
    likedByMe = false,
    link = null,
    mentionIds = emptySet(),
    mentionedMe = false,
    likeOwnerIds = emptySet(),
    ownedByMe = false,
    published = "",
    attachment = null,
    users = arrayListOf()
)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepositoryImpl,
    appAuth: AppAuth
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<FeedItem>> = appAuth
        .authStateFlow.flatMapLatest { (myId, _) ->
            repository.data.map { pagingData ->
                pagingData.map { post ->
                    if (post is Post) {
//                        maxId.value = maxOf(post.id, maxId.value)// сравнение текущего макс.ид и ид в паггинге
                        post.copy(ownedByMe = post.authorId == myId)
                    } else {
                        post
                    }
                }
            }
//                .catch { throw Exception() }
        }.flowOn(Dispatchers.Default)

    private val _media = MutableLiveData<MediaModel?>(null)  //Для картинок, видео, аудио
    val media: LiveData<MediaModel?>
        get() = _media

    private val _dataState = MutableLiveData(FeedModelState()) //Состояние
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch { //Загружаем посты c помщью коротюнов и вьюмоделскоуп
        try {
            _dataState.value = FeedModelState(loading = true)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun changeContentAndSave(content: String) {
        val text: String = content.trim()
        //функция изменения и сохранения в репозитории
        edited.value?.let {
            val postCopy = it.copy(
                author = "me",
                content = text,
                published = OffsetDateTime.now().toString(),
            )
            viewModelScope.launch {
                try {
                    val mediaModel = _media.value
                    if (mediaModel == null && it.content != text) {
                        repository.savePost(postCopy)

                    } else if (mediaModel != null && it.content != text) {
                        repository.saveWithAttachment(postCopy, mediaModel)
                    }
                    _dataState.value = FeedModelState()
                    _postCreated.value = Unit
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        emptyNew()
    }

    fun emptyNew() {
        edited.value = empty
    }

    fun likeById(id: Int, likedByMe: Boolean) {
        viewModelScope.launch {
            try {
                repository.likePostById(id, likedByMe)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun removeById(id: Int) {
        viewModelScope.launch {
            try {
                repository.removePostById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun setMedia(
        uri: Uri, inputStream: InputStream, type: AttachmentType
    ) {
        _media.value = MediaModel(uri, inputStream, type)
    }

    fun clearMedia() {
        _media.value = null
    }
}