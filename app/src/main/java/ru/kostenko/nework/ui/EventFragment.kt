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
import io.getstream.avatarview.coil.loadImage
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentEventBinding
import ru.kostenko.nework.databinding.PlaceBinding
import ru.kostenko.nework.dto.AttachmentType
import ru.kostenko.nework.util.AndroidUtils
import ru.kostenko.nework.util.MediaLifecycleObserver
import ru.kostenko.nework.viewmodel.EventViewModel
import ru.kostenko.nework.viewmodel.UserViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class EventFragment : Fragment() {
    private val eventViewModel: EventViewModel by activityViewModels()
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
        val binding = FragmentEventBinding.inflate(inflater, container, false)
        val toolbar = binding.toolbar

        val event = eventViewModel.event.value!!.copy()
        val observer = MediaLifecycleObserver()

        toolbar.apply {
            setTitle(R.string.event)
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
                            putExtra(Intent.EXTRA_TEXT, event.content)
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

        binding.author.text = event.author
        binding.eventDate.text = OffsetDateTime.parse(event.datetime)
            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm"))
        binding.content.text = event.content
        binding.job.text = if (event.authorJob.isNullOrEmpty()) getString(R.string.in_search_job)
        else (event.authorJob)
        binding.eventType.text = event.type.str

        Glide.with(binding.avatar).load(event.authorAvatar).placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.post_avatar_drawable).timeout(10_000)
            .apply(RequestOptions().circleCrop()).into(binding.avatar)

        if (event.attachment != null) {
            when (event.attachment.type) {
                AttachmentType.IMAGE -> {
                    binding.imageAttach.visibility = View.VISIBLE
                }

                AttachmentType.AUDIO -> {
                    binding.audioGroup.visibility = View.VISIBLE
                }

                AttachmentType.VIDEO -> {
                    binding.videoGroup.visibility = View.VISIBLE
                }
            }
        } else {
            binding.imageAttach.visibility = View.GONE
            binding.audioGroup.visibility = View.GONE
            binding.videoGroup.visibility = View.VISIBLE
        }

        binding.imageAttach.visibility =
            if (event.attachment != null && event.attachment.type == AttachmentType.IMAGE) View.VISIBLE else View.GONE

        binding.audioGroup.visibility =
            if (event.attachment != null && event.attachment.type == AttachmentType.AUDIO) View.VISIBLE else View.GONE

        binding.videoGroup.visibility =
            if (event.attachment != null && event.attachment.type == AttachmentType.VIDEO) View.VISIBLE else View.GONE

        event.attachment?.apply {
            binding.imageAttach.contentDescription = this.url
            Glide.with(binding.imageAttach).load(this.url).placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp).timeout(10_000).into(binding.imageAttach)
        }

        binding.play.setOnClickListener {
            binding.videoContent.apply {
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


        binding.playButton.setOnClickListener {
            observer.apply {
                val url = event.attachment!!.url
                mediaPlayer?.setDataSource(url)
            }.play()
        }

        binding.stopButton.setOnClickListener {
            if (observer.mediaPlayer != null && observer.mediaPlayer!!.isPlaying) {
                observer.mediaPlayer?.stop()
            }
        }

        binding.btnElike.text = AndroidUtils.eraseZero(
            event.likeOwnerIds.size.toLong()
        )
        binding.btnElike.isChecked = event.likedByMe
        binding.btnElike.isCheckable = false

        binding.btnEmention.text = AndroidUtils.eraseZero(
            event.participantsIds.size.toLong()
        )
        binding.btnEmention.isChecked = event.participatedByMe
        binding.btnEmention.isCheckable = false

        mapView = binding.mapview.apply {
            userLocation = MapKitFactory.getInstance().createUserLocationLayer(mapWindow)
            userLocation.isVisible = true
            userLocation.isHeadingEnabled = false

            val arguments = event.coords?.copy()
            if (arguments?.lat != null) {
                val latCoord = arguments.lat
                val longCoord = arguments.long
                val collection = mapWindow.map.mapObjects.addCollection()
                collection.clear()
                val placeBinding = PlaceBinding.inflate(layoutInflater)
                collection.addPlacemark(
                    Point(latCoord, longCoord), ViewProvider(placeBinding.root)
                )
                mapWindow.map.cameraPosition
                mapWindow.map.move(
                    CameraPosition(
                        Point(latCoord, longCoord), 16.5F, 0.0f, 0.0f
                    ), Animation(Animation.Type.SMOOTH, 5f), null
                )
            } else {
                binding.mapview.visibility = View.GONE
            }
        }

        val speakers = event.speakerIds.mapNotNull {
            event.users[it]
        }
        binding.avatarLayoutSpkrs.isVisible = speakers.isNotEmpty()
        listOf(
            binding.avatarSpkrs0,
            binding.avatarSpkrs1,
            binding.avatarSpkrs2,
            binding.avatarSpkrs3,
            binding.avatarSpkrs4,
        ).forEachIndexed { index, avatarView ->
            val user = speakers.getOrElse(index) {
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

        if (event.speakerIds.size <= 5) binding.btnSpkrsMore.visibility = View.GONE
        binding.btnSpkrsMore.setOnClickListener {
            userViewModel.setSetIds(event.speakerIds)
            requireParentFragment().findNavController()
                .navigate(R.id.action_eventFragment_to_likersMentMoreFragment)
        }


        val likers = event.likeOwnerIds.mapNotNull {
            event.users[it]
        }
        binding.avatarLayoutEventLike.isVisible = likers.isNotEmpty()
        listOf(
            binding.avatarEliker0,
            binding.avatarEliker1,
            binding.avatarEliker2,
            binding.avatarEliker3,
            binding.avatarEliker4,
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

        if (event.likeOwnerIds.size <= 5) binding.btnElikersMore.visibility = View.GONE
        binding.btnElikersMore.setOnClickListener {
            userViewModel.setSetIds(event.likeOwnerIds)
            requireParentFragment().findNavController()
                .navigate(R.id.action_eventFragment_to_likersMentMoreFragment)
        }


        val participants = event.participantsIds.mapNotNull {
            event.users[it]
        }
        binding.avatarLayoutEment.isVisible = participants.isNotEmpty()
        listOf(
            binding.avatarEment0,
            binding.avatarEment1,
            binding.avatarEment2,
            binding.avatarEment3,
            binding.avatarEment4,
        ).forEachIndexed { index, avatarView ->
            val user = participants.getOrElse(index) {
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

        if (event.participantsIds.size <= 5) binding.btnEmentMore.visibility = View.GONE
        binding.btnEmentMore.setOnClickListener {
            userViewModel.setSetIds(event.participantsIds)
            requireParentFragment().findNavController()
                .navigate(R.id.action_eventFragment_to_likersMentMoreFragment)
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