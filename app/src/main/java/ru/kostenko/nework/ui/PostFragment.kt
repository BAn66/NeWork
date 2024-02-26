package ru.kostenko.nework.ui

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
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

import ru.kostenko.nework.R

import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentPostBinding
import ru.kostenko.nework.databinding.PlaceBinding
import ru.kostenko.nework.dto.AttachmentType

import ru.kostenko.nework.util.AndroidUtils
import ru.kostenko.nework.util.MediaLifecycleObserver
import ru.kostenko.nework.viewmodel.PostViewModel

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class PostFragment : Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth
    private val postViewModel: PostViewModel by activityViewModels()
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

        val post = postViewModel.post
        val observer: MediaLifecycleObserver = MediaLifecycleObserver()

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

        if (post.value?.attachment != null) {
            when (post.value!!.attachment!!.type) {
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
            if (post.value?.attachment != null && post.value!!.attachment!!.type == AttachmentType.IMAGE) View.VISIBLE else View.GONE

        binding.audioGroup.visibility =
            if (post.value?.attachment != null && post.value!!.attachment!!.type == AttachmentType.AUDIO) View.VISIBLE else View.GONE

        binding.videoGroup.visibility =
            if (post.value?.attachment != null && post.value!!.attachment!!.type == AttachmentType.VIDEO) View.VISIBLE else View.GONE

        post.value?.attachment?.apply {
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
                    Uri.parse(post.value!!.attachment!!.url)
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
                val url = post.value!!.attachment!!.url
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
            post.value?.likeOwnerIds?.size?.toLong()
                ?: 0
        )
        binding.btnLike.isChecked = post.value?.likedByMe!!

        binding.btnLike.setOnClickListener {//анимация лайка
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1F, 1.25F, 1F)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1F, 1.25F, 1F)
            ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY).apply {
                duration = 500
//                    repeatCount = 100
                interpolator = BounceInterpolator()
            }.start()
            postViewModel.likePostById(post.value?.id!!, post.value?.likedByMe!!)
        }

//            btnLike.setOnLongClickListener {
//                onPostInteractionListener.onOpenLikers(post)
//                true
//            }

        //Для карты
        mapView = binding.mapview.apply {
            userLocation = MapKitFactory.getInstance().createUserLocationLayer(mapWindow)
            userLocation.isVisible = true
            userLocation.isHeadingEnabled = false

            val arguments = post.value!!.coords
            //Создаем маркер на карте
            //переход к точке на карте после клика на списке
            if (arguments?.latC != null) {
                val collection = mapWindow.map.mapObjects.addCollection()
                collection.clear()
                val placeBinding = PlaceBinding.inflate(layoutInflater)
                collection.addPlacemark(
                    Point(arguments.latC, arguments.longC),
                    ViewProvider(placeBinding.root)
                )
                val cameraPosition = mapWindow.map.cameraPosition
                mapWindow.map.move(
                    CameraPosition(
                        Point(arguments.latC, arguments.longC),
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