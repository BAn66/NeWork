package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.R
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentUserDetailsBinding
import ru.kostenko.nework.viewmodel.AuthViewModel
import ru.kostenko.nework.viewmodel.UserViewModel
import javax.inject.Inject

@AndroidEntryPoint
class UserDetailsFragment: Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentUserDetailsBinding.inflate(inflater, container, false)

        //Навигация через табы
        val navController = requireNotNull(
            childFragmentManager.findFragmentById(R.id.nav_user_fragment_main)
        ).findNavController()
        binding.navUserView.setupWithNavController(navController)

        //Данные о пользователе из userViewModel
        val user = userViewModel.user
        val nameUser = user.value?.name
        val loginUser = user.value?.login

        //Наполняем верхний аппбар
        toolbar = binding.toolbar
        toolbar.apply {
            setTitle("$nameUser / $loginUser")
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener { findNavController().popBackStack() }
        }

        //Установка аватарки в качестве фото профиля
        Glide.with(binding.userPhoto)
            .load(user.value?.avatar)
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.video_holder)
            .timeout(10_000)
            .into(binding.userPhoto)

        return binding.root
    }
}