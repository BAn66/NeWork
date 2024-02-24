package ru.kostenko.nework.ui

import android.app.DatePickerDialog
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ru.kostenko.nework.R
import ru.kostenko.nework.authorization.AppAuth
import ru.kostenko.nework.databinding.FragmentJobsBinding
import ru.kostenko.nework.databinding.FragmentNewJobBinding
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

        val monthNames = arrayOf(
            "января",
            "февраля",
            "марта",
            "апреля",
            "мая",
            "июня",
            "июля",
            "августа",
            "сентября",
            "октября",
            "ноября",
            "декабря"
        )

        val monthNameStart = monthNames[OffsetDateTime.now().month.value - 1]
        binding.start.text = OffsetDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd $monthNameStart yyyy - "))

        binding.end.text = "НВ"

        binding.start.setOnClickListener {
            selectDateDialog(binding.start, it.context)
            val start = binding.start.text.toString()
            binding.start.text = "$start - "
            jobsViewModel.startDate(start)

        }
        binding.end.setOnClickListener {
            selectDateDialog(binding.end, it.context)
            val end = binding.end.text
            jobsViewModel.endDate(end.toString())

        }
        binding.createBtn.setOnClickListener {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                jobsViewModel.save(
                    binding.companyName.text.toString(),
                    binding.position.text.toString(),
                    binding.start.text.toString(),
                    binding.end.text.toString(),
                    binding.link.text.toString()
                )
            }
        }
            findNavController().popBackStack()
    }


        return binding.root
    }
}

fun selectDateDialog(textView: TextView?, context: Context) {
    val currentDateTime = java.util.Calendar.getInstance()
    val startYear = currentDateTime.get(java.util.Calendar.YEAR)
    val startMonth = currentDateTime.get(java.util.Calendar.MONTH)
    val startDay = currentDateTime.get(java.util.Calendar.DAY_OF_MONTH)
    DatePickerDialog(context, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        val pickedDateTime = java.util.Calendar.getInstance()
        pickedDateTime.set(year, month, dayOfMonth)
        val result = GregorianCalendar(year, month, dayOfMonth).time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.uuu'Z'", Locale.getDefault())
        textView?.setText(dateFormat.format(result))
    }, startYear, startMonth, startDay).show()


}