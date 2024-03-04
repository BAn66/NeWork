package ru.kostenko.nework.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.adapter.OnPostInteractionListener
import ru.kostenko.nework.adapter.PostsAdapter
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentPostsBinding
import ru.kostenko.nework.dto.Post
import ru.kostenko.nework.util.MediaLifecycleObserver
import ru.kostenko.nework.viewmodel.AuthViewModel
import ru.kostenko.nework.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class PostsFragment : Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth
    private val postViewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentPostsBinding.inflate(layoutInflater)

        val adapter = PostsAdapter(object : OnPostInteractionListener {
            override fun like(post: Post) {
                if (authViewModel.authenticated)
                    postViewModel.likePostById(post.id, post.likedByMe)
                else {
                    val authDialogFragment = AuthDialogFragment()
                    val manager = activity?.supportFragmentManager
                    manager?.let { fragmentManager ->
                        authDialogFragment.show(
                            fragmentManager,
                            "myDialog"
                        )
                    }
                }
            }

            override fun remove(post: Post) {
                postViewModel.removePostById(post.id)
            }

            override fun edit(post: Post) {
                postViewModel.clearMedia()
                postViewModel.editPost(post)
            }

            override fun openPost(post: Post) {
                lifecycleScope.launch {
                    postViewModel.getPostById(post.id).join()
                    requireParentFragment().requireParentFragment()
                        .findNavController().navigate(R.id.action_mainFragment_to_postFragment)
                }
            }

            override fun share(post: Post) {
                //создаем актвити Chooser для расшаривания текста поста через Intent
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                //startActivity(intent) //Более скромный вариант ниже более симпатичный вариант
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.description_shared))
                startActivity(shareIntent)
            }
        }, MediaLifecycleObserver())

        binding.list.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                postViewModel.data.collectLatest(adapter::submitData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swiperefresh.isRefreshing =
                        state.refresh is LoadState.Loading
                }
            }
        }

        postViewModel.edited.observe(viewLifecycleOwner) { post->// Начало редактирования
            if (post.id != 0) {
                requireParentFragment().requireParentFragment()
                    .findNavController().navigate(R.id.action_mainFragment_to_newPostFragment)
            }
        }

        binding.addPost.setOnClickListener {
            if (authViewModel.authenticated)
                requireParentFragment().requireParentFragment()
                    .findNavController().navigate(R.id.action_mainFragment_to_newPostFragment)
            else {
                val authDialogFragment = AuthDialogFragment()
                val manager = activity?.supportFragmentManager
                manager?.let { fragmentManager ->
                    authDialogFragment.show(
                        fragmentManager,
                        "myDialog"
                    )
                }
            }
        }
        return binding.root
    }

}

//        val toolbar = requireParentFragment().activity?.findViewById<Toolbar>(R.id.toolbar)
//        toolbar?.setTitle("TEST")