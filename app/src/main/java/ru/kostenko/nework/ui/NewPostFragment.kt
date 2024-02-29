package ru.kostenko.nework.ui

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toFile
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentNewPostBinding
import ru.kostenko.nework.dto.AttachmentType
import ru.kostenko.nework.util.MediaLifecycleObserver
import ru.kostenko.nework.util.StringArg
import ru.kostenko.nework.viewmodel.PostViewModel


@AndroidEntryPoint
class NewPostFragment : Fragment() {

    companion object {
        var Bundle.text by StringArg
    }

    private lateinit var toolbar: Toolbar
    private val postViewModel: PostViewModel by activityViewModels()
    private val photoResultContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { // Контракт для картинок
            if (it.resultCode == Activity.RESULT_OK) {
//                val uri = Uri.parse(Uri_string)
                val uri = it.data?.data ?: return@registerForActivityResult
                val file = uri.toFile().inputStream()
                postViewModel.setMedia(uri, file, AttachmentType.IMAGE)
            }
        }
    private val videoResultContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { // Контракт для VIDEO
            it?.let {
                val stream = context?.contentResolver?.openInputStream(it)
                if (stream != null) {
                    postViewModel.setMedia(it, stream, AttachmentType.VIDEO)
                }
            }
        }

    private val audioResultContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { // Контракт для AUDIO
            it?.let {
                val stream = context?.contentResolver?.openInputStream(it)
                if (stream != null) {
                    postViewModel.setMedia(it, stream, AttachmentType.AUDIO)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val observer = MediaLifecycleObserver()
        val binding = FragmentNewPostBinding.inflate(layoutInflater)

        //Если есть черновик он вставляется
        binding.editTextNewPost.setText(postViewModel.content.value)

        //Верхний аппбар
        toolbar = binding.toolbar
        toolbar.apply {
            setTitle(R.string.newpost)
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener {
                if (binding.editTextNewPost.text.isNotBlank()) {
                    postViewModel.setContent(binding.editTextNewPost.text.toString())
                } else {
                    postViewModel.setContent("")
                }
                postViewModel.clearCoords()
                postViewModel.emptyNew()
                requireParentFragment().findNavController().navigate(R.id.action_newPostFragment_to_mainFragment)
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
                            postViewModel.changePostAndSave(content)
                            activity?.invalidateOptionsMenu()
                            requireParentFragment().findNavController()
                                .navigate(R.id.action_newPostFragment_to_mainFragment)
                        }
                        true
                    }

                    else -> false
                }
            }
        }

//        viewModel.postCreated.observe(viewLifecycleOwner) { //Работа с SingleLiveEvent: Остаемся на экране редактирования пока не придет ответ с сервера
//            viewModel.loadPosts()// не забываем обновить значения вью модели (запрос с сервера и загрузка к нам)
//            findNavController().popBackStack(R.id.postsFragment, false)
//}

        //Кнопка очистки фото
        binding.remove.setOnClickListener {
            postViewModel.clearMedia()
        }

        //Выбираем фото
        binding.takePhoto.setOnClickListener { //Берем фотку через галерею
            postViewModel.clearMedia()
            val pictureDialog = AlertDialog.Builder(it.context)
            pictureDialog.setTitle("Select Action")
            val pictureDialogItems =
                arrayOf("Select photo from gallery", "Capture photo from camera")
            pictureDialog.setItems(
                pictureDialogItems
            ) { dialog, which ->
                when (which) {
                    0 -> choosePhotoFromGallary()
                    1 -> takePhotoFromCamera()
                }
            }
            pictureDialog.show()
        }

        //Выбираем видео или аудио
        binding.takeFile.setOnClickListener {
            postViewModel.clearMedia()
            val pictureDialog = AlertDialog.Builder(it.context)
            pictureDialog.setTitle("Select Action")
            val pictureDialogItems =
                arrayOf("Select video", "Select audio")
            pictureDialog.setItems(
                pictureDialogItems
            ) { dialog, which ->
                when (which) {
                    0 -> takeVideo()
                    1 -> takeAudio()
                }
            }
            pictureDialog.show()

        }

        postViewModel.media.observe(viewLifecycleOwner) { mediaModel ->
            Log.d("ImgTAAG", "postViewModel2: ${postViewModel.media.value?.uri} ")
            if (mediaModel == null) {
                binding.imageContainer.isGone = true
                binding.videoGroup.isGone = true
                binding.audioGroup.isGone = true
                return@observe
            }

            if (mediaModel.type == AttachmentType.IMAGE) {
                binding.imageContainer.isVisible = true
                binding.preview.setImageURI(mediaModel.uri)
                postViewModel.setContent(binding.editTextNewPost.text.toString())


            }
            if (mediaModel.type == AttachmentType.VIDEO) {
                binding.videoGroup.isVisible = true
                postViewModel.setContent(binding.editTextNewPost.text.toString())

            }
            if (mediaModel.type == AttachmentType.AUDIO) {
                binding.audioGroup.isVisible = true
                postViewModel.setContent(binding.editTextNewPost.text.toString())
            }
        }

        postViewModel.content.observe(viewLifecycleOwner) {
            binding.editTextNewPost.setText(postViewModel.content.value)
        }


        binding.play.setOnClickListener {
            binding.videoContent.apply {
                setMediaController(MediaController(context))
                setVideoURI(
                    postViewModel.media.value?.uri
                )
                setOnPreparedListener {
                    start()
                }
                setOnCompletionListener {
                    stopPlayback()
                }
            }
        }

        //TODO в фрагменте нового поста не проигрывается аудио
        binding.playButton.setOnClickListener {
            observer.apply {
                //Не забываем добавлять разрешение в андроид манифест на работу с сетью
                val uri = postViewModel.media.value?.uri
//                    observer.mediaPlayer?.setDataSource(requireContext(), uri)
                observer.mediaPlayer?.setDataSource(uri.toString())
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


        binding.takeLocation.setOnClickListener {
            postViewModel.setContent(binding.editTextNewPost.text.toString())
            requireParentFragment()
                .findNavController().navigate(R.id.action_newPostFragment_to_mapFragment)
        }

        //Для редактирования поста
        if (postViewModel.edited.value?.id != 0) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    postViewModel.edited.observe(viewLifecycleOwner) { //Не факт что сработает)
                        val resultPost = it.copy()
                        postViewModel.setContent(resultPost.content)
                        binding.editTextNewPost.setText(postViewModel.content.value)
                        postViewModel.setContent(binding.editTextNewPost.toString())
                        if (resultPost.coords != null) {
                            postViewModel.setCoords(
                                resultPost.coords.lat,
                                resultPost.coords.long
                            )
                        }

                        if (resultPost.attachment != null) {
                            when (resultPost.attachment.type) {
                                AttachmentType.IMAGE -> binding.imageContainer .visibility = View.VISIBLE
                                AttachmentType.AUDIO -> binding.audioGroup.visibility = View.VISIBLE
                                AttachmentType.VIDEO -> binding.videoGroup.visibility = View.VISIBLE
                            }
                        } else {
                            binding.imageContainer.visibility = View.GONE
                            binding.audioGroup.visibility = View.GONE
                            binding.videoGroup.visibility = View.VISIBLE
                        }

                        binding.imageContainer.visibility =
                            if (resultPost.attachment != null && resultPost.attachment.type == AttachmentType.IMAGE) View.VISIBLE else View.GONE

                        binding.audioGroup.visibility =
                            if (resultPost.attachment != null && resultPost.attachment.type == AttachmentType.AUDIO) View.VISIBLE else View.GONE

                        binding.videoGroup.visibility =
                            if (resultPost.attachment != null && resultPost.attachment.type == AttachmentType.VIDEO) View.VISIBLE else View.GONE

                        resultPost.attachment?.apply {
                            binding.preview.contentDescription = this.url
                            Glide.with(binding.preview)
                                .load(this.url)
                                .placeholder(R.drawable.ic_loading_100dp)
                                .error(R.drawable.ic_error_100dp)
                                .timeout(10_000)
                                .into(binding.preview)
                        }

                        binding.play.setOnClickListener {
                            binding.videoContent.apply {
                                setMediaController(MediaController(context))
                                setVideoURI(
                                    Uri.parse(resultPost.attachment!!.url)
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
                                val url = resultPost.attachment!!.url
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

                    }
                }
            }
        }


//TODO сделать кнопку очистки для видео и аудио(может использовать уже имеющуюся?) в новом посте
        //TODO Сделать выбор отмеченных пользователей в новом посте

        return binding.root
    }

    fun choosePhotoFromGallary() {
        ImagePicker.Builder(this)
            .crop()
            .galleryOnly()
            .maxResultSize(2048, 2048)
            .createIntent(
                photoResultContract::launch
            )
    }

    private fun takePhotoFromCamera() {
        ImagePicker.Builder(this)
            .crop()
            .cameraOnly()
            .maxResultSize(2048, 2048)
            .createIntent(
                photoResultContract::launch
            )
    }

    fun takeVideo() {
        videoResultContract.launch("video/*")
    }

    fun takeAudio() {
        audioResultContract.launch("audio/*")
    }

}