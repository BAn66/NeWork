package ru.kostenko.nework.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import ru.kostenko.nework.databinding.FragmentJobDateDialogBinding
import ru.kostenko.nework.util.AndroidUtils

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
            context?.let { item ->
                AndroidUtils.pickDateForJob(binding.editStart, item)
            }
        }
        View.OnClickListener {
            context?.let { item ->
                AndroidUtils.pickDateForJob(binding.editEnd, item)
            }
        }

        binding.startInputLayout.setOnClickListener {
            context?.let { item ->
                AndroidUtils.pickDateForJob(binding.editStart, item)
            }
        }
        binding.endInputLayout.setOnClickListener {
            context?.let { item ->
                AndroidUtils.pickDateForJob(binding.editEnd, item)
            }
        }
        binding.editStart.setOnClickListener {
            context?.let { item ->
                AndroidUtils.pickDateForJob(binding.editStart, item)
            }
        }
        binding.editEnd.setOnClickListener {
            context?.let { item ->
                AndroidUtils.pickDateForJob(binding.editEnd, item)
            }
        }
        return binding.root
    }
}