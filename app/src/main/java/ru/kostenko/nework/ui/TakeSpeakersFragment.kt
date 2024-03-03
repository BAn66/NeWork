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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.adapter.OnUsersInteractionListener
import ru.kostenko.nework.adapter.UsersAdapter
import ru.kostenko.nework.databinding.FragmentTakeSpeakersBinding
import ru.kostenko.nework.dto.User
import ru.kostenko.nework.viewmodel.EventViewModel
import ru.kostenko.nework.viewmodel.UserViewModel

@AndroidEntryPoint
class TakeSpeakersFragment : Fragment() {

    val userViewModel: UserViewModel by activityViewModels()
    val eventViewModel: EventViewModel by activityViewModels()
    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTakeSpeakersBinding.inflate(layoutInflater)
        val tmpSpeakersIds = mutableSetOf<Int>()
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
                            if(tmpSpeakersIds.isNotEmpty()) eventViewModel.setSpeakers(tmpSpeakersIds)
                            Log.d("MentTAAAG", "save : $tmpSpeakersIds")
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
                if (!tmpSpeakersIds.contains(user.id)) tmpSpeakersIds.add(user.id)
                Log.d("MentTAAAG", "onUserCheckBoxClicked +: $tmpSpeakersIds ")
            }

            override fun onUserUnCheckBoxClicked(user: User) {
                if (tmpSpeakersIds.contains(user.id)) tmpSpeakersIds.remove(user.id)
                Log.d("MentTAAAG", "onUserUnCheckBoxClicked -: $tmpSpeakersIds ")
            }
        })

        binding.userList.adapter = adapter
        userViewModel.dataTakeble.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }

        return binding.root
    }
}
