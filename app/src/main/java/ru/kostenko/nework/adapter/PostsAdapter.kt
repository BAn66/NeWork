package ru.kostenko.nework.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.net.Uri
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
import ru.kostenko.nework.databinding.CardPostBinding
import ru.kostenko.nework.dto.AttachmentType
import ru.kostenko.nework.dto.DiffCallback
import ru.kostenko.nework.dto.FeedItem
import ru.kostenko.nework.dto.Post
import ru.kostenko.nework.util.AndroidUtils.eraseZero
import ru.kostenko.nework.util.MediaLifecycleObserver
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

interface OnPostInteractionListener {
    fun like(post: Post)
    fun remove(post: Post)
    fun edit(post: Post)
    fun openPost(post: Post)
    fun share(post: Post)
}

class PostsAdapter(
    private val onPostIteractionLister: OnPostInteractionListener,
    private val observer: MediaLifecycleObserver
) : PagingDataAdapter<FeedItem, PostViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onPostIteractionLister, observer)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position) ?: return
        holder.bind(post as Post)
    }

}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onPostInteractionListener: OnPostInteractionListener,
    private val observer: MediaLifecycleObserver
) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = OffsetDateTime.parse(post.published)
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm"))
            content.text = post.content

            Glide.with(avatar)
                .load(post.authorAvatar)
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.post_avatar_drawable)
                .timeout(10_000)
                .apply(RequestOptions().circleCrop()) //делает круглыми аватарки
                .into(avatar)

            if (post.attachment != null) {
                when (post.attachment.type) {
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
                if (post.attachment != null && post.attachment.type == AttachmentType.IMAGE) View.VISIBLE else View.GONE

            audioGroup.visibility =
                if (post.attachment != null && post.attachment.type == AttachmentType.AUDIO) View.VISIBLE else View.GONE

            videoGroup.visibility =
                if (post.attachment != null && post.attachment.type == AttachmentType.VIDEO) View.VISIBLE else View.GONE

            post.attachment?.apply {
                imageAttach.contentDescription = this.url
                Glide.with(imageAttach)
                    .load(this.url)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(imageAttach)
            }

            play.setOnClickListener {
                videoContent.apply {
                    setMediaController(MediaController(context))
                    setVideoURI(
                        Uri.parse(post.attachment!!.url)
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
                    val url = post.attachment!!.url
                    mediaPlayer?.setDataSource(url)
                }.play()
            }

            stopButton.setOnClickListener {
                if (observer.mediaPlayer != null && observer.mediaPlayer!!.isPlaying) {
                    observer.mediaPlayer?.stop()
                }
            }

            btnLike.text = eraseZero(post.likeOwnerIds.size.toLong())
            btnLike.isChecked = post.likedByMe

            btnLike.setOnClickListener {
                val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1F, 1.25F, 1F)
                val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1F, 1.25F, 1F)
                ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY).apply {
                    duration = 500
                    interpolator = BounceInterpolator()
                }.start()
                onPostInteractionListener.like(post)
            }

            btnShare.setOnClickListener {
                onPostInteractionListener.share(post)
            }

            content.setOnClickListener {
                println("content clicked")
                onPostInteractionListener.openPost(post)
            }

            postLayout.setOnClickListener { onPostInteractionListener.openPost(post) }
            avatar.setOnClickListener { onPostInteractionListener.openPost(post) }
            author.setOnClickListener { onPostInteractionListener.openPost(post) }
            published.setOnClickListener { onPostInteractionListener.openPost(post) }
            content.setOnClickListener { onPostInteractionListener.openPost(post) }
            imageAttach.setOnClickListener { onPostInteractionListener.openPost(post) }

            menu.isVisible = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.option_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onPostInteractionListener.remove(post)
                                true
                            }

                            R.id.edit -> {
                                onPostInteractionListener.edit(post)
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


