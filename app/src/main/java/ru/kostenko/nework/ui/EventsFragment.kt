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
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.adapter.EventsAdapter
import ru.kostenko.nework.adapter.OnEventInteractionListener
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentEventsBinding
import ru.kostenko.nework.dto.Event
import ru.kostenko.nework.util.MediaLifecycleObserver
import ru.kostenko.nework.viewmodel.AuthViewModel
import ru.kostenko.nework.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventsFragment: Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth
    private val eventViewModel: EventViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEventsBinding.inflate(layoutInflater)

        val adapter = EventsAdapter(object : OnEventInteractionListener {
            override fun like(event: Event) {
                if (authViewModel.authenticated)
                    eventViewModel.likeEventById(event.id, event.likedByMe)
                else {
                    val authDialogFragment = AuthDialogFragment()
                    val manager = activity?.supportFragmentManager
                    manager?.let { fragmentManager ->
                        authDialogFragment.show(
                            fragmentManager,
                            "myDialog"
                        )
                    }
                }
            }

            override fun remove(event: Event) {
                eventViewModel.removeEventById(event.id)
            }

            override fun edit(event: Event) {
                eventViewModel.clearMedia()
                eventViewModel.editEvent(event)
            }

            override fun openEvent(event: Event) {
                lifecycleScope.launch {
                    eventViewModel.getEventById(event.id).join()
                    requireParentFragment().requireParentFragment()
                        .findNavController().navigate(R.id.action_mainFragment_to_eventFragment)
                }
            }

            override fun share(event: Event) {
                //создаем актвити Chooser для расшаривания текста поста через Intent

                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, event.content)
                    type = "text/plain"
                }
                //startActivity(intent) //Более скромный вариант ниже более симпатичный вариант
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.description_shared))
                startActivity(shareIntent)
            }

            override fun participate(event: Event) {
                if (authViewModel.authenticated)
                    eventViewModel.participate(event.id, event.participatedByMe)
                else {
                    val authDialogFragment = AuthDialogFragment()
                    val manager = activity?.supportFragmentManager
                    manager?.let { fragmentManager ->
                        authDialogFragment.show(
                            fragmentManager,
                            "myDialog"
                        )
                    }
                }
            }

        }, MediaLifecycleObserver())

        binding.list.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                eventViewModel.data.collectLatest(adapter::submitData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swiperefresh.isRefreshing =
                        state.refresh is LoadState.Loading
//                                || state.prepend is LoadState.Loading ||
//                                state.append is LoadState.Loading
                }
            }
        }


        eventViewModel.edited.observe(viewLifecycleOwner) {
            if (it.id != 0) {
                requireParentFragment().requireParentFragment()
                    .findNavController().navigate(R.id.action_mainFragment_to_newEventFragment)
            }
        }

        binding.addEvent.setOnClickListener {
            if (authViewModel.authenticated)
                requireParentFragment().requireParentFragment()
                    .findNavController().navigate(R.id.action_mainFragment_to_newEventFragment)
            else {
                val authDialogFragment = AuthDialogFragment()
                val manager = activity?.supportFragmentManager
                manager?.let { fragmentManager ->
                    authDialogFragment.show(
                        fragmentManager,
                        "myDialog"
                    )
                }
            }
        }
        return binding.root
    }
}