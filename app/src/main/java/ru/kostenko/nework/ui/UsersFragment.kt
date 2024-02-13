package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

            override fun getUserDetals(user: User) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        userViewModel.getUserById(user.id).join()
                        Toast.makeText(context, "Переход на экран пользователя", Toast.LENGTH_SHORT)
                            .show()
//                        findNavController().navigate(R.id.userProfileFragment)
                    }
                }
            }

        })

        binding.userList.adapter = adapter

        userViewModel.data.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }
        return binding.root
    }
}
