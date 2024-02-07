package ru.kostenko.nework.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentPostsBinding


@AndroidEntryPoint
class PostsFragment: Fragment() {

    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostsBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        toolbar = binding.root.findViewById<Toolbar>(R.id.toolbar)


        toolbar.apply{
            setTitle(R.string.app_name)
            inflateMenu(R.menu.auth_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.authentication -> {
                        Toast.makeText(this.context, "авторизуйся", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
        }

        binding.addPost.setOnClickListener {
            Toast.makeText(this.context, "добавь пост", Toast.LENGTH_SHORT).show()
        }
    return binding.root
    }


}