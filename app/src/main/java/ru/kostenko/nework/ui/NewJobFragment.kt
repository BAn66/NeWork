package ru.kostenko.nework.ui

import android.app.DatePickerDialog
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import ru.kostenko.nework.util.StringArg
import ru.kostenko.nework.viewmodel.AuthViewModel
import ru.kostenko.nework.viewmodel.JobsViewModel
import ru.kostenko.nework.viewmodel.UserViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.GregorianCalendar
import java.util.Locale
import javax.inject.Inject

class NewJobFragment : Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth
    private val authViewModel: AuthViewModel by activityViewModels()
    private val jobsViewModel: JobsViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var toolbar: Toolbar

    companion object {
        var Bundle.text by StringArg
    }
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
            binding.start.text = start
        }

        setFragmentResultListener("setDateEnd") { key, bundle ->
            val end = bundle.getString("end")
            binding.start.text = end
        }

        binding.createBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    var companyName = "Pilot"
                    var position = "manager"
                    var start: String = "2024-02-01T08:05:53.667Z"
                    var end: String = "2024-02-24T08:05:53.667Z"
                    var url: String? = "www.pilot.ru"
//                    if (binding.companyName.text.isNullOrBlank()) {
//                        Toast.makeText(context, R.string.error_blank_job, Toast.LENGTH_LONG).show()
//                    } else companyName = binding.companyName.text.toString()
//                    if (binding.position.text.isNullOrBlank())
//                        Toast.makeText(context, R.string.error_blank_job, Toast.LENGTH_LONG).show()
//                    else position = binding.position.text.toString()
//                    if(binding.start.text.isNotBlank()) start = binding.start.text.toString()
//                    if(binding.end.text.isNotBlank()) end = binding.end.text.toString()
//                    if(binding.link.text?.isNotBlank() == true) url = binding.link.text.toString()

                    jobsViewModel.save(
                        companyName,
                        position,
                        start,
                        end,
                        url
                    )
                }
            }
            findNavController().popBackStack()
        }


        return binding.root
    }
}



