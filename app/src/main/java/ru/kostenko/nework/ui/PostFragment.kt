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
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth
    private val postViewModel: PostViewModel by activityViewModels()
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

        val post = postViewModel.post.value!!.copy()
        val observer = MediaLifecycleObserver()

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
            .apply(RequestOptions().circleCrop()) //делает круглыми аватарки
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
                //Не забываем добавлять разрешение в андроид манифест на работу с сетью
                val url = post.attachment!!.url
                mediaPlayer?.setDataSource(url) //TODO при нажатии на паузу аудиоплеера и повторном плэй падает
            }.play()
        }

        binding.pauseButton.setOnClickListener {
            if (observer.mediaPlayer != null) {
                if (observer.mediaPlayer!!.isPlaying) observer.mediaPlayer?.pause() else observer.mediaPlayer?.start()
            }
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

        binding.btnLike.setOnClickListener {//анимация лайка
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1F, 1.25F, 1F)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1F, 1.25F, 1F)
            ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY).apply {
                duration = 500
//                    repeatCount = 100
                interpolator = BounceInterpolator()
            }.start()
            postViewModel.likePostById(post.id, post.likedByMe)
        }

        //Для карты
        mapView = binding.mapview.apply {
            userLocation = MapKitFactory.getInstance().createUserLocationLayer(mapWindow)
            userLocation.isVisible = true
            userLocation.isHeadingEnabled = false

            val arguments = post.coords?.copy()
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


        //Группа лайков и лайкеров
        val listLikersId = mutableListOf<Int>()
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

                Log.d("KAva", "onCreateViewList: $listLikersId")
                if (listLikersId.size == 0) binding.avatarLayoutLike.visibility = View.GONE
                else if (listLikersId.size < 6) {
                    for (i in 0..(listLikersId.size - 1)) {
                        userViewModel.getUserById(listLikersId[i]).join()
                        val userName = userViewModel.user.value?.name
                        val userAvatar = userViewModel.user.value?.avatar
                        Log.d("KAva", "onCreateView1-1: $userAvatar")
                        var pair = mapAvatarsLikers.getValue(i)
                        if (userAvatar.isNullOrEmpty()) {
                            pair = pair.copy(second = userName)
                            Log.d("KAva", "onCreateView1-2-1: ${pair.second}")
                            mapAvatarsLikers.set(i, pair)
                            Log.d(
                                "KAva",
                                "onCreateView1-2-2: ${mapAvatarsLikers.getValue(i).second}"
                            )
                        } else {
                            pair = pair.copy(second = userAvatar)
                            Log.d("KAva", "onCreateView1-3-1: ${pair.second}")
                            mapAvatarsLikers.set(i, pair)
                            Log.d(
                                "KAva",
                                "onCreateView1-3-2: ${mapAvatarsLikers.getValue(i).second}"
                            )
                        }
                    }


                } else {
                    for (i in 0..4) {
                        userViewModel.getUserById(listLikersId[i]).join()
                        val userName = userViewModel.user.value?.name
                        val userAvatar = userViewModel.user.value?.avatar
                        Log.d("KAva", "onCreateView2-1: $userAvatar")
                        var pair = mapAvatarsLikers.getValue(i)
                        if (userAvatar.isNullOrEmpty()) {
                            pair = pair.copy(second = userName)
                            Log.d("KAva", "onCreateView2-2-1: ${pair.second}")
                            mapAvatarsLikers.set(i, pair)
                            Log.d(
                                "KAva",
                                "onCreateView2-2-2: ${mapAvatarsLikers.getValue(i).second}"
                            )
                        } else {
                            pair = pair.copy(second = userAvatar)
                            Log.d("KAva", "onCreateView2-3-1: ${pair.second}")
                            mapAvatarsLikers.set(i, pair)
                            Log.d(
                                "KAva",
                                "onCreateView2-3-2: ${mapAvatarsLikers.getValue(i).second}"
                            )
                        }
                    }
                }

                mapAvatarsLikers.forEach {
                    it.value.first.visibility = View.GONE
                    Log.d("KAva", "onCreateViewMap: ${it.value.second}")
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
                                    it.value.first.avatarInitials = it.value.second!!.substring(0, 1).uppercase()
                            }
                        }
                    }
                }
            }
        }

        if (post.likeOwnerIds.size <= 5) binding.btnLikersMore.visibility = View.GONE
        binding.btnLikersMore.setOnClickListener {
            Toast.makeText(context, "Открываем список лайкеров", Toast.LENGTH_SHORT).show()
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