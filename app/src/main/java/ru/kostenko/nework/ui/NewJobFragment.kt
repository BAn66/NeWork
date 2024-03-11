package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentNewJobBinding
import ru.kostenko.nework.util.AndroidUtils.formatDateForJob
import ru.kostenko.nework.viewmodel.JobsViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class NewJobFragment : Fragment() {
    private val jobsViewModel: JobsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentNewJobBinding.inflate(layoutInflater)
        val toolbar = binding.toolbar
        toolbar.apply {
            title = "New job"
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }


        binding.start.text = OffsetDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        binding.end.setText(R.string.until_now)

        binding.startEndCard.setOnClickListener {
            val dialog = JobDateDialogFragment()
            dialog.show(parentFragmentManager, null)
        }

        setFragmentResultListener("setDateStart") { _, bundle ->
            val start = bundle.getString("start")
            binding.start.text = start
        }

        setFragmentResultListener("setDateEnd") { _, bundle ->
            val end = bundle.getString("end")
            binding.end.text = end
        }

        binding.createBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                    if (binding.companyName.text.isNullOrBlank() || binding.position.text.isNullOrBlank() ||
                        binding.start.text.isNullOrBlank()
                    ) {
                        Toast.makeText(
                            context,
                            getString(R.string.error_blank_job), Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        jobsViewModel.save(
                            name = binding.companyName.text.toString(),
                            position = binding.position.text.toString(),
                            link = if (binding.link.text.isNullOrBlank()) {
                                ""
                            } else {
                                binding.link.text.toString()
                            },
                            start = formatDateForJob(binding.start.text.toString()),
                            finish = if (binding.end.text.isNullOrBlank()) {
                                "1900-01-01T00:00:00Z"
                            } else {
                                formatDateForJob(binding.end.text.toString())
                            },
                        )
                    }
                }
            }
            findNavController().popBackStack()
        }
        return binding.root
    }


}



