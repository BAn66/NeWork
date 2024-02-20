package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentWallBinding
import javax.inject.Inject

@AndroidEntryPoint
class WallFragment:Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentWallBinding.inflate(layoutInflater)

        return  binding.root
    }

}