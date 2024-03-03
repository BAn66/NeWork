package ru.kostenko.nework.ui

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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.adapter.JobsAdapter
import ru.kostenko.nework.adapter.OnJobInteractionListener
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentJobsBinding
import ru.kostenko.nework.dto.Job
import ru.kostenko.nework.viewmodel.AuthViewModel
import ru.kostenko.nework.viewmodel.JobsViewModel
import ru.kostenko.nework.viewmodel.UserViewModel

import javax.inject.Inject

@AndroidEntryPoint
class JobsFragment : Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth
    private val authViewModel: AuthViewModel by activityViewModels()
    private val jobsViewModel: JobsViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val userId = userViewModel.user.value?.id!!
        val binding = FragmentJobsBinding.inflate(layoutInflater)
        with(binding) {
            val jobsAdapter = JobsAdapter(object : OnJobInteractionListener {
                override fun edit(job: Job) {
                    TODO("Not yet implemented")
                }

                override fun delete(job: Job) {
                    jobsViewModel.removeMyJob(job.id)
                }
            })
            listJobs.adapter = jobsAdapter

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    jobsViewModel.setId(userId)
                    jobsViewModel.getJobs(userId)
                    jobsViewModel.data.collectLatest {
                        jobsAdapter.submitList(it)
                    }
                }
            }

            if (authViewModel.authenticated && authViewModel.data.value.id.toInt() == userId){
                addJob.visibility = View.VISIBLE
            } else {
                addJob.visibility = View.GONE
            }

            addJob.setOnClickListener {
                    requireParentFragment().requireParentFragment()
                        .findNavController()
                        .navigate(R.id.action_userDetailsFragment_to_newJobFragment)
            }
        }
        return binding.root
    }

}