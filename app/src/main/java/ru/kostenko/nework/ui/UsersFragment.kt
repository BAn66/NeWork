package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ru.kostenko.nework.adapter.OnUsersInteractionListener
import ru.kostenko.nework.adapter.UsersAdapter
import ru.kostenko.nework.databinding.FragmentUsersBinding
import ru.kostenko.nework.dto.User
import ru.kostenko.nework.viewmodel.UserViewModel

class UsersFragment: Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val binding = FragmentUsersBinding.inflate(layoutInflater)

//            val adapter = UsersAdapter(object : OnUsersInteractionListener {
//                override fun onOpenProfile(user: User) {
//                    lifecycleScope.launch {
//                        userViewModel.getUserById(user.id).join()
//                        findNavController().navigate(R.id.userProfileFragment)
//                    }
//                }
//            })

//            binding.userList.adapter = adapter

            userViewModel.dataState.observe(viewLifecycleOwner) {
                adapter.submitList(it.filter { user ->
                    userViewModel.userIds.value!!.contains(user.id)
                })
            }

            return binding.root
        }
    }
