package ru.kostenko.nework.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentAuthBinding
import ru.kostenko.nework.databinding.FragmentDateEventBinding
import ru.kostenko.nework.dto.EventType
import ru.kostenko.nework.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

@AndroidEntryPoint
class DateEventFragment : BottomSheetDialogFragment() {

    private val eventViewModel: EventViewModel by activityViewModels()
    companion object {
        const val TAG = "ModalBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
//    = inflater.inflate(R.layout.fragment_date_event, container, false)
    {
        val binding = FragmentDateEventBinding.inflate(layoutInflater)
        binding.online.isChecked = true
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
        datePicker.dialog?.show()

        binding.dateEventInput.setOnClickListener {
            val dialog = EventDateDialogFragment()
            dialog.show(parentFragmentManager, null)
        }

        binding.editDateEvent.setOnClickListener {
            val dialog = EventDateDialogFragment()
            dialog.show(parentFragmentManager, null)
        }

        setFragmentResultListener("setDateEvent") { _, bundle ->
            val date = bundle.getString("dateEvent")
            setFragmentResultListener("setTimeEvent") { _, bundle ->
                val time = bundle.getString("timeEvent")
                binding.editDateEvent.setText("$date $time")
                eventViewModel.setDateTime(binding.editDateEvent.text.toString())
            }
        }



        val checkedRadioButtonId = binding.radioGroup.checkedRadioButtonId
        // Returns View.NO_ID if nothing is checked.
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->

        }

        binding.online.setOnCheckedChangeListener { buttonView, isChecked ->
            Toast.makeText(this.context, "онлайн", Toast.LENGTH_LONG).show()
//            eventViewModel.setTypeEvent.(EventType.ONLINE)
        }

        binding.offline.setOnCheckedChangeListener { buttonView, isChecked ->
            Toast.makeText(this.context, "оффлайн", Toast.LENGTH_LONG).show()
//            eventViewModel.setTypeEvent.(EventType.OFFLINE)
        }

        return binding.root
    }

}