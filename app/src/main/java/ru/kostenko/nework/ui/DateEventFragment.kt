package ru.kostenko.nework.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentAuthBinding
import ru.kostenko.nework.databinding.FragmentDateEventBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

@AndroidEntryPoint
class DateEventFragment : BottomSheetDialogFragment() {
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
            selectDateDialog(binding.editDateEvent)
        }

        binding.editDateEvent.setOnClickListener {
            selectDateDialog(binding.editDateEvent)
        }

        val checkedRadioButtonId = binding.radioGroup.checkedRadioButtonId
        // Returns View.NO_ID if nothing is checked.
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->

        }

        binding.online.setOnCheckedChangeListener { buttonView, isChecked ->
            Toast.makeText(this.context, "онлайн", Toast.LENGTH_LONG).show()
        }

        binding.offline.setOnCheckedChangeListener { buttonView, isChecked ->
            Toast.makeText(this.context, "оффлайн", Toast.LENGTH_LONG).show()
        }

        return binding.root
    }

    fun selectDateDialog(textView: TextView) {
//        val currentDateTime = Calendar.getInstance()
//        val startYear = currentDateTime.get(Calendar.YEAR)
//        val startMonth = currentDateTime.get(Calendar.MONTH)
//        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
//
//        DatePickerDialog(
//            textView.context,
//            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
//                val pickedDateTime = Calendar.getInstance()
//                pickedDateTime.set(year, month, dayOfMonth)
//                val result = GregorianCalendar(year, month, dayOfMonth).time
//                val dateFormat =
//                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.uuu'Z'", Locale.getDefault())
//                textView.text = dateFormat.format(result)
//            },
//            startYear,
//            startMonth,
//            startDay
//        ).show()
        //TODO сделать еще один диалог с вводом отдельно даты и времени




    }
}