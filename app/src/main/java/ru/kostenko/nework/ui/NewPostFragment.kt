package ru.kostenko.nework.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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

    private val postViewModel: PostViewModel by activityViewModels()
    private val photoResultContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {

                val uri = it.data?.data ?: return@registerForActivityResult
                val file = uri.toFile().inputStream()
                postViewModel.setMedia(uri, file, AttachmentType.IMAGE)
            }
        }
    private val videoResultContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let {
                val stream = context?.contentResolver?.openInputStream(it)
                if (stream != null) {
                    postViewModel.setMedia(it, stream, AttachmentType.VIDEO)
                }
            }
        }

    private val audioResultContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
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
        binding.editTextNewPost.setText(postViewModel.content.value)
        binding.editTextNewPost.requestFocus()

        val toolbar = binding.toolbar
        toolbar.apply {
            setTitle(R.string.newpost)
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener {
                if (binding.editTextNewPost.text.isNotBlank()) {
                    postViewModel.setContent(binding.editTextNewPost.text.toString())
                } else {
                    postViewModel.setContent("")
                }
                postViewModel.emptyNew()
                requireParentFragment().findNavController()
                    .navigate(R.id.action_newPostFragment_to_mainFragment)
            }

            inflateMenu(R.menu.save_feed_item)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.save -> {
                        if (binding.editTextNewPost.text.isBlank()
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

        binding.remove.setOnClickListener {
            postViewModel.clearMedia()
        }


        binding.takePhoto.setOnClickListener {
            postViewModel.setContent(binding.editTextNewPost.text.toString())
            postViewModel.clearMedia()
            val pictureDialog = AlertDialog.Builder(it.context)
            pictureDialog.setTitle(R.string.select_action)
            val str1 = getString(R.string.select_gallary)
            val str2 = getString(R.string.select_camera)
            val pictureDialogItems =
                arrayOf(str1, str2)
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

        binding.editTextNewPost.setOnClickListener {
            postViewModel.setContent(binding.editTextNewPost.text.toString())
        }

        binding.takeFile.setOnClickListener {
            postViewModel.setContent(binding.editTextNewPost.text.toString())
            postViewModel.clearMedia()
            val videoDialog = AlertDialog.Builder(it.context)
            videoDialog.setTitle(R.string.select_action)
            val str1 = getString(R.string.select_video)
            val str2 = getString(R.string.select_audio)
            val videoDialogItems =
                arrayOf(str1, str2)
            videoDialog.setItems(
                videoDialogItems
            ) { _, which ->
                when (which) {
                    0 -> takeVideo()
                    1 -> takeAudio()
                }
            }
            videoDialog.show()

        }

        postViewModel.media.observe(viewLifecycleOwner) { mediaModel ->
            if (mediaModel == null) {
                binding.imageContainer.isGone = true
                binding.videoGroup.isGone = true
                binding.audioGroup.isGone = true
                return@observe
            }

            if (mediaModel.type == AttachmentType.IMAGE) {
                binding.imageContainer.isVisible = true
                binding.videoGroup.isGone = true
                binding.audioGroup.isGone = true
                binding.preview.setImageURI(mediaModel.uri)
                postViewModel.setContent(binding.editTextNewPost.text.toString())


            }
            if (mediaModel.type == AttachmentType.VIDEO) {
                binding.videoGroup.isVisible = true
                binding.imageContainer.isGone = true
                binding.audioGroup.isGone = true
                postViewModel.setContent(binding.editTextNewPost.text.toString())

            }
            if (mediaModel.type == AttachmentType.AUDIO) {
                binding.audioGroup.isVisible = true
                binding.imageContainer.isGone = true
                binding.videoGroup.isGone = true
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


        binding.playButton.setOnClickListener {
            observer.apply {
                val uri = postViewModel.media.value?.uri
                observer.mediaPlayer?.setDataSource(uri.toString())
            }.play()
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

        postViewModel.edited.observe(viewLifecycleOwner) { editedPost ->
            if (editedPost.id != 0) {
                postViewModel.setContent(editedPost.content)
                binding.editTextNewPost.requestFocus()

                editedPost.attachment?.let { attachment ->
                    val type = attachment.type
                    val url = attachment.url
                    postViewModel.setMedia(url.toUri(), null, type)
                    if (type == AttachmentType.IMAGE) {
                        binding.imageContainer.visibility = View.VISIBLE
                        editedPost.attachment.apply {
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

        binding.takePeople.setOnClickListener {
            postViewModel.setContent(binding.editTextNewPost.text.toString())
            requireParentFragment().findNavController()
                .navigate(R.id.action_newPostFragment_to_takePeopleFragment)
        }

        return binding.root
    }

    private fun choosePhotoFromGallary() {
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

    private fun takeVideo() {
        videoResultContract.launch("video/*")
    }

    private fun takeAudio() {
        audioResultContract.launch("audio/*")
    }

}