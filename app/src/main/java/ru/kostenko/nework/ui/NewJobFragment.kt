package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentNewJobBinding
import ru.kostenko.nework.viewmodel.AuthViewModel
import ru.kostenko.nework.viewmodel.JobsViewModel
import ru.kostenko.nework.viewmodel.UserViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import javax.inject.Inject

class NewJobFragment : Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth
    private val authViewModel: AuthViewModel by activityViewModels()
    private val jobsViewModel: JobsViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentNewJobBinding.inflate(layoutInflater)
        //Наполняем верхний аппбар
        toolbar = binding.toolbar
        toolbar.apply {
            setTitle("New job")
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }


        binding.start.text = OffsetDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy - "))

        binding.end.text = "НВ"

        binding.startEndCard.setOnClickListener {
            var dialog = JobDateDialogFragment()
            dialog.show(requireParentFragment().parentFragmentManager, "jobDateDialog")
        }

        setFragmentResultListener("setDateStart") { key, bundle ->
            val start = bundle.getString("start")
            binding.start.setText(start)
        }

        setFragmentResultListener("setDateEnd") { key, bundle ->
            val end = bundle.getString("end")
            binding.end.setText(end)
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
                            start = binding.start.text.toString(),
                            finish = if (binding.end.text.isNullOrBlank()) {
                                "1900-01-01T00:00:00Z"
                            } else {
                                binding.end.text.toString()
                            },
                        )
//Заглушка
//                        jobsViewModel.save(
//                            "Pilot",
//                            "manager",
//                            "2024-02-01T08:05:53.667Z",
//                            "2024-02-24T08:05:53.667Z",
//                            "www.pilot.ru"
//                        )
                    }

                }
            }
            findNavController().popBackStack()
        }
        return binding.root
    }


}



