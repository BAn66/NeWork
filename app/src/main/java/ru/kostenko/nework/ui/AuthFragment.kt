package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentAuthBinding

class AuthFragment:Fragment() {
    private lateinit var toolbar_login: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuthBinding.inflate(layoutInflater)

        /*Верхнее меню*/
        toolbar_login = binding.root.findViewById<Toolbar>(R.id.toolbar)
        toolbar_login.apply{
            setTitle(R.string.login)
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener { view ->
                findNavController().popBackStack()
            }
        }

        binding.login.setOnClickListener{
            var auth: Boolean = true
            if (auth){
                findNavController().popBackStack()
            }
        }

        return binding.root
    }
}