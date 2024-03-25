package ru.kostenko.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.R
import ru.kostenko.nework.databinding.FragmentDateEventBinding
import ru.kostenko.nework.dto.EventType
import ru.kostenko.nework.viewmodel.EventViewModel

@AndroidEntryPoint
class DateEventFragment : BottomSheetDialogFragment() {

    private val eventViewModel: EventViewModel by activityViewModels()

    companion object {
        const val TAG = "ModalBottomSheet"
        const val DATEEVENT = "dateEvent"
        const val TIMEEVENT = "timeEvent"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDateEventBinding.inflate(layoutInflater)
        binding.online.isChecked = true
        eventViewModel.setEventType(EventType.ONLINE)

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.select_date)
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

        setFragmentResultListener("setDateEvent") { _, bundle1 ->
            val date = bundle1.getString(DATEEVENT)
            setFragmentResultListener("setTimeEvent") { _, bundle2 ->
                val time = bundle2.getString(TIMEEVENT)
                val str = "$date $time"
                binding.editDateEvent.setText(str)
                eventViewModel.setDateTime(binding.editDateEvent.text.toString())
            }
        }

        binding.radioGroup.checkedRadioButtonId

        binding.online.setOnCheckedChangeListener { _, _ ->
            eventViewModel.setEventType(EventType.OFFLINE)
        }

        binding.offline.setOnCheckedChangeListener { _, _ ->
            eventViewModel.setEventType(EventType.ONLINE)
        }

        return binding.root
    }

}