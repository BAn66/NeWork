package ru.kostenko.nework.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
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
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentAuthBinding
import ru.kostenko.nework.databinding.FragmentNewPostBinding
import ru.kostenko.nework.dto.Post
import ru.kostenko.nework.util.StringArg
import ru.kostenko.nework.viewmodel.PostViewModel
import java.util.Collections.copy

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    companion object {
        var Bundle.text by StringArg
    }
    private lateinit var toolbar_login: Toolbar
    private val viewModel: PostViewModel by activityViewModels()
    private val mediaResultContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { // Контракт для картинок
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data ?: return@registerForActivityResult
                val file = uri.toFile()
//                viewModel.setMedia(uri, file)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(layoutInflater)

        //Верхний аппбар
        toolbar_login = binding.toolbar
        toolbar_login.apply {
            setTitle(R.string.newpost)
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }

        //кнопка сохранения
        var showMenu = false
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                showMenu = true
                menuInflater.inflate(R.menu.save_feed_item, menu)

            }

            override fun onPrepareMenu(menu: Menu) {
                menu.findItem(R.id.save).isVisible = showMenu
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
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
                            showMenu = false
                            activity?.invalidateOptionsMenu()
                        }
                        true
                    }
                    else -> false
                }

            override fun onMenuClosed(menu: Menu) {}
        }, viewLifecycleOwner)

        //        Для загрузки черновика
        setFragmentResultListener("requestSavedTmpContent") { key, bundle ->
            val savedTmpContent = bundle.getString("savedTmpContent")
            binding.editTextNewPost.setText(savedTmpContent)
        }

        //Для редактирования поста
        setFragmentResultListener("requestIdForNewPostFragment") { key, bundle ->
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
                            //todo добавить загрузку картинки при редактировании картинки
                        }
                    }
                }
            }
        }

        viewModel.postCreated.observe(viewLifecycleOwner) { //Работа с SingleLiveEvent: Остаемся на экране редактирования пока не придет ответ с сервера
            viewModel.loadPosts()// не забываем обновить значения вью модели (запрос с сервера и загрузка к нам)
            findNavController().popBackStack(R.id.postsFragment, false)

        }

        //Кнопка очистки фото
        binding.remove.setOnClickListener {
            viewModel.clearMedia()
        }

        binding.takePhoto.setOnClickListener { //Берем фотку через галерею
            ImagePicker.Builder(this)
                .crop()
                .galleryOnly()
                .maxResultSize(2048, 2048)
                .createIntent(mediaResultContract::launch)
        }
        viewModel.media.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.imageContainer.isGone = true
                return@observe
            }
            binding.imageContainer.isVisible = true
            binding.preview.setImageURI(it.uri)
        }

        return binding.root
    }

}