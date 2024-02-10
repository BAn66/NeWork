package ru.kostenko.nework.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentRegistrationBinding
import ru.kostenko.nework.viewmodel.LoginViewModel

class RegistrationFragment: Fragment() {
    private lateinit var toolbar_registration: Toolbar
    private val viewModel: LoginViewModel by activityViewModels()

    private val photoResultContract =// Контракт для картинок
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data ?: return@registerForActivityResult
                val file = uri.toFile()
                viewModel.setPhoto(uri, file)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegistrationBinding.inflate(layoutInflater)
        toolbar_registration = binding.toolbar
        toolbar_registration.apply {
            setTitle(R.string.registration)
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener { view ->
                findNavController().navigate(R.id.action_registrationFragment_to_mainFragment)
            }
        }


        binding.avatar.setOnClickListener { //Берем фотку через галерею
            ImagePicker.Builder(this)
                .crop()
                .galleryOnly()
                .maxResultSize(2048, 2048)
                .createIntent(photoResultContract::launch)
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            binding.avatar.setImageURI(it?.uri)
        }

        binding.loginBtn.setOnClickListener {
            findNavController().popBackStack()
        }



        return  binding.root

    }
}