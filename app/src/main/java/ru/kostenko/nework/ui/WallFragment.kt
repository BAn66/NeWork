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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.adapter.OnPostInteractionListener
import ru.kostenko.nework.adapter.PostsAdapter
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentWallBinding
import ru.kostenko.nework.dto.Post
import ru.kostenko.nework.util.MediaLifecycleObserver
import ru.kostenko.nework.viewmodel.AuthViewModel
import ru.kostenko.nework.viewmodel.PostViewModel
import ru.kostenko.nework.viewmodel.UserViewModel
import ru.kostenko.nework.viewmodel.WallViewModel
import javax.inject.Inject

@AndroidEntryPoint
class WallFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
    private val authViewModel: AuthViewModel by activityViewModels()
    private val wallViewModel: WallViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val  postViewModel: PostViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentWallBinding.inflate(layoutInflater)

        with(binding) {
            val authorId = userViewModel.user.value?.id
            val wallAdapter = PostsAdapter(object : OnPostInteractionListener {
                override fun like(post: Post) {
                    if(authorId == appAuth.authStateFlow.value.id.toInt() && authViewModel.authenticated){
                        wallViewModel.likeMyPostById(
                            post.id,
                            post.likedByMe
                        )
                    }
                    else if (authViewModel.authenticated) {
                        userViewModel.user.value?.let {
                            wallViewModel.likePostById(
                                it.id,
                                post.id,
                                post.likedByMe
                            )
                        }
                    } else {
                        val authDialogFragmentFromUserDetails = AuthDialogFragmentFromUserDetails()
                        val manager = activity?.supportFragmentManager
                        manager?.let { fragmentManager ->
                            authDialogFragmentFromUserDetails.show(
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
                }

                override fun share(post: Post) {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }
                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.description_shared))
                    startActivity(shareIntent)
                }
            }, MediaLifecycleObserver())

            listWall.adapter = wallAdapter
            if (authorId == appAuth.authStateFlow.value.id.toInt() && authViewModel.authenticated) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        wallViewModel.getWallPosts(0).collectLatest(wallAdapter::submitData)
                    }
                }
            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        authorId?.let { wallViewModel.getWallPosts(it).collectLatest(wallAdapter::submitData) }
                    }
                }
            }

            postViewModel.edited.observe(viewLifecycleOwner) { post->// Начало редактирования
                if (post.id != 0) {
                    requireParentFragment().requireParentFragment()
                        .findNavController().navigate(R.id.action_userDetailsFragment_to_newPostFragment)
                }
            }

            return binding.root
        }

    }
}