package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
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
                    val authDialogFragmentFromPosts = AuthDialogFragmentFromPosts()
                    val manager = activity?.supportFragmentManager
                    manager?.let { fragmentManager ->
                        authDialogFragmentFromPosts.show(
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
                postViewModel.editPost(post)
            }

            override fun openPost(post: Post) {
                val resultId = post.id
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

        //TODO Редактирование не работает.

        /*        Работа редактирования через фрагменты (конкретно все в фрагменте NewPost)*/
        postViewModel.edited.observe(viewLifecycleOwner) { it ->// Начало редактирования
//            Toast.makeText(this.context, "Переход на карточку поста", Toast.LENGTH_LONG).show()
            val resultId = it.id
            setFragmentResult("requestIdForNewPostFragment", bundleOf("id" to resultId))
            if (it.id != 0) {
                requireParentFragment().requireParentFragment()
                    .findNavController().navigate(R.id.action_mainFragment_to_newPostFragment)
            }
        }

        //TODO временное хранение не работает.
        binding.addPost.setOnClickListener {
            setFragmentResultListener("requestTmpContent") { key, bundle ->
                val tmpContent = bundle.getString("tmpContent")
                setFragmentResult(
                    "requestSavedTmpContent",
                    bundleOf("savedTmpContent" to tmpContent)
                )
            }
            if (authViewModel.authenticated)
                requireParentFragment().requireParentFragment()
                    .findNavController().navigate(R.id.action_mainFragment_to_newPostFragment)
            else {
                val authDialogFragmentFromPosts = AuthDialogFragmentFromPosts()
                val manager = activity?.supportFragmentManager
                manager?.let { fragmentManager ->
                    authDialogFragmentFromPosts.show(
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