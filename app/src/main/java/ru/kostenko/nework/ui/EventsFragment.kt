package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.kostenko.nework.adapter.EventsAdapter
import ru.kostenko.nework.adapter.OnEventInteractionListener
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentEventsBinding
import ru.kostenko.nework.dto.Event
import ru.kostenko.nework.util.MediaLifecycleObserver
import ru.kostenko.nework.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventsFragment: Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth
    private val eventViewModel: EventViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEventsBinding.inflate(layoutInflater)

        val adapter = EventsAdapter(object : OnEventInteractionListener {
            override fun like(event: Event) { //TODO при лайке не авторизованным пользователем необходимо переходить на экран логина
                eventViewModel.likeEventById(event.id, event.likedByMe)
            }

            override fun remove(event: Event) {
                eventViewModel.removeEventById(event.id)
            }

            override fun edit(event: Event) {
                eventViewModel.editEvent(event)
            }

            override fun openEvent(event: Event) {
                val resultId = event.id
//                setFragmentResult("requestIdForPostFragment", bundleOf("id" to resultId))
//                findNavController().navigate(R.id.action_feedFragment_to_postFragment)
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

        //        Работа редактирования через фрагменты (конкретно все в фрагменте NewPost)
        eventViewModel.edited.observe(viewLifecycleOwner) { it ->// Начало редактирования
            val resultId = it.id
            setFragmentResult("requestIdForNewPostFragment", bundleOf("id" to resultId))
            if (it.id != 0) {
//                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }
        }

        binding.addEvent.setOnClickListener {
            Toast.makeText(this.context, "Добавь событие", Toast.LENGTH_LONG).show()
            setFragmentResultListener("requestTmpContent") { key, bundle ->
                val tmpContent = bundle.getString("tmpContent")
                setFragmentResult(
                    "requestSavedTmpContent",
                    bundleOf("savedTmpContent" to tmpContent)
                )
            }
//            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }
        return binding.root
    }
}