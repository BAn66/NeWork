package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import ru.kostenko.nework.R
import ru.kostenko.nework.adapter.OnUsersInteractionListener
import ru.kostenko.nework.adapter.UsersAdapter
import ru.kostenko.nework.databinding.FragmentTakeSpeakersBinding
import ru.kostenko.nework.dto.User
import ru.kostenko.nework.viewmodel.EventViewModel
import ru.kostenko.nework.viewmodel.UserViewModel

@AndroidEntryPoint
class TakeSpeakersFragment : Fragment() {
    private val userViewModel: UserViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTakeSpeakersBinding.inflate(layoutInflater)
        val tmpSpeakersIds = MutableStateFlow(emptySet<Long>())
        val toolbar: Toolbar = binding.toolbar
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
                        eventViewModel.setSpeakers(tmpSpeakersIds.value)
                        findNavController().popBackStack()
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
                tmpSpeakersIds.update { it + user.id.toLong() }
            }

            override fun onUserUnCheckBoxClicked(user: User) {
                tmpSpeakersIds.update { it - user.id.toLong() }
            }
        })

        binding.userList.adapter = adapter

        userViewModel.dataTakeble.asFlow()
            .combine(tmpSpeakersIds){users, spkIds ->
                users.map {
                    it.copy(isChecked = it.id.toLong() in spkIds)
                }
            }.asLiveData()
            .observe(viewLifecycleOwner) { users ->
                adapter.submitList(users)
            }

        return binding.root
    }
}
