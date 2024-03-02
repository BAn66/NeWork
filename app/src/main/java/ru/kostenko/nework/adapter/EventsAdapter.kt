package ru.kostenko.nework.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.MediaController
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.CardEventBinding
import ru.kostenko.nework.dto.AttachmentType
import ru.kostenko.nework.dto.DiffCallback
import ru.kostenko.nework.dto.Event
import ru.kostenko.nework.dto.FeedItem
import ru.kostenko.nework.util.AndroidUtils.eraseZero
import ru.kostenko.nework.util.MediaLifecycleObserver
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

interface OnEventInteractionListener {
    fun like(event: Event)
    fun remove(event: Event)
    fun edit(event: Event)
    fun openEvent(event: Event)
    fun share(event: Event)
}

class EventsAdapter(
    private val onEventIteractionLister: OnEventInteractionListener,
    private val observer: MediaLifecycleObserver
) : PagingDataAdapter<FeedItem, EventsAdapter.EventViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding =
            CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onEventIteractionLister, observer)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position) ?: return
        holder.bind(event as Event)
    }


    class EventViewHolder(
        private val binding: CardEventBinding,
        private val onEventInteractionListener: OnEventInteractionListener,
        private val observer: MediaLifecycleObserver
    ) : RecyclerView.ViewHolder(binding.root) {
        //        @SuppressLint("SetTextI18n")
        fun bind(event: Event) {
            binding.apply {
                author.text = event.author
                published.text = OffsetDateTime.parse(event.published)
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm"))
                Log.d("EventTAAAG", "bind event.published: ${event.published.toString()} ")
                content.setText(event.content)
                typeEvent.text =event.type.str

                Log.d("EventTAAAG", "bind event.datetime: ${event.datetime.toString()} ")
                if (event.datetime != "1900-01-01T00:00:00Z") {
                    dateTime.text = OffsetDateTime.parse(event.datetime)
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm"))
//                dateTime.text = event.datetime.toString()
                } else dateTime.text = "без даты"



                Glide.with(avatar)
                    .load(event.authorAvatar)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.post_avatar_drawable)
                    .timeout(10_000)
                    .apply(RequestOptions().circleCrop()) //делает круглыми аватарки
                    .into(avatar)

                if (event.attachment != null) {
                    when (event.attachment.type) {
                        AttachmentType.IMAGE -> imageAttach.visibility = View.VISIBLE
                        AttachmentType.AUDIO -> audioGroup.visibility = View.VISIBLE
                        AttachmentType.VIDEO -> videoGroup.visibility = View.VISIBLE
                    }
                } else {
                    imageAttach.visibility = View.GONE
                    audioGroup.visibility = View.GONE
                    videoGroup.visibility = View.VISIBLE
                }

                imageAttach.visibility =
                    if (event.attachment != null && event.attachment.type == AttachmentType.IMAGE) View.VISIBLE else View.GONE

                audioGroup.visibility =
                    if (event.attachment != null && event.attachment.type == AttachmentType.AUDIO) View.VISIBLE else View.GONE

                videoGroup.visibility =
                    if (event.attachment != null && event.attachment.type == AttachmentType.VIDEO) View.VISIBLE else View.GONE

                event.attachment?.apply {
                    imageAttach.contentDescription = this.url
                    Glide.with(imageAttach)
                        .load(this.url)
                        .placeholder(R.drawable.ic_loading_100dp)
                        .error(R.drawable.ic_error_100dp)
                        .timeout(10_000)
                        .into(imageAttach)
                }

//            if (post.authorJob != null) {
//                author.text = itemView.context.getString(
//                    R.string.author_job,
//                    post.author,
//                    post.authorJob
//                )
//            } else author.text = post.author

                play.setOnClickListener {
                    videoContent.apply {
                        setMediaController(MediaController(context))
                        setVideoURI(
                            Uri.parse(event.attachment!!.url)
                        )
                        setOnPreparedListener {
                            start()
                        }
                        setOnCompletionListener {
                            stopPlayback()
                        }
                    }
                }


                playButton.setOnClickListener {
                    observer.apply {
                        //Не забываем добавлять разрешение в андроид манифест на работу с сетью
                        mediaPlayer?.setDataSource(event.attachment!!.url)
                    }.play()
                }

                pauseButton.setOnClickListener {
                    if (observer.mediaPlayer != null) {
                        if (observer.mediaPlayer!!.isPlaying) observer.mediaPlayer?.pause() else observer.mediaPlayer?.start()
                    }
                }

                stopButton.setOnClickListener {
                    if (observer.mediaPlayer != null && observer.mediaPlayer!!.isPlaying) {
                        observer.mediaPlayer?.stop()
                    }
                }

                btnLike.text = eraseZero(event.likeOwnerIds.size.toLong())
                btnLike.isChecked = event.likedByMe

                btnLike.setOnClickListener {//анимация лайка
                    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1F, 1.25F, 1F)
                    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1F, 1.25F, 1F)
                    ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY).apply {
                        duration = 500
//                    repeatCount = 100
                        interpolator = BounceInterpolator()
                    }.start()
                    onEventInteractionListener.like(event)
                }

                btnShare.setOnClickListener {
                    onEventInteractionListener.share(event)
                }

                content.setOnClickListener {
                    println("content clicked")
                    onEventInteractionListener.openEvent(event)
                }

                postLayout.setOnClickListener { onEventInteractionListener.openEvent(event) }
                avatar.setOnClickListener { onEventInteractionListener.openEvent(event) }
                author.setOnClickListener { onEventInteractionListener.openEvent(event) }
                published.setOnClickListener { onEventInteractionListener.openEvent(event) }
                content.setOnClickListener { onEventInteractionListener.openEvent(event)  }
                imageAttach.setOnClickListener { onEventInteractionListener.openEvent(event) }

                menu.isVisible = event.ownedByMe  //Меню видно если пост наш
                menu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.option_post)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    onEventInteractionListener.remove(event)
                                    true
                                }

                                R.id.edit -> {
                                    onEventInteractionListener.edit(event)
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }
            }
        }
    }
}





