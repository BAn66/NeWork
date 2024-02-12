package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ru.kostenko.nework.databinding.FragmentUsersBinding

class UsersFragment: Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val binding = FragmentUsersBinding.inflate(layoutInflater)

//            val adapter = UserListAdapter(object : OnUserListInteractionListener {
//                override fun onOpenProfile(user: User) {
//                    lifecycleScope.launch {
//                        userViewModel.getUserById(user.id).join()
//                        findNavController().navigate(R.id.userProfileFragment)
//                    }
//                }
//            })

//            binding.userList.adapter = adapter
//
//            userViewModel.data.observe(viewLifecycleOwner) {
//                adapter.submitList(it.filter { user ->
//                    userViewModel.userIds.value!!.contains(user.id)
//                })
//            }

            return binding.root
        }
    }
