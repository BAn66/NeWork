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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.dto.AttachmentType
import ru.kostenko.nework.dto.Event
import ru.kostenko.nework.dto.EventType
import ru.kostenko.nework.dto.FeedItem
import ru.kostenko.nework.dto.MediaModel
import ru.kostenko.nework.model.FeedModelState
import ru.kostenko.nework.repository.EventRepositoryImpl
import ru.kostenko.nework.util.SingleLiveEvent
import java.io.InputStream
import java.time.OffsetDateTime
import javax.inject.Inject

private val empty = Event(
    id = 0,
    authorId = 0,
    author = "",
    authorJob = "",
    authorAvatar = null,
    content = "",
    datetime = "",
    published = "",
    coords = null,
    type = EventType.OFFLINE,
    likeOwnerIds = emptySet(),
    likedByMe = false,
    speakerIds = emptySet(),
    participantsIds = emptySet(),
    participatedByMe = false,
    attachment = null,
    link = null,
    users = mapOf(),
    ownedByMe = false,
)

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepositoryImpl,
    appAuth: AppAuth
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<FeedItem>> = appAuth
        .authStateFlow.flatMapLatest { (myId, _) ->
            repository.dataEvents.map { pagingData ->
                pagingData.map { event ->
                    if (event is Event) {
                        event.copy(ownedByMe = event.authorId == myId)
                    } else {
                        event
                    }
                }
            }
        }.flowOn(Dispatchers.Default)

    private val _media = MutableLiveData<MediaModel?>(null)  //Для картинок, видео, аудио
    val media: LiveData<MediaModel?>
        get() = _media

    private val _dataState = MutableLiveData(FeedModelState()) //Состояние
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val edited = MutableLiveData(empty)

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    init {
        loadEvents()
    }

    fun loadEvents() = viewModelScope.launch { //Загружаем события c помщью коротюнов и вьюмоделскоуп
        try {
            _dataState.value = FeedModelState(loading = true)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun changeEventAndSave(content: String, dateTime: OffsetDateTime, type: EventType) {
        val text: String = content.trim()
        //функция изменения и сохранения в репозитории
        edited.value?.let {
            val eventCopy = it.copy(
                author = "me",
                content = text,
                published = OffsetDateTime.now().toString(),
                datetime = dateTime.toLocalDate().toString(),
                type = type
            //TODO Проверить везде как даты вводятся и отображаются
            )
            viewModelScope.launch {
                try {
                    val mediaModel = _media.value
                    if (mediaModel == null && it.content != text) {
                        repository.saveEvent(eventCopy)

                    } else if (mediaModel != null && it.content != text) {
                        repository.saveEventWithAttachment(eventCopy, mediaModel)
                    }
                    _dataState.value = FeedModelState()
                    _eventCreated.value = Unit
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

    fun removeEventById(id: Int) {
        viewModelScope.launch {
            try {
                repository.removeEventById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun likeEventById(id: Int, likedByMe: Boolean) {
        viewModelScope.launch {
            try {
                repository.likeEventById(id, likedByMe)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun editEvent(event: Event) {
        edited.value = event
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