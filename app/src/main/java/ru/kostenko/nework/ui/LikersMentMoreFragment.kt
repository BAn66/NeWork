package ru.kostenko.nework.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.adapter.OnUsersInteractionListener
import ru.kostenko.nework.adapter.UsersAdapter
import ru.kostenko.nework.databinding.FragmentTakePeopleBinding
import ru.kostenko.nework.dto.User
import ru.kostenko.nework.viewmodel.PostViewModel
import ru.kostenko.nework.viewmodel.UserViewModel

class LikersMentMoreFragment : Fragment() {

        val userViewModel: UserViewModel by activityViewModels()
        private lateinit var toolbar: Toolbar

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val binding = FragmentTakePeopleBinding.inflate(layoutInflater)
            val tmpMentionIds = mutableSetOf<Int>()
            toolbar = binding.toolbar
            toolbar.apply {
                setTitle(R.string.users)
                setNavigationIcon(R.drawable.arrow_back_24)
                setNavigationOnClickListener {
                    findNavController().popBackStack()
                }
            }

            val adapter = UsersAdapter(object : OnUsersInteractionListener {
                override fun onUserClicked(user: User) {
                }

                override fun onUserCheckBoxClicked(user: User) {
                }

                override fun onUserUnCheckBoxClicked(user: User) {
                }
            })

            binding.userList.adapter = adapter
            userViewModel.dataSetPeople.observe(viewLifecycleOwner) { users ->
                adapter.submitList(users)
            }

            return binding.root
        }
    }
