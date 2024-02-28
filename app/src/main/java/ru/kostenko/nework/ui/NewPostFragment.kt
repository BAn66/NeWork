package ru.kostenko.nework.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentNewPostBinding
import ru.kostenko.nework.dto.AttachmentType
import ru.kostenko.nework.util.MediaLifecycleObserver
import ru.kostenko.nework.util.StringArg
import ru.kostenko.nework.viewmodel.PostViewModel
import java.io.File


@AndroidEntryPoint
class NewPostFragment : Fragment() {

    companion object {
        var Bundle.text by StringArg
    }

    private lateinit var toolbar_login: Toolbar
    private val viewModel: PostViewModel by activityViewModels()
    private val photoResultContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { // Контракт для картинок
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data ?: return@registerForActivityResult
                val file = uri.toFile().inputStream()
                viewModel.setMedia(uri, file, AttachmentType.IMAGE)
            }
        }
    private val videoResultContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { // Контракт для VIDEO
            it?.let {
                val stream = context?.contentResolver?.openInputStream(it)
                if (stream != null) {
                    viewModel.setMedia(it, stream, AttachmentType.VIDEO)
                }
            }
        }

    private val audioResultContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { // Контракт для VIDEO
            it?.let {
                val stream = context?.contentResolver?.openInputStream(it)
                if (stream != null) {
                    viewModel.setMedia(it, stream, AttachmentType.AUDIO)
                }
            }
        }

//    private val AudioResultContract =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { // Контракт для AUDIO
//            if (it.resultCode == Activity.RESULT_OK) {
//                val uri = it.data?.data ?: return@registerForActivityResult
//                val file = uri.toFile().inputStream()
//                viewModel.setMedia(uri, file, AttachmentType.AUDIO)
//            }
//        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.clearMedia()
        val observer: MediaLifecycleObserver = MediaLifecycleObserver()
        val binding = FragmentNewPostBinding.inflate(layoutInflater)

        //Верхний аппбар
        toolbar_login = binding.toolbar
        toolbar_login.apply {
            setTitle(R.string.newpost)
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener {
                if (binding.editTextNewPost.text.isNotBlank()) {
                    viewModel.setContent(binding.editTextNewPost.text.toString())
                } else {
                    viewModel.setContent("")
                }
//                //Для отправки черновика
//                parentFragmentManager.setFragmentResult(
//                    "saveTmpContent",
//                    bundleOf("tmpContent" to tmpContent)
//                )
                findNavController().popBackStack()
            }
            inflateMenu(R.menu.save_feed_item)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.save -> {
                        if (!binding.editTextNewPost.text.isNotBlank()
                        ) {
                            Snackbar.make(
                                binding.root, R.string.error_empty_content,
                                BaseTransientBottomBar.LENGTH_INDEFINITE
                            )
                                .setAction(android.R.string.ok) {
                                }.show()
                        } else {
                            val content = binding.editTextNewPost.text.toString()
                            viewModel.changePostAndSave(content)
                            activity?.invalidateOptionsMenu()
                            findNavController()
                                .navigate(R.id.action_newPostFragment_to_mainFragment)
                        }
                        true
                    }
                    else -> false
                }
            }
        }

        //        Для загрузки черновика
        setFragmentResultListener("takeTmpContent") { _, bundle ->
            val savedTmpContent = bundle.getString("savedTmpContent")
            binding.editTextNewPost.setText(savedTmpContent)
        }


        //Для редактирования поста
        setFragmentResultListener("requestIdForNewPostFragment") { _, bundle ->
            // Здесь можно передать любой тип, поддерживаемый Bundle-ом
            val resultId = bundle.getInt("id")
            if (resultId != 0) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.edited.observe(viewLifecycleOwner) { //Не факт что сработает)
                            if (it.id == resultId) {
                                val resultPost = it.copy()
                                viewModel.editPost(resultPost)
                                binding.editTextNewPost.setText(resultPost.content)
                            }
                        }
                    }
                }
            }
        }

//        viewModel.postCreated.observe(viewLifecycleOwner) { //Работа с SingleLiveEvent: Остаемся на экране редактирования пока не придет ответ с сервера
//            viewModel.loadPosts()// не забываем обновить значения вью модели (запрос с сервера и загрузка к нам)
//            findNavController().popBackStack(R.id.postsFragment, false)
//
//        }

        //Кнопка очистки фото
        binding.remove.setOnClickListener {
            viewModel.clearMedia()
        }

        binding.takePhoto.setOnClickListener { //Берем фотку через галерею
            viewModel.clearMedia()
            ImagePicker.Builder(this)
                .crop()
                .galleryOnly()
                .maxResultSize(2048, 2048)
                .createIntent(
                    photoResultContract::launch
                )
        }

        binding.takeFile.setOnClickListener {
            viewModel.clearMedia()
            videoResultContract.launch("video/*")
        }

        binding.takeAudio.setOnClickListener {
            viewModel.clearMedia()
            audioResultContract.launch("audio/*")
        }

        viewModel.media.observe(viewLifecycleOwner) { mediaModel ->
            if (mediaModel == null) {
                binding.imageContainer.isGone = true
                binding.videoGroup.isGone = true
                binding.audioGroup.isGone = true
                return@observe
            }

            if (mediaModel.type == AttachmentType.IMAGE) {
                binding.imageContainer.isVisible = true
                binding.preview.setImageURI(mediaModel.uri)
            }
            if (mediaModel.type == AttachmentType.VIDEO) {
                binding.videoGroup.isVisible = true
                binding.videoContent.apply {
                    setMediaController(MediaController(this.context))
                    setVideoURI(mediaModel.uri)
                }
            }
            if (mediaModel.type == AttachmentType.AUDIO) {
                binding.audioGroup.isVisible = true
                observer.apply {
                    //Не забываем добавлять разрешение в андроид манифест на работу с сетью
                    val url = mediaModel.uri.toString()
                    observer.mediaPlayer?.setDataSource(url)
                    //TODO при нажатии на паузу аудиоплеера и повторном плэй падает
                }
            }
        }

        binding.play.setOnClickListener {
            binding.videoContent.apply {
                setMediaController(MediaController(context))
                setOnPreparedListener {
                    start()
                }
                setOnCompletionListener {
                    stopPlayback()
                }
            }
        }

        binding.playButton.setOnClickListener {
            if (observer.mediaPlayer != null) {
                observer.play()
            }
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

        //TODO при переходе на карту и обратно не сохранаяется напечатанный текст
        binding.takeLocation.setOnClickListener {
            requireParentFragment()
                .findNavController().navigate(R.id.action_newPostFragment_to_mapFragment)
        }


        return binding.root
    }

}