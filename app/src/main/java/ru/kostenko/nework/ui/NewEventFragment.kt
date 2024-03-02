package ru.kostenko.nework.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentNewEventBinding
import ru.kostenko.nework.dto.AttachmentType
import ru.kostenko.nework.dto.EventType
import ru.kostenko.nework.util.MediaLifecycleObserver
import ru.kostenko.nework.util.StringArg
import ru.kostenko.nework.viewmodel.EventViewModel
import java.time.OffsetDateTime


@AndroidEntryPoint
class NewEventFragment : Fragment() {

    companion object {
        var Bundle.text by StringArg
    }

    private lateinit var toolbar: Toolbar
    private val eventViewModel: EventViewModel by activityViewModels()

    private val photoResultContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { // Контракт для картинок
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data ?: return@registerForActivityResult
                val file = uri.toFile().inputStream()
                eventViewModel.setMedia(uri, file, AttachmentType.IMAGE)
            }
        }
    private val videoResultContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { // Контракт для VIDEO
            it?.let {
                val stream = context?.contentResolver?.openInputStream(it)
                if (stream != null) {
                    eventViewModel.setMedia(it, stream, AttachmentType.VIDEO)
                }
            }
        }

    private val audioResultContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { // Контракт для AUDIO
            it?.let {
                val stream = context?.contentResolver?.openInputStream(it)
                if (stream != null) {
                    eventViewModel.setMedia(it, stream, AttachmentType.AUDIO)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val observer = MediaLifecycleObserver()
        val binding = FragmentNewEventBinding.inflate(layoutInflater)

        binding.editTextNewPost.setText(eventViewModel.content.value)
        binding.editTextNewPost.requestFocus()

        //Верхний аппбар
        toolbar = binding.toolbar
        toolbar.apply {
            setTitle(R.string.new_event)
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener {
                if (binding.editTextNewPost.text.isNotBlank()) {
                    eventViewModel.setContent(binding.editTextNewPost.text.toString())
                } else {
                    eventViewModel.setContent("")
                }
                eventViewModel.clearCoords()
                requireParentFragment().findNavController()
                    .navigate(R.id.action_newEventFragment_to_mainFragment)
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
                            val dateTime =
                            if(eventViewModel.datetime.value.isNullOrEmpty()){
                                OffsetDateTime.now().toString()
                            } else eventViewModel.datetime.value.toString()
                            // Первоначальная заглушка
                            eventViewModel.changeEventAndSave(content, dateTime, EventType.ONLINE)


                            activity?.invalidateOptionsMenu()
                            findNavController()
                                .navigate(R.id.action_newEventFragment_to_mainFragment)
                        }
                        true
                    }

                    else -> false
                }
            }
        }
//Кнопка очистки фото
        binding.remove.setOnClickListener {
            eventViewModel.clearMedia()
        }

        //Выбираем фото
        binding.takePhoto.setOnClickListener { //Берем фотку через галерею
            eventViewModel.clearMedia()
            val pictureDialog = AlertDialog.Builder(it.context)
            pictureDialog.setTitle("Select Action")
            val pictureDialogItems =
                arrayOf("Select photo from gallery", "Capture photo from camera")
            pictureDialog.setItems(
                pictureDialogItems
            ) { _, which ->
                when (which) {
                    0 -> choosePhotoFromGallary()
                    1 -> takePhotoFromCamera()
                }
            }
            pictureDialog.show()
        }

        //Выбираем видео или аудио
        binding.takeFile.setOnClickListener {
            eventViewModel.clearMedia()
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
        eventViewModel.media.observe(viewLifecycleOwner) { mediaModel ->
            if (mediaModel == null) {
                binding.imageContainer.isGone = true
                binding.videoGroup.isGone = true
                binding.audioGroup.isGone = true
                return@observe
            }

            if (mediaModel.type == AttachmentType.IMAGE) {
                binding.imageContainer.isVisible = true
                binding.preview.setImageURI(mediaModel.uri)
                eventViewModel.setContent(binding.editTextNewPost.text.toString())


            }
            if (mediaModel.type == AttachmentType.VIDEO) {
                binding.videoGroup.isVisible = true
                eventViewModel.setContent(binding.editTextNewPost.text.toString())

            }
            if (mediaModel.type == AttachmentType.AUDIO) {
                binding.audioGroup.isVisible = true
                eventViewModel.setContent(binding.editTextNewPost.text.toString())
            }
        }

        eventViewModel.content.observe(viewLifecycleOwner) {
            binding.editTextNewPost.setText(eventViewModel.content.value)
        }


        binding.play.setOnClickListener {
            binding.videoContent.apply {
                setMediaController(MediaController(context))
                setVideoURI(
                    eventViewModel.media.value?.uri
                )
                setOnPreparedListener {
                    start()
                }
                setOnCompletionListener {
                    stopPlayback()
                }
            }
        }

        //Переход на экран с картой
        binding.takeLocation.setOnClickListener {
            eventViewModel.setContent(binding.editTextNewPost.text.toString())
            requireParentFragment()
                .findNavController().navigate(R.id.action_newEventFragment_to_mapFragment)
        }



        //Вызов диалога для установки даты и вида событий
        binding.addDateEvent.setOnClickListener {
            val modalBottomSheet = DateEventFragment()
            modalBottomSheet.show(parentFragmentManager, DateEventFragment.TAG)
//            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        //Для редактирования поста
        eventViewModel.edited.observe(viewLifecycleOwner) { editedEvent ->
            if (editedEvent.id != 0) {
                eventViewModel.setContent(editedEvent.content)
                binding.editTextNewPost.requestFocus()
                // TODO не редактирует локацию и если не менять медиа не сохраняет текст
                editedEvent.attachment?.let { attachment ->
                    val type = attachment.type
                    val url = attachment.url
                    eventViewModel.setMedia(url.toUri(), null, type)
                    if (type == AttachmentType.IMAGE) {
                        binding.imageContainer.visibility = View.VISIBLE
//                                binding.preview.setImageURI(url.toUri())
                        editedEvent.attachment.apply {
//                                    imageAttach.contentDescription = this.url
                            Glide.with(binding.preview)
                                .load(url)
                                .placeholder(R.drawable.ic_loading_100dp)
                                .error(R.drawable.ic_error_100dp)
                                .timeout(10_000)
                                .into(binding.preview)
                        }

                    }
                }
            }
        }

        return binding.root

        //TODO сделать кнопку очистки для видео и аудио(может использовать уже имеющуюся?) в событии
        //TODO Сделать выбор спикеров в событии
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