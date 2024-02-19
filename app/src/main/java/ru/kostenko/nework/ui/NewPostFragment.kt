package ru.kostenko.nework.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.util.StringArg
import ru.kostenko.nework.viewmodel.PostViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    companion object {
        var Bundle.text by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()
    private val mediaResultContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { // Контракт для картинок
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data ?: return@registerForActivityResult
                val file = uri.toFile()
//                viewModel.setMedia(uri, file)
            }
        }

}