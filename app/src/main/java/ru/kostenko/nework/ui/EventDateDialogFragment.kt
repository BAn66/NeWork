package ru.kostenko.nework.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.icu.text.DateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.DialogPickDateTimeBinding
import ru.kostenko.nework.databinding.FragmentJobDateDialogBinding
import ru.kostenko.nework.util.AndroidUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class EventDateDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var binding = DialogPickDateTimeBinding.inflate(layoutInflater)

        binding.cancelBtn.setOnClickListener {
            dismiss()
        }
        binding.okBtn.setOnClickListener {
            val dateEvent = binding.dateEdit.text.toString()
            val timeEvent = binding.timeEdit.text.toString()
            parentFragmentManager.setFragmentResult("setDateEvent", bundleOf("dateEvent" to dateEvent))
            parentFragmentManager.setFragmentResult("setTimeEvent", bundleOf("timeEvent" to timeEvent))
            dismiss()
        }

        View.OnClickListener {
            context?.let { item ->
                AndroidUtils.pickDate(binding.dateEdit, item)
            }
        }
        View.OnClickListener {
            context?.let { item ->
                AndroidUtils.pickTime(binding.timeEdit, item)
            }
        }

        binding.dateInput.setOnClickListener {
            context?.let { item ->
                AndroidUtils.pickDate(binding.dateEdit, item)
            }
        }
        binding.timeInput.setOnClickListener {
            context?.let { item ->
                AndroidUtils.pickTime(binding.timeEdit, item)
            }
        }
        binding.dateEdit.setOnClickListener {
            context?.let { item ->
                AndroidUtils.pickDate(binding.dateEdit, item)
            }
        }
        binding.timeEdit.setOnClickListener {
            context?.let { item ->
                AndroidUtils.pickTime(binding.timeEdit, item)
            }
        }

        return binding.root
    }
}