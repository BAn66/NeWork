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
class PostsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentPostsBinding.inflate(layoutInflater)

//        val toolbar = requireParentFragment().activity?.findViewById<Toolbar>(R.id.toolbar)
//        toolbar?.setTitle("TEST")

        binding.addPost.setOnClickListener {
            Toast.makeText(this.context, "Добавь пост", Toast.LENGTH_LONG).show()
        }
        return binding.root
    }

}