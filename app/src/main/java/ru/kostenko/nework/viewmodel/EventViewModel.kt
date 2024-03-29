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
import ru.kostenko.nework.dto.Coords
import ru.kostenko.nework.dto.Event
import ru.kostenko.nework.dto.EventType
import ru.kostenko.nework.dto.FeedItem
import ru.kostenko.nework.dto.MediaModel
import ru.kostenko.nework.model.FeedModelState
import ru.kostenko.nework.repository.EventRepositoryImpl
import ru.kostenko.nework.util.AndroidUtils.formatDateForServer
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
                        event.copy(ownedByMe = event.authorId.toLong() == myId)
                    } else {
                        event
                    }
                }
            }
        }.flowOn(Dispatchers.Default)

    private val _media = MutableLiveData<MediaModel?>(null)
    val media: LiveData<MediaModel?>
        get() = _media

    private val _dataState = MutableLiveData(FeedModelState())

    private val _datetime = SingleLiveEvent<String>()
    val datetime: LiveData<String>
        get() = _datetime

    val edited = MutableLiveData(empty)

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event>
        get() = _event

    private val _content = SingleLiveEvent<String>()
    val content: LiveData<String>
        get() = _content

    private val _coords = MutableLiveData<Coords?>()
    val coords: LiveData<Coords?>
        get() = _coords

    private val _eventType = MutableLiveData<EventType>()
    val eventType: LiveData<EventType>
        get() = _eventType

    init {
        loadEvents()
    }

    val authorId = appAuth.authStateFlow.value.id.toInt()

    private fun loadEvents() =
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }

    fun changeEventAndSave(content: String, dateTime: String, type: EventType) {
        val text: String = content.trim()
        edited.value?.let { editEvent ->
            viewModelScope.launch {
                val eventCopy = editEvent.copy(
                    author = repository.getUserById(authorId).name,
                    content = text,
                    published = OffsetDateTime.now().toString(),
                    datetime = dateTime,
                    type = type,
                    users = mapOf(),
                )
                try {
                    val mediaModel = if (_media.value?.inputStream != null) _media.value else null
                    repository.saveEvent(eventCopy, mediaModel)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
                clearCoords()
                clearContent()
                clearMedia()
                clearDateTime()
            }
        }
        emptyNew()
    }

    private fun emptyNew() {
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
        uri: Uri, inputStream: InputStream?, type: AttachmentType
    ) {
        _media.value = MediaModel(uri, inputStream, type)
    }

    fun clearMedia() {
        _media.value = null
    }

    fun setContent(tmpContent: String) {
        _content.value = tmpContent
    }

    private fun clearContent() {
        _content.value = ""
    }

    fun setCoords(latC: Double, longC: Double) {
        edited.value = edited.value?.copy(coords = Coords(latC, longC))
    }

    private fun clearCoords() {
        _coords.value = null
    }

    fun setDateTime(string: String) {
        _datetime.value = formatDateForServer(string)
    }

    private fun clearDateTime() {
        _datetime.value = ""
    }

    fun setEventType(type: EventType) {
        _eventType.value = type
    }

    fun getEventById(id: Int) = viewModelScope.launch {
        _dataState.postValue(FeedModelState(loading = true))
        try {
            _event.value = repository.getEventById(id)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun setParticipants(set: Set<Long>) {
        edited.value = edited.value?.copy(participantsIds = set)
    }

    fun setSpeakers(set: Set<Long>) {
        edited.value = edited.value?.copy(speakerIds = set)
    }

    fun participate(id: Int, participatedByMe: Boolean) {
        viewModelScope.launch {
            try {
                repository.participateById(id, participatedByMe)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

}