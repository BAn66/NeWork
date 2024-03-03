package ru.kostenko.nework.ui

import android.content.Intent
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.adapter.OnUsersInteractionListener
import ru.kostenko.nework.adapter.UsersAdapter
import ru.kostenko.nework.databinding.FragmentTakePeopleBinding
import ru.kostenko.nework.databinding.FragmentUsersBinding
import ru.kostenko.nework.dto.User
import ru.kostenko.nework.viewmodel.PostViewModel
import ru.kostenko.nework.viewmodel.UserViewModel

@AndroidEntryPoint
class TakePeopleFragment : Fragment() {

    val userViewModel: UserViewModel by activityViewModels()
    val postViewModel: PostViewModel by activityViewModels()
    private lateinit var toolbar: Toolbar
//    private val tmpMentionIds: Set<Int> = emptySet()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTakePeopleBinding.inflate(layoutInflater)
        val tmpMentionIds = mutableSetOf<Int>()
        toolbar = binding.toolbar
        toolbar.apply {
            setTitle(R.string.take_people)
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            inflateMenu(R.menu.save_feed_item)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.save -> {
                        lifecycleScope.launch {
                            if(tmpMentionIds.isNotEmpty()) postViewModel.setMentinoed(tmpMentionIds)
                            Log.d("MentTAAAG", "save : $tmpMentionIds")
                            findNavController().popBackStack()
                        }
                        true
                    }

                    else -> false
                }
            }
        }

        val adapter = UsersAdapter(object : OnUsersInteractionListener {
            override fun onUserClicked(user: User) {
            }

            override fun onUserCheckBoxClicked(user: User) {
                if (!tmpMentionIds.contains(user.id)) tmpMentionIds.add(user.id)
                Log.d("MentTAAAG", "onUserCheckBoxClicked +: $tmpMentionIds ")
            }

            override fun onUserUnCheckBoxClicked(user: User) {
                if (tmpMentionIds.contains(user.id)) tmpMentionIds.remove(user.id)
                Log.d("MentTAAAG", "onUserUnCheckBoxClicked -: $tmpMentionIds ")
            }
        })

        binding.userList.adapter = adapter
        userViewModel.dataTakeble.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }

        return binding.root
    }
}
