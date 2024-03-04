package ru.kostenko.nework.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.adapter.OnUsersInteractionListener
import ru.kostenko.nework.adapter.UsersAdapter
import ru.kostenko.nework.databinding.FragmentUsersBinding
import ru.kostenko.nework.dto.User
import ru.kostenko.nework.viewmodel.UserViewModel

@AndroidEntryPoint
class UsersFragment : Fragment() {

    val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUsersBinding.inflate(layoutInflater)
        val adapter = UsersAdapter(object : OnUsersInteractionListener {
            override fun onUserClicked(user: User) {
                lifecycleScope.launch {
                    userViewModel.getUserById(user.id).join()
                    requireParentFragment().requireParentFragment().findNavController()
                        .navigate(R.id.action_mainFragment_to_userDetailsFragment)
                }
            }

            override fun onUserCheckBoxClicked(user: User) {
            }

            override fun onUserUnCheckBoxClicked(user: User) {
            }
        })

        binding.userList.adapter = adapter
        userViewModel.data.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }

        return binding.root
    }
}
