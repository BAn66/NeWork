package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentRegistrationBinding

class RegistrationFragment: Fragment() {
    private lateinit var toolbar_registration: Toolbar

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

        binding.loginBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        return  binding.root

    }
}