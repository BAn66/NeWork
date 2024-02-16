package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.kostenko.nework.adapter.MediaLifecycleObserver
import ru.kostenko.nework.adapter.OnPostInteractionListener
import ru.kostenko.nework.adapter.PostsAdapter
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentPostsBinding
import ru.kostenko.nework.dto.Post
import ru.kostenko.nework.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class PostsFragment : Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth
    private val postViewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentPostsBinding.inflate(layoutInflater)
        //TODO Сделать внизу поста цепочку аватаров лайкнувших

//        работа с постом
        val adapter = PostsAdapter(object : OnPostInteractionListener {
            override fun like(post: Post) { //TODO при лайке не авторизованным пользователем необходимо переходить на экран логина
                postViewModel.likePostById(post.id, post.likedByMe)
            }

            override fun remove(post: Post) {
                postViewModel.removePostById(post.id)
            }

            override fun edit(post: Post) {
                postViewModel.editPost(post)
            }

            override fun openPost(post: Post) {
                val resultId = post.id
//                setFragmentResult("requestIdForPostFragment", bundleOf("id" to resultId))
//                findNavController().navigate(R.id.action_feedFragment_to_postFragment)

            }
        }, MediaLifecycleObserver())

        binding.list.adapter = adapter
//            .withLoadStateHeaderAndFooter(
//                footer = PostLoadingStateAdapter {
//                    adapter.retry()
//                },
//                header = PostLoadingStateAdapter {
//                    adapter.retry()
//                }
//            )

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
//                                || state.prepend is LoadState.Loading ||
//                                state.append is LoadState.Loading
                }
            }
        }

        //        Работа редактирования через фрагменты (конкретно все в фрагменте NewPost)
        postViewModel.edited.observe(viewLifecycleOwner) { it ->// Начало редактирования
            val resultId = it.id
            setFragmentResult("requestIdForNewPostFragment", bundleOf("id" to resultId))
            if (it.id != 0) {
//                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }
        }

        binding.addPost.setOnClickListener {
            Toast.makeText(this.context, "Добавь пост", Toast.LENGTH_LONG).show()
            setFragmentResultListener("requestTmpContent") { key, bundle ->
                val tmpContent = bundle.getString("tmpContent")
                setFragmentResult(
                    "requestSavedTmpContent",
                    bundleOf("savedTmpContent" to tmpContent)
                )
            }
//            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }
        return binding.root
    }

}

//        val toolbar = requireParentFragment().activity?.findViewById<Toolbar>(R.id.toolbar)
//        toolbar?.setTitle("TEST")