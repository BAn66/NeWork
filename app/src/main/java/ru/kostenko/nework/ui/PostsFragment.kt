package ru.kostenko.nework.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentPostsBinding

@AndroidEntryPoint
class PostsFragment : Fragment() {

    private lateinit var appBar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentPostsBinding.inflate(layoutInflater)

        appBar = binding.root.findViewById(R.id.custom_app_bar)

        activity?.setActionBar(appBar)
        activity?.actionBar?.setTitle(R.string.app_name)



        binding.addPost.setOnClickListener {
//            Toast.makeText(binding.root, "Добавь пост", Toast.LENGTH_SHORT).show()
            Snackbar.make(binding.root, "Добавь пост",Snackbar.LENGTH_SHORT).show()
        }


        return super.onCreateView(inflater, container, savedInstanceState)

    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.authentication -> {
            Toast.makeText(this.context, "Добавь пост", Toast.LENGTH_SHORT).show()
            true
        }

        else -> {
            // The user's action isn't recognized.
            // Invoke the superclass to handle it.
//            super.onOptionsItemSelected(item)
            false
        }
    }
}