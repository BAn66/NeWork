package ru.kostenko.nework.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.R
import ru.kostenko.nework.viewmodel.PostViewModel

@AndroidEntryPoint
class AddPlaceDialog : DialogFragment() {
    val viewModel: PostViewModel by activityViewModels()//Диалог добавления места в список

    companion object {
        private const val LAT_KEY = "LAT_KEY"
        private const val LONG_KEY = "LONG_KEY"

        fun createBundle(lat: Double, long: Double): Bundle = bundleOf(
            LAT_KEY to lat, LONG_KEY to long
        )

//        fun newInstance(lat: Double, long: Double, id: Long? = null) = AddPlaceDialog().apply{
//            arguments = bundleOf(LAT_KEY to lat, LONG_KEY to long, ID_KEY to id)
//        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = AppCompatEditText(requireContext())
        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Set position?")
            .setNegativeButton(android.R.string.cancel){_, _ ->
                findNavController().popBackStack()
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.setCoords(
                    requireArguments().getDouble(LAT_KEY),
                    requireArguments().getDouble(LONG_KEY)
                )
                Log.d("KCoords", "onCreateView: ${viewModel.coords.value}")
                requireParentFragment().findNavController().navigate(R.id.action_addPlace_to_newPostFragment)
            }

                .create()
    }
}