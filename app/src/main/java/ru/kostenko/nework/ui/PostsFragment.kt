package ru.kostenko.nework.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentPostsBinding


@AndroidEntryPoint
class PostsFragment: Fragment() {

    private lateinit var toolbar_posts: Toolbar
//    val fragmentManager: FragmentManager = (activity as AppCompatActivity).supportFragmentManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentPostsBinding.inflate(layoutInflater)
//        if (savedInstanceState == null) {
//            fragmentManager.commit {
//                setReorderingAllowed(true)
//                add<AuthFragment>(R.id.authFragment)
//            }
//        }
        val view = binding.root


        setHasOptionsMenu(true)
        toolbar_posts = binding.root.findViewById<Toolbar>(R.id.toolbar)


        toolbar_posts.apply{
            setTitle(R.string.app_name)
            inflateMenu(R.menu.auth_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.authentication -> {
                            findNavController().navigate(R.id.action_postsFragment_to_authFragment)
                        true
                    }
                    else -> false
                }
            }
        }

        binding.addPost.setOnClickListener {
            Toast.makeText(this.context, "Добавь пост" , Toast.LENGTH_LONG).show()
         }
    return binding.root
    }

}