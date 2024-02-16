package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentAuthBinding
import ru.kostenko.nework.repository.AuthResultCode
import ru.kostenko.nework.util.AndroidUtils.focusAndShowKeyboard
import ru.kostenko.nework.util.StringArg
import ru.kostenko.nework.viewmodel.LoginViewModel

@AndroidEntryPoint
class AuthFragment : Fragment() {
    private lateinit var toolbar_login: Toolbar

    companion object {
        var Bundle.textLogin by StringArg
        var Bundle.textPassword by StringArg
    }

    private val viewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuthBinding.inflate(layoutInflater)
        toolbar_login = binding.toolbar
        toolbar_login.apply {
            setTitle(R.string.login)
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }

        val textLogin =
            arguments?.textLogin//получение аргументов между фрагментами при создании нового поста из старого в choosere
        if (textLogin != null) {
            binding.editLogin.setText(textLogin)
        }

        val textPassword =
            arguments?.textPassword//получение аргументов между фрагментами при создании нового поста из старого в choosere
        if (textLogin != null) {
            binding.editPassword.setText(textPassword)
        }

        binding.editLogin.requestFocus()
        binding.editLogin.focusAndShowKeyboard()
        binding.editPassword.focusAndShowKeyboard()



        binding.loginBtn.setOnClickListener {
            if (binding.editLogin.text.isNullOrBlank() && binding.editPassword.text.isNullOrBlank()) {
                Toast.makeText(context, R.string.error_blank_auth, Toast.LENGTH_LONG).show()
            } else if (binding.editLogin.text.isNullOrBlank()) {
                Toast.makeText(context, R.string.error_blank_username, Toast.LENGTH_SHORT).show()
            } else if (binding.editPassword.text.isNullOrBlank()) {
                Toast.makeText(context, R.string.error_blank_password, Toast.LENGTH_SHORT).show()
            } else {

                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        when (viewModel.sendRequest(
                            binding.editLogin.text.toString(),
                            binding.editPassword.text.toString()
                        )) {
                            AuthResultCode.IncorrectPassword -> Toast.makeText(
                                context,
                                R.string.wrong_login_or_pass,
                                Toast.LENGTH_SHORT
                            ).show()

                            AuthResultCode.UserNotFound -> Toast.makeText(
                                context,
                                R.string.user_not_found,
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

        binding.register.setOnClickListener{
            findNavController().navigate(R.id.action_authFragment_to_registrationFragment)
        }

        return binding.root
    }
}