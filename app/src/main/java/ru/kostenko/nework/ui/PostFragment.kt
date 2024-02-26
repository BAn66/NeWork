package ru.kostenko.nework.ui

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.MediaController
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.filter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.adapter.OnPostInteractionListener
import ru.kostenko.nework.adapter.PostsAdapter
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentPostBinding
import ru.kostenko.nework.dto.AttachmentType
import ru.kostenko.nework.dto.Post
import ru.kostenko.nework.util.AndroidUtils
import ru.kostenko.nework.util.MediaLifecycleObserver
import ru.kostenko.nework.viewmodel.PostViewModel
import ru.kostenko.nework.viewmodel.UserViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class PostFragment : Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth
    private val postViewModel: PostViewModel by activityViewModels()
    private lateinit var toolbar: Toolbar

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentPostBinding.inflate(inflater, container, false)

        //Наполняем верхний аппбар
        toolbar = binding.toolbar
        toolbar.apply {
            setTitle(R.string.post)
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            inflateMenu(R.menu.share_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.share -> {
                        Toast.makeText(context, "Делимся ссылкой", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
        }

//        setFragmentResultListener("openPostId") { _, bundle ->
//            val postId = bundle.getInt("id")
//            Log.d("MYTAAAG", "onCreateView2: ${postId}")
//            lifecycleScope.launch {
//                postViewModel.getPostById(postId).join()
//            }
//        }

        val post= postViewModel.post
        Log.d("MYTAAAG", "onCreateView3:${post.value?.id} ${postViewModel.post.value?.id}")
            val observer :MediaLifecycleObserver = MediaLifecycleObserver()
//
            binding.author.text = post.value?.author
            binding.published.text = OffsetDateTime.parse(post.value?.published)
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm"))
            binding.content.text = post.value!!.content

                Glide.with(binding.avatar)
                    .load(post.value?.authorAvatar)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.post_avatar_drawable)
                    .timeout(10_000)
                    .apply(RequestOptions().circleCrop()) //делает круглыми аватарки
                    .into(binding.avatar)
//
//                if (post.value?.attachment != null) {
//                    when (post.value!!.attachment!!.type) {
//                        AttachmentType.IMAGE -> binding.imageAttach.visibility = View.VISIBLE
//                        AttachmentType.AUDIO -> binding.audioGroup.visibility = View.VISIBLE
//                        AttachmentType.VIDEO -> binding.videoGroup.visibility = View.VISIBLE
//                    }
//                } else {
//                    binding.imageAttach.visibility = View.GONE
//                    binding.audioGroup.visibility = View.GONE
//                    binding.videoGroup.visibility = View.VISIBLE
//                }
//
//            binding.imageAttach.visibility =
//                    if (post.value?.attachment != null && post.value!!.attachment!!.type == AttachmentType.IMAGE) View.VISIBLE else View.GONE
//
//            binding.audioGroup.visibility =
//                    if (post.value?.attachment != null && post.value!!.attachment!!.type == AttachmentType.AUDIO) View.VISIBLE else View.GONE
//
//            binding.videoGroup.visibility =
//                    if (post.value?.attachment != null && post.value!!.attachment!!.type == AttachmentType.VIDEO) View.VISIBLE else View.GONE
//
//            post.value?.attachment?.apply {
//                    binding.imageAttach.contentDescription = this.url
//                    Glide.with(binding.imageAttach)
//                        .load(this.url)
//                        .placeholder(R.drawable.ic_loading_100dp)
//                        .error(R.drawable.ic_error_100dp)
//                        .timeout(10_000)
//                        .into(binding.imageAttach)
//                }
//
////            if (post.authorJob != null) {
////                author.text = itemView.context.getString(
////                    R.string.author_job,
////                    post.author,
////                    post.authorJob
////                )
////            } else author.text = post.author
//
//            binding.play.setOnClickListener {
//                binding.videoContent.apply{
//                        setMediaController(MediaController( context))
//                        setVideoURI(
//                            Uri.parse(post.value!!.attachment!!.url)
//                        )
//                        setOnPreparedListener {
//                            start()
//                        }
//                        setOnCompletionListener {
//                            stopPlayback()
//                        }
//                    }
//                }
//
//
//            binding.playButton.setOnClickListener {
//                observer.apply {
//                        //Не забываем добавлять разрешение в андроид манифест на работу с сетью
//                        val url = post.value!!.attachment!!.url
//                        mediaPlayer?.setDataSource(url) //TODO при нажатии на паузу аудиоплеера и повторном плэй падает
//                    }.play()
//                }
//
//            binding.pauseButton.setOnClickListener {
//                    if (observer.mediaPlayer != null) {
//                        if (observer.mediaPlayer!!.isPlaying) observer.mediaPlayer?.pause() else observer.mediaPlayer?.start()
//                    }
//                }
//
//            binding.stopButton.setOnClickListener {
//                    if (observer.mediaPlayer != null &&  observer.mediaPlayer!!.isPlaying) {
//                        observer.mediaPlayer?.stop()
//                    }
//                }
//
//            binding.btnLike.text = AndroidUtils.eraseZero(post.value?.likeOwnerIds?.size?.toLong()
//                ?: 0)
//            binding.btnLike.isChecked = post.value?.likedByMe!!
//
//            binding.btnLike.setOnClickListener {//анимация лайка
//                    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1F, 1.25F, 1F)
//                    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1F, 1.25F, 1F)
//                    ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY).apply {
//                        duration = 500
////                    repeatCount = 100
//                        interpolator = BounceInterpolator()
//                    }.start()
//                    postViewModel.likePostById(post.value?.id!!, post.value?.likedByMe!!)
//                }

//            btnLike.setOnLongClickListener {
//                onPostInteractionListener.onOpenLikers(post)
//                true
//            }


//            val adapter = PostsAdapter(object : OnPostInteractionListener {
//                override fun like(post: Post) {
//                    postViewModel.likePostById(post.id, post.likedByMe)
//                }
//                override fun remove(post: Post) {
//                    postViewModel.removePostById(post.id)
//                    findNavController().navigateUp()
//                }
//                override fun edit(post: Post) {
//                    postViewModel.editPost(post)
//                }
//                override fun openPost(post: Post) {}
//            }, MediaLifecycleObserver())
//            binding.listPost.adapter = adapter
//            val idPost = MutableStateFlow(result).value
//            viewLifecycleOwner.lifecycleScope.launch {
//                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                    postViewModel.data.collectLatest {
//                        adapter.submitData(
//                            it.filter {
//                                it.id == idPost
//                            }
//                        )
//                    }
//                }
//            }


        return binding.root
    }
}