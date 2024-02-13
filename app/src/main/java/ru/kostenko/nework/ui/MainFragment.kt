package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentMainBinding

@AndroidEntryPoint
class MainFragment : Fragment() {
    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentMainBinding.inflate(inflater, container, false)

        val navController = requireNotNull(
            childFragmentManager.findFragmentById(R.id.nav_host_fragment_main)
        ).findNavController()

        binding.navView.setupWithNavController(navController)

        toolbar = binding.toolbar
        toolbar.apply {
            setTitle(R.string.app_name)
            inflateMenu(R.menu.auth_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.authentication -> {
                        requireParentFragment()
                            .findNavController().navigate(R.id.action_mainFragment_to_authFragment)
                        true
                    }
                    else -> false
                }
            }
        }
        return binding.root
    }


}