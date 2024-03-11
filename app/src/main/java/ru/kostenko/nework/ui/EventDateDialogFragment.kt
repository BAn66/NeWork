package ru.kostenko.nework.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import ru.kostenko.nework.databinding.DialogPickDateTimeBinding
import ru.kostenko.nework.util.AndroidUtils

class EventDateDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DialogPickDateTimeBinding.inflate(layoutInflater)

        binding.cancelBtn.setOnClickListener {
            dismiss()
        }
        binding.okBtn.setOnClickListener {
            val dateEvent = binding.dateEdit.text.toString()
            val timeEvent = binding.timeEdit.text.toString()
            parentFragmentManager.setFragmentResult("setDateEvent", bundleOf(DateEventFragment.DATEEVENT to dateEvent))
            parentFragmentManager.setFragmentResult("setTimeEvent", bundleOf(DateEventFragment.TIMEEVENT to timeEvent))
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