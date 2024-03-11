package ru.kostenko.nework.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import io.getstream.avatarview.AvatarView
import io.getstream.avatarview.coil.loadImage
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentPostBinding
import ru.kostenko.nework.databinding.PlaceBinding
import ru.kostenko.nework.dto.AttachmentType
import ru.kostenko.nework.util.AndroidUtils
import ru.kostenko.nework.util.MediaLifecycleObserver
import ru.kostenko.nework.viewmodel.PostViewModel
import ru.kostenko.nework.viewmodel.UserViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class PostFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
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
                    R.id.share -> { //Делимся текстом карточки
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
        binding.job.text = if (post.authorJob.isNullOrEmpty()) "В поиске работы"
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

        val listLikersId = mutableListOf<Long>()
        post.likeOwnerIds.forEach {
            listLikersId.add(it)
        }
        val avatarView0: AvatarView = binding.avatarLayoutLike.findViewById(R.id.avatar_liker_0)
        val avatarView1: AvatarView = binding.avatarLayoutLike.findViewById(R.id.avatar_liker_1)
        val avatarView2: AvatarView = binding.avatarLayoutLike.findViewById(R.id.avatar_liker_2)
        val avatarView3: AvatarView = binding.avatarLayoutLike.findViewById(R.id.avatar_liker_3)
        val avatarView4: AvatarView = binding.avatarLayoutLike.findViewById(R.id.avatar_liker_4)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val mapAvatarsLikers = mutableMapOf<Int, Pair<AvatarView, String?>>()
                mapAvatarsLikers.put(0, Pair(avatarView0, null))
                mapAvatarsLikers.put(1, Pair(avatarView1, null))
                mapAvatarsLikers.put(2, Pair(avatarView2, null))
                mapAvatarsLikers.put(3, Pair(avatarView3, null))
                mapAvatarsLikers.put(4, Pair(avatarView4, null))


                if (listLikersId.size == 0) binding.avatarLayoutLike.visibility = View.GONE
                else if (listLikersId.size < 6) {
                    for (i in 0..(listLikersId.size - 1)) {
                        userViewModel.getUserById(listLikersId[i].toInt()).join()
                        val userName = userViewModel.user.value?.name
                        val userAvatar = userViewModel.user.value?.avatar
                        var pair = mapAvatarsLikers.getValue(i)
                        if (userAvatar.isNullOrEmpty()) {
                            pair = pair.copy(second = userName)
                            mapAvatarsLikers.set(i, pair)
                        } else {
                            pair = pair.copy(second = userAvatar)
                            mapAvatarsLikers.set(i, pair)
                        }
                    }


                } else {
                    for (i in 0..4) {
                        userViewModel.getUserById(listLikersId[i].toInt()).join()
                        val userName = userViewModel.user.value?.name
                        val userAvatar = userViewModel.user.value?.avatar
                        var pair = mapAvatarsLikers.getValue(i)
                        if (userAvatar.isNullOrEmpty()) {
                            pair = pair.copy(second = userName)
                            mapAvatarsLikers.set(i, pair)
                        } else {
                            pair = pair.copy(second = userAvatar)
                            mapAvatarsLikers.set(i, pair)
                        }
                    }
                }

                mapAvatarsLikers.forEach {
                    it.value.first.visibility = View.GONE
                    if (it.value.second != null) {
                        if (it.value.second!!.startsWith("https://")) {
                            it.value.first.visibility = View.VISIBLE
                            it.value.first.loadImage(it.value.second)
                        } else {
                            if (it.value.second == "") {
                                it.value.first.visibility = View.VISIBLE
                                it.value.first.loadImage(R.drawable.post_avatar_drawable)
                            } else {
                                it.value.first.visibility = View.VISIBLE
                                it.value.first.avatarInitials =
                                    it.value.second!!.substring(0, 1).uppercase()
                            }
                        }
                    }
                }
            }
        }

        if (post.likeOwnerIds.size <= 5) binding.btnLikersMore.visibility = View.GONE
        binding.btnLikersMore.setOnClickListener {
            userViewModel.setSetIds(post.likeOwnerIds)
            requireParentFragment().findNavController()
                .navigate(R.id.action_postFragment_to_likersMentMoreFragment)
        }


        val listMentId = mutableListOf<Long>()
        post.mentionIds.forEach {
            listMentId.add(it)
        }
        val avatarViewMent0: AvatarView = binding.avatarLayoutMent.findViewById(R.id.avatar_ment_0)
        val avatarViewMent1: AvatarView = binding.avatarLayoutMent.findViewById(R.id.avatar_ment_1)
        val avatarViewMent2: AvatarView = binding.avatarLayoutMent.findViewById(R.id.avatar_ment_2)
        val avatarViewMent3: AvatarView = binding.avatarLayoutMent.findViewById(R.id.avatar_ment_3)
        val avatarViewMent4: AvatarView = binding.avatarLayoutMent.findViewById(R.id.avatar_ment_4)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val mapAvatarsMentioneds = mutableMapOf<Int, Pair<AvatarView, String?>>()
                mapAvatarsMentioneds.put(0, Pair(avatarViewMent0, null))
                mapAvatarsMentioneds.put(1, Pair(avatarViewMent1, null))
                mapAvatarsMentioneds.put(2, Pair(avatarViewMent2, null))
                mapAvatarsMentioneds.put(3, Pair(avatarViewMent3, null))
                mapAvatarsMentioneds.put(4, Pair(avatarViewMent4, null))

                if (listMentId.size == 0) binding.avatarLayoutMent.visibility = View.GONE
                else if (listMentId.size < 6) {
                    for (i in 0..(listMentId.size - 1)) {
                        userViewModel.getUserById(listMentId[i].toInt()).join()
                        val userName = userViewModel.user.value?.name
                        val userAvatar = userViewModel.user.value?.avatar
                        var pair = mapAvatarsMentioneds.getValue(i)
                        if (userAvatar.isNullOrEmpty()) {
                            pair = pair.copy(second = userName)
                            mapAvatarsMentioneds.set(i, pair)
                        } else {
                            pair = pair.copy(second = userAvatar)
                            mapAvatarsMentioneds.set(i, pair)
                        }
                    }
                } else {
                    for (i in 0..4) {
                        userViewModel.getUserById(listMentId[i].toInt()).join()
                        val userName = userViewModel.user.value?.name
                        val userAvatar = userViewModel.user.value?.avatar
                        var pair = mapAvatarsMentioneds.getValue(i)
                        if (userAvatar.isNullOrEmpty()) {
                            pair = pair.copy(second = userName)
                            mapAvatarsMentioneds.set(i, pair)
                        } else {
                            pair = pair.copy(second = userAvatar)
                            mapAvatarsMentioneds.set(i, pair)
                        }
                    }
                }

                mapAvatarsMentioneds.forEach {
                    it.value.first.visibility = View.GONE
                    if (it.value.second != null) {
                        if (it.value.second!!.startsWith("https://")) {
                            it.value.first.visibility = View.VISIBLE
                            it.value.first.loadImage(it.value.second)
                        } else {
                            if (it.value.second == "") {
                                it.value.first.visibility = View.VISIBLE
                                it.value.first.loadImage(R.drawable.post_avatar_drawable)
                            } else {
                                it.value.first.visibility = View.VISIBLE
                                it.value.first.avatarInitials =
                                    it.value.second!!.substring(0, 1).uppercase()
                            }
                        }
                    }
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