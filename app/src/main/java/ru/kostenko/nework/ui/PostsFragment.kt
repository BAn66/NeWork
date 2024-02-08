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
import ru.kostenko.nework.databinding.FragmentPostsBinding


@AndroidEntryPoint
class PostsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentPostsBinding.inflate(layoutInflater)

//        setHasOptionsMenu(true)
        val toolbar = binding.root.findViewById<Toolbar>(R.id.toolbar)

        toolbar.apply {
            setTitle(R.string.app_name)
            inflateMenu(R.menu.auth_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.authentication -> {
                        requireParentFragment()
                            .requireParentFragment()
                            .findNavController().navigate(R.id.action_mainFragment_to_authFragment)
                        true
                    }
                    else -> false
                }
            }
        }

        binding.addPost.setOnClickListener {
            Toast.makeText(this.context, "Добавь пост", Toast.LENGTH_LONG).show()
        }
        return binding.root
    }

}