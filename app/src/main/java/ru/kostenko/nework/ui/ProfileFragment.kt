package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.R
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentProfileBinding
import ru.kostenko.nework.viewmodel.AuthViewModel
import ru.kostenko.nework.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth
//    private val postViewModel: PostViewModel by activityViewModels()
//    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var toolbar_login: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentProfileBinding.inflate(layoutInflater)
        toolbar_login = binding.toolbar
        toolbar_login.apply {
            setTitle(R.string.profile)
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener { findNavController().popBackStack() }


        }

        binding.signOut.setOnClickListener {
            appAuth.removeAuth()
            findNavController().popBackStack()
        }

        return binding.root
    }
}