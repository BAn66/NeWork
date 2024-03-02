package ru.kostenko.nework.ui

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
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
import io.getstream.avatarview.AvatarView
import io.getstream.avatarview.coil.loadImage
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentEventBinding
import ru.kostenko.nework.databinding.PlaceBinding
import ru.kostenko.nework.dto.AttachmentType
import ru.kostenko.nework.util.AndroidUtils
import ru.kostenko.nework.util.MediaLifecycleObserver
import ru.kostenko.nework.viewmodel.EventViewModel
import ru.kostenko.nework.viewmodel.UserViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class EventFragment : Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth
    private val eventViewModel: EventViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var toolbar: Toolbar

    private var mapView: MapView? = null
    private lateinit var userLocation: UserLocationLayer

    private val premissionLauncher = //запрос на разрешение на геолокацию
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            when {
                granted -> {
                    MapKitFactory.getInstance().resetLocationManagerToDefault()
                    userLocation.cameraPosition()?.target?.also {
                        val map = mapView?.mapWindow?.map ?: return@registerForActivityResult
                        val cameraPosition = map.cameraPosition
                        map.move(
                            CameraPosition(
                                it,
                                cameraPosition.zoom,
                                cameraPosition.azimuth,
                                cameraPosition.tilt,
                            )
                        )
                    }
                }

                else -> {
                    Toast.makeText(
                        requireContext(),
                        "Location permission required",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentEventBinding.inflate(inflater, container, false)

        //Наполняем верхний аппбар
        toolbar = binding.toolbar
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
                    R.id.share -> {//Делимся текстом карточки
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
        binding.job.text = if (event.authorJob.isNullOrEmpty()) "В поиске работы"
        else (event.authorJob)
        binding.eventType.text = event.type.str

        Glide.with(binding.avatar)
            .load(event.authorAvatar)
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.post_avatar_drawable)
            .timeout(10_000)
            .apply(RequestOptions().circleCrop()) //делает круглыми аватарки
            .into(binding.avatar)

        if (event.attachment != null) {
            when (event.attachment.type) {
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
            if (event.attachment != null && event.attachment.type == AttachmentType.IMAGE) View.VISIBLE else View.GONE

        binding.audioGroup.visibility =
            if (event.attachment != null && event.attachment.type == AttachmentType.AUDIO) View.VISIBLE else View.GONE

        binding.videoGroup.visibility =
            if (event.attachment != null && event.attachment.type == AttachmentType.VIDEO) View.VISIBLE else View.GONE

        event.attachment?.apply {
            binding.imageAttach.contentDescription = this.url
            Glide.with(binding.imageAttach)
                .load(this.url)
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .into(binding.imageAttach)
        }

//            if (post.authorJob != null) {
//                author.text = itemView.context.getString(
//                    R.string.author_job,
//                    post.author,
//                    post.authorJob
//                )
//            } else author.text = post.author

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
                //Не забываем добавлять разрешение в андроид манифест на работу с сетью
                val url = event.attachment!!.url
                mediaPlayer?.setDataSource(url) //TODO при нажатии на паузу аудиоплеера и повторном плэй падает
            }.play()
        }

//        binding.pauseButton.setOnClickListener {
//            if (observer.mediaPlayer != null) {
//                if (observer.mediaPlayer!!.isPlaying) observer.mediaPlayer?.pause() else observer.mediaPlayer?.start()
//            }
//        }

        binding.stopButton.setOnClickListener {
            if (observer.mediaPlayer != null && observer.mediaPlayer!!.isPlaying) {
                observer.mediaPlayer?.stop()
            }
        }

        binding.btnLike.text = AndroidUtils.eraseZero(
            event.likeOwnerIds.size.toLong()

        )
        binding.btnLike.isChecked = event.likedByMe


        //TODO Проверить работу кнопки лайка внутри события
        binding.btnLike.setOnClickListener {//анимация лайка
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1F, 1.25F, 1F)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1F, 1.25F, 1F)
            ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY).apply {
                duration = 500
//                    repeatCount = 100
                interpolator = BounceInterpolator()
            }.start()
            eventViewModel.likeEventById(event.id, event.likedByMe)
        }

        //Для карты
        mapView = binding.mapview.apply {
            userLocation = MapKitFactory.getInstance().createUserLocationLayer(mapWindow)
            userLocation.isVisible = true
            userLocation.isHeadingEnabled = false

            val arguments = event.coords?.copy()
            //Создаем маркер на карте
            //переход к точке на карте после клика на списке
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
                //При входе в приложение показываем текущее местоположение
                binding.mapview.visibility = View.GONE
            }
        }
        //Группа спикеров
        val listSpeakersId = mutableListOf<Int>()
        event.speakerIds.forEach {
            listSpeakersId.add(it)
        }
        val avatarViewSpkr0: AvatarView = binding.avatarLayoutSpkrs.findViewById(R.id.avatar_spkrs_0)
        val avatarViewSpkr1: AvatarView = binding.avatarLayoutSpkrs.findViewById(R.id.avatar_spkrs_1)
        val avatarViewSpkr2: AvatarView = binding.avatarLayoutSpkrs.findViewById(R.id.avatar_spkrs_2)
        val avatarViewSpkr3: AvatarView = binding.avatarLayoutSpkrs.findViewById(R.id.avatar_spkrs_3)
        val avatarViewSpkr4: AvatarView = binding.avatarLayoutSpkrs.findViewById(R.id.avatar_spkrs_4)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val mapAvatarsSpkrs = mutableMapOf<Int, Pair<AvatarView, String?>>()
                mapAvatarsSpkrs.put(0, Pair(avatarViewSpkr0, null))
                mapAvatarsSpkrs.put(1, Pair(avatarViewSpkr1, null))
                mapAvatarsSpkrs.put(2, Pair(avatarViewSpkr2, null))
                mapAvatarsSpkrs.put(3, Pair(avatarViewSpkr3, null))
                mapAvatarsSpkrs.put(4, Pair(avatarViewSpkr4, null))

                if (listSpeakersId.size == 0) binding.avatarLayoutLike.visibility = View.GONE
                else if (listSpeakersId.size < 6) {
                    for (i in 0..(listSpeakersId.size - 1)) {
                        userViewModel.getUserById(listSpeakersId[i]).join()
                        val userName = userViewModel.user.value?.name
                        val userAvatar = userViewModel.user.value?.avatar
                        var pair = mapAvatarsSpkrs.getValue(i)
                        if (userAvatar.isNullOrEmpty()) {
                            pair = pair.copy(second = userName)
                            mapAvatarsSpkrs.set(i, pair)
                        } else {
                            pair = pair.copy(second = userAvatar)
                            mapAvatarsSpkrs.set(i, pair)
                        }
                    }


                } else {
                    for (i in 0..4) {
                        userViewModel.getUserById(listSpeakersId[i]).join()
                        val userName = userViewModel.user.value?.name
                        val userAvatar = userViewModel.user.value?.avatar
                        var pair = mapAvatarsSpkrs.getValue(i)
                        if (userAvatar.isNullOrEmpty()) {
                            pair = pair.copy(second = userName)
                            mapAvatarsSpkrs.set(i, pair)
                        } else {
                            pair = pair.copy(second = userAvatar)
                            mapAvatarsSpkrs.set(i, pair)
                        }
                    }
                }

                mapAvatarsSpkrs.forEach {
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

        if (event.speakerIds.size <= 5) binding.btnSpkrsMore.visibility = View.GONE
        binding.btnSpkrsMore.setOnClickListener {
            Toast.makeText(context, "Открываем список лекторов", Toast.LENGTH_SHORT).show()
        }

        //Группа лайков и лайкеров
        val listLikersId = mutableListOf<Int>()
        event.likeOwnerIds.forEach {
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
                        userViewModel.getUserById(listLikersId[i]).join()
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
                        userViewModel.getUserById(listLikersId[i]).join()
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

        if (event.likeOwnerIds.size <= 5) binding.btnLikersMore.visibility = View.GONE
        binding.btnLikersMore.setOnClickListener {
            Toast.makeText(context, "Открываем список лайкеров", Toast.LENGTH_SHORT).show()
        }

        //Упомянутые и все все все
        val listMentId = mutableListOf<Int>()
        event.participantsIds.forEach {
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
                        userViewModel.getUserById(listMentId[i]).join()
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
                        userViewModel.getUserById(listMentId[i]).join()
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

        if (event.participantsIds.size <= 5) binding.btnMentMore.visibility = View.GONE
        binding.btnMentMore.setOnClickListener {
            Toast.makeText(context, "Открываем список упомянутых", Toast.LENGTH_SHORT).show()
        }

        //TODO сделать так чтобы кнопка выступающих была активна если я выступающий

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