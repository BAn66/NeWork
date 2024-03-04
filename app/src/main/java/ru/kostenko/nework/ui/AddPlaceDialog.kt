package ru.kostenko.nework.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.kostenko.nework.viewmodel.EventViewModel
import ru.kostenko.nework.viewmodel.PostViewModel

@AndroidEntryPoint
class AddPlaceDialog : DialogFragment() {
    val postViewModel: PostViewModel by activityViewModels()//Диалог добавления места в список
    val eventViewModel: EventViewModel by activityViewModels()

    companion object {
        private const val LAT_KEY = "LAT_KEY"
        private const val LONG_KEY = "LONG_KEY"

        fun createBundle(lat: Double, long: Double): Bundle = bundleOf(
            LAT_KEY to lat, LONG_KEY to long
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = AppCompatEditText(requireContext())
        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Set position?")
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                findNavController().popBackStack()
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->

                postViewModel.setCoords(
                    requireArguments().getDouble(LAT_KEY),
                    requireArguments().getDouble(LONG_KEY)
                )
                Log.d("PostTAAG", "При установке координат поста: ${postViewModel.coords.value}")

                eventViewModel.setCoords(
                    requireArguments().getDouble(LAT_KEY),
                    requireArguments().getDouble(LONG_KEY)
                )
                Log.d("EventTAAG", "При установке координат события: ${eventViewModel.coords.value}")
                findNavController().popBackStack()
            }

            .create()
    }
}