package ru.kostenko.nework.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import ru.kostenko.nework.databinding.FragmentJobDateDialogBinding
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class JobDateDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var binding = FragmentJobDateDialogBinding.inflate(layoutInflater)

        binding.cancelBtn.setOnClickListener {
            dismiss()
        }
        binding.okBtn.setOnClickListener {
            val resultStart  = binding.editStart.text.toString()
            val resultEnd = binding.editEnd.text.toString()
            parentFragmentManager.setFragmentResult("setDateStart", bundleOf("start" to resultStart))
            parentFragmentManager.setFragmentResult("setDateEnd", bundleOf("end" to resultEnd))
            dismiss()
        }

        View.OnClickListener {
            selectDateDialog(binding.editStart)
        }
        View.OnClickListener {
            selectDateDialog(binding.editEnd)
        }

        binding.startInputLayout.setOnClickListener {
            selectDateDialog(binding.editStart)
        }
        binding.endInputLayout.setOnClickListener {
            selectDateDialog(binding.editEnd)
        }
        binding.editStart.setOnClickListener {
            selectDateDialog(binding.editStart)
        }
        binding.editEnd.setOnClickListener {
            selectDateDialog(binding.editEnd)
        }
        return binding.root
    }

    fun selectDateDialog(textView: TextView) {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(textView.context, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val pickedDateTime = Calendar.getInstance()
            pickedDateTime.set(year, month, dayOfMonth)
            val result = GregorianCalendar(year, month, dayOfMonth).time
            val dateFormat =
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.uuu'Z'", Locale.getDefault())
            textView.text = dateFormat.format(result)
        }, startYear, startMonth, startDay).show()
    }
}