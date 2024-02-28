package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.kostenko.nework.adapter.OnPostInteractionListener
import ru.kostenko.nework.adapter.PostsAdapter
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentWallBinding
import ru.kostenko.nework.dto.Post
import ru.kostenko.nework.util.MediaLifecycleObserver
import ru.kostenko.nework.viewmodel.AuthViewModel
import ru.kostenko.nework.viewmodel.UserViewModel
import ru.kostenko.nework.viewmodel.WallViewModel
import javax.inject.Inject

@AndroidEntryPoint
class WallFragment : Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth
    private val authViewModel: AuthViewModel by activityViewModels()
    private val wallViewModel: WallViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentWallBinding.inflate(layoutInflater)

        with(binding) {
//            textWall.text = userViewModel.user.value?.login
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
                    TODO("Not yet implemented")
                }

                override fun edit(post: Post) {
                    TODO("Not yet implemented")
                }

                override fun openPost(post: Post) {
                }

                override fun share(post: Post) {
                    TODO("Not yet implemented")
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

            return binding.root
        }

    }
}