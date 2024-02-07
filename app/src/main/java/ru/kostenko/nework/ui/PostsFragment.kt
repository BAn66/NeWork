package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.databinding.FragmentPostsBinding

@AndroidEntryPoint
class PostsFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostsBinding.inflate(layoutInflater)

        binding.addPost.setOnClickListener {
            Toast.makeText(this.context, "добавь пост", Toast.LENGTH_SHORT).show()
        }
    return binding.root
    }

}