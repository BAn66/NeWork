package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentAuthBinding
import ru.kostenko.nework.util.AndroidUtils.focusAndShowKeyboard
import ru.kostenko.nework.util.StringArg

@AndroidEntryPoint
class AuthFragment:Fragment() {
    private lateinit var toolbar_login: Toolbar
    companion object {
        var Bundle.textLogin by StringArg
        var Bundle.textPassword by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuthBinding.inflate(layoutInflater)

        toolbar_login = binding.toolbar
        toolbar_login.apply{
            setTitle(R.string.login)
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener { view ->
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
//                LoginViewModel.updateUser(
//                    binding.editLogin.text.toString(),
//                    binding.editPassword.text.toString()
//                )
                Toast.makeText(context, "Авторизация прошла", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_authFragment_to_mainFragment)
            }
        }

        return binding.root
    }
}