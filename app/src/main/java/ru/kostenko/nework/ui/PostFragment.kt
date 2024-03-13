package ru.kostenko.nework.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.ui_view.ViewProvider
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.avatarview.coil.loadImage
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentPostBinding
import ru.kostenko.nework.databinding.PlaceBinding
import ru.kostenko.nework.dto.AttachmentType
import ru.kostenko.nework.util.AndroidUtils
import ru.kostenko.nework.util.MediaLifecycleObserver
import ru.kostenko.nework.viewmodel.PostViewModel
import ru.kostenko.nework.viewmodel.UserViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class PostFragment : Fragment() {
    private val postViewModel: PostViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private var mapView: MapView? = null
    private lateinit var userLocation: UserLocationLayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentPostBinding.inflate(inflater, container, false)
        val toolbar = binding.toolbar
        val post = postViewModel.post.value!!.copy()
        val observer = MediaLifecycleObserver()

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
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, post.content)
                            type = "text/plain"
                        }
                        val shareIntent =
                            Intent.createChooser(intent, getString(R.string.description_shared))
                        startActivity(shareIntent)
                        true
                    }

                    else -> false
                }
            }
        }

        binding.author.text = post.author
        binding.published.text = OffsetDateTime.parse(post.published)
            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm"))
        binding.content.text = post.content
        binding.job.text = if (post.authorJob.isNullOrEmpty()) getString(R.string.in_search_job)
        else (post.authorJob)

        Glide.with(binding.avatar)
            .load(post.authorAvatar)
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.post_avatar_drawable)
            .timeout(10_000)
            .apply(RequestOptions().circleCrop())
            .into(binding.avatar)

        if (post.attachment != null) {
            when (post.attachment.type) {
                AttachmentType.IMAGE -> binding.imageAttach.visibility = View.VISIBLE
                AttachmentType.AUDIO -> binding.audioGroup.visibility = View.VISIBLE
                AttachmentType.VIDEO -> binding.videoGroup.visibility = View.VISIBLE
            }
        } else {
            binding.imageAttach.visibility = View.GONE
            binding.audioGroup.visibility = View.GONE
            binding.videoGroup.visibility = View.VISIBLE
        }

        binding.imageAttach.visibility =
            if (post.attachment != null && post.attachment.type == AttachmentType.IMAGE) View.VISIBLE else View.GONE

        binding.audioGroup.visibility =
            if (post.attachment != null && post.attachment.type == AttachmentType.AUDIO) View.VISIBLE else View.GONE

        binding.videoGroup.visibility =
            if (post.attachment != null && post.attachment.type == AttachmentType.VIDEO) View.VISIBLE else View.GONE

        post.attachment?.apply {
            binding.imageAttach.contentDescription = this.url
            Glide.with(binding.imageAttach)
                .load(this.url)
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .into(binding.imageAttach)
        }

        binding.play.setOnClickListener {
            binding.videoContent.apply {
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


        binding.playButton.setOnClickListener {
            observer.apply {
                val url = post.attachment!!.url
                mediaPlayer?.setDataSource(url)
            }.play()
        }

        binding.stopButton.setOnClickListener {
            if (observer.mediaPlayer != null && observer.mediaPlayer!!.isPlaying) {
                observer.mediaPlayer?.stop()
            }
        }

        binding.btnLike.text = AndroidUtils.eraseZero(
            post.likeOwnerIds.size.toLong()
        )
        binding.btnLike.isChecked = post.likedByMe
        binding.btnLike.isCheckable = false

        binding.btnMention.text = AndroidUtils.eraseZero(
            post.mentionIds.size.toLong()
        )
        binding.btnMention.isChecked = post.mentionedMe
        binding.btnMention.isCheckable = false

        mapView = binding.mapview.apply {
            userLocation = MapKitFactory.getInstance().createUserLocationLayer(mapWindow)
            userLocation.isVisible = true
            userLocation.isHeadingEnabled = false

            val arguments = post.coords?.copy()
            if (arguments?.lat != null) {
                val latCoord = arguments.lat
                val longCoord = arguments.long
                val collection = mapWindow.map.mapObjects.addCollection()
                collection.clear()
                val placeBinding = PlaceBinding.inflate(layoutInflater)
                collection.addPlacemark(
                    Point(latCoord, longCoord),
                    ViewProvider(placeBinding.root)
                )
                mapWindow.map.cameraPosition
                mapWindow.map.move(
                    CameraPosition(
                        Point(latCoord, longCoord),
                        16.5F,
                        0.0f,
                        0.0f
                    ),
                    Animation(Animation.Type.SMOOTH, 5f),
                    null
                )
            } else {
                binding.mapview.visibility = View.GONE
            }
        }

        val likers = post.likeOwnerIds.mapNotNull {
            post.users[it]
        }
        binding.avatarLayoutLike.isVisible = likers.isNotEmpty()
        listOf(
            binding.avatarLiker0,
            binding.avatarLiker1,
            binding.avatarLiker2,
            binding.avatarLiker3,
            binding.avatarLiker4,
        ).forEachIndexed { index, avatarView ->
            val user = likers.getOrElse(index) {
                avatarView.isGone = true
                return@forEachIndexed
            }
            val avatar = user.avatar
            if (avatar?.startsWith("https://") == true) {
                avatarView.loadImage(avatar)
            } else {
                if (avatar == "") {
                    avatarView.loadImage(R.drawable.post_avatar_drawable)
                } else {
                    avatarView.avatarInitials = user.name.take(1).uppercase()
                }
            }
        }

        if (post.likeOwnerIds.size <= 5) binding.btnLikersMore.visibility = View.GONE
        binding.btnLikersMore.setOnClickListener {
            userViewModel.setSetIds(post.likeOwnerIds)
            requireParentFragment().findNavController()
                .navigate(R.id.action_postFragment_to_likersMentMoreFragment)
        }


        val mentions = post.mentionIds.mapNotNull {
            post.users[it]
        }
        binding.avatarLayoutMent.isVisible = likers.isNotEmpty()
        listOf(
            binding.avatarMent0,
            binding.avatarMent1,
            binding.avatarMent2,
            binding.avatarMent3,
            binding.avatarMent4,
        ).forEachIndexed { index, avatarView ->
            val user = mentions.getOrElse(index) {
                avatarView.isGone = true
                return@forEachIndexed
            }
            val avatar = user.avatar
            if (avatar?.startsWith("https://") == true) {
                avatarView.loadImage(avatar)
            } else {
                if (avatar == "") {
                    avatarView.loadImage(R.drawable.post_avatar_drawable)
                } else {
                    avatarView.avatarInitials = user.name.take(1).uppercase()
                }
            }
        }

        if (post.mentionIds.size <= 5) binding.btnMentMore.visibility = View.GONE
        binding.btnMentMore.setOnClickListener {
            userViewModel.setSetIds(post.mentionIds)
            requireParentFragment().findNavController()
                .navigate(R.id.action_postFragment_to_likersMentMoreFragment)
        }


        return binding.root
    }


    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        MapKitFactory.getInstance().onStart()

    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
        MapKitFactory.getInstance().onStop()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("haveApiKey", true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView = null
    }


}