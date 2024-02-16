package ru.kostenko.nework.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.MediaController
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.CardPostBinding
import ru.kostenko.nework.dto.AttachmentType
import ru.kostenko.nework.dto.FeedItem
import ru.kostenko.nework.dto.Post

interface OnPostInteractionListener {
    fun like(post: Post)
    fun remove(post: Post)
    fun edit(post: Post)
    fun openPost(post: Post)
//    fun onOpenLikers(post: Post) Это в детальную карточку поста надо
}

class PostsAdapter(
    private val onPostIteractionLister: OnPostInteractionListener,
    private val observer :MediaLifecycleObserver
) : PagingDataAdapter<FeedItem, PostViewHolder>(PostDiffCallback()) {

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
    private val observer :MediaLifecycleObserver
) : RecyclerView.ViewHolder(binding.root){
    @SuppressLint("SetTextI18n")
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published.toString()
            content.text = post.content

            Glide.with(avatar)
                .load(post.authorAvatar)
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.post_avatar_drawable)
                .timeout(10_000)
                .apply(RequestOptions().circleCrop()) //делает круглыми аватарки
                .into(avatar)

            if (post.attachment != null) {
                when (post.attachment.attachmentType) {
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
                if (post.attachment != null && post.attachment.attachmentType == AttachmentType.IMAGE) View.VISIBLE else View.GONE

            audioGroup.visibility =
                if (post.attachment != null && post.attachment.attachmentType == AttachmentType.AUDIO) View.VISIBLE else View.GONE

            videoGroup.visibility =
                if (post.attachment != null && post.attachment.attachmentType == AttachmentType.VIDEO) View.VISIBLE else View.GONE

            post.attachment?.apply {
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
                    videoContent.apply{
                        setMediaController(MediaController( context))
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
                    //Не забываем добавлять разрешение в андроид манифест на работу с сетью
                    mediaPlayer?.setDataSource(post.attachment!!.url)
                }.play()
            }

            pauseButton.setOnClickListener {
                if (observer.mediaPlayer != null) {
                    if (observer.mediaPlayer!!.isPlaying) observer.mediaPlayer?.pause() else observer.mediaPlayer?.start()
                }
            }

            stopButton.setOnClickListener {
                if (observer.mediaPlayer != null &&  observer.mediaPlayer!!.isPlaying) {
                   observer.mediaPlayer?.stop()
                }
            }

            btnLike.text = eraseZero(post.likeOwnerIds.size.toLong())
            btnLike.isChecked = post.likedByMe

            btnLike.setOnClickListener {//анимация лайка
                val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1F, 1.25F, 1F)
                val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1F, 1.25F, 1F)
                ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY).apply {
                    duration = 500
//                    repeatCount = 100
                    interpolator = BounceInterpolator()
                }.start()
                onPostInteractionListener.like(post)
            }

//            btnLike.setOnLongClickListener {
//                onPostInteractionListener.onOpenLikers(post)
//                true
//            }

            content.setOnClickListener {
                println("content clicked")
                onPostInteractionListener.openPost(post)
            }



            postLayout.setOnClickListener { onPostInteractionListener.openPost(post) }
            avatar.setOnClickListener { onPostInteractionListener.openPost(post) }
            author.setOnClickListener { onPostInteractionListener.openPost(post) }
            published.setOnClickListener { onPostInteractionListener.openPost(post) }
//            imageAttach.setOnClickListener { onPostInteractionListener.openImage(post) }

            menu.isVisible = post.ownedByMe  //Меню видно если пост наш
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

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class)
            return false
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return newItem == oldItem
    }
}

private fun eraseZero(number: Long): String {
    var s = ""

    when (number) {
        in 0..999 -> s = number.toString()
        in 1000..9999 -> s =
            (number.toDouble() / 1000).toString().substring(0, number.toString().length - 1)
                .replace(".0", "") + "K"

        in 10000..999999 -> s =
            number.toString().substring(0, number.toString().length - 3) + "K"

        in 1000000..Int.MAX_VALUE -> s =
            (number.toDouble() / 1000000).toString().substring(0, number.toString().length - 4)
                .replace(".0", "") + "M"
    }
    return s
}

class MediaLifecycleObserver :
    LifecycleEventObserver { //Отслеживает события жизенного цикла в приложении

    var mediaPlayer: MediaPlayer? = MediaPlayer()

    fun play(){
        mediaPlayer?.setOnPreparedListener{
            it.start()
        }
        mediaPlayer?.prepareAsync()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> mediaPlayer?.pause()
            Lifecycle.Event.ON_STOP -> {
                mediaPlayer?.release()
                mediaPlayer = null
            }
            Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)
            else -> Unit
        }

    }
}