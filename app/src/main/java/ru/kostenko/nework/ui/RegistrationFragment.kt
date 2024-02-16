package ru.kostenko.nework.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentRegistrationBinding
import ru.kostenko.nework.repository.AuthResultCode
import ru.kostenko.nework.viewmodel.LoginViewModel

@AndroidEntryPoint
class RegistrationFragment: Fragment() {
    private lateinit var toolbar_registration: Toolbar
    private val viewModel: LoginViewModel by activityViewModels()

    private val photoResultContract =// Контракт для картинок
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data ?: return@registerForActivityResult
                val file = uri.toFile()
                viewModel.setPhoto(uri, file)
            } else if (it.resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(context, ImagePicker.getError(it.data), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
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
            setNavigationOnClickListener {
                findNavController().navigate(R.id.action_registrationFragment_to_mainFragment)
            }
        }


        binding.avatar.setOnClickListener { //Берем фотку через галерею
            ImagePicker.Builder(this)
                .galleryMimeTypes(
                    mimeTypes = arrayOf(
                        "image/png",
                        "image/jpeg"
                    )
                )
                .crop()
                .galleryOnly()
                .maxResultSize(2048, 2048)
                .createIntent(photoResultContract::launch)

        }

        viewModel.photo.observe(viewLifecycleOwner) {
            binding.avatar.setImageURI(it?.uri)
        }

        binding.loginBtn.setOnClickListener {
            if (binding.editLogin.text.isNullOrBlank() && binding.editPassword.text.isNullOrBlank()) {
                Toast.makeText(context, R.string.error_blank_auth, Toast.LENGTH_LONG).show()
            } else if (binding.editLogin.text.isNullOrBlank()) {
                Toast.makeText(context, R.string.error_blank_username, Toast.LENGTH_SHORT).show()
            } else if (binding.editName.text.isNullOrBlank()) {
                Toast.makeText(context, R.string.error_blank_name, Toast.LENGTH_SHORT).show()
            } else if (binding.editPassword.text.isNullOrBlank()) {
                Toast.makeText(context, R.string.error_blank_password, Toast.LENGTH_SHORT).show()
            } else if (binding.editPasswordRepeat.text.isNullOrBlank()) {
                Toast.makeText(context, R.string.error_blank_password_repeat_text, Toast.LENGTH_SHORT).show()
            } else if (binding.editPasswordRepeat.text.toString() != binding.editPassword.text.toString()) {
                Toast.makeText(context, R.string.error_blank_pass_notequals_passre, Toast.LENGTH_SHORT).show()
            } else {

                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        when (viewModel.sendRequest(
                            binding.editLogin.text.toString(),
                            binding.editPassword.text.toString(),
                            binding.editName.text.toString()
                        )){
                            AuthResultCode.UserAlreadyRegister -> Toast.makeText(
                                context,
                                R.string.user_already_register,
                                Toast.LENGTH_SHORT
                            ).show()

                            AuthResultCode.WrongFormatMedia -> Toast.makeText(
                                context,
                                R.string.wrong_format_media,
                                Toast.LENGTH_SHORT
                            ).show()

                            AuthResultCode.Success -> findNavController().navigate(R.id.action_authFragment_to_mainFragment)

                            else -> Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT)
                                .show()
                        }

                    }
                }
            }

        }

        return  binding.root

    }
}