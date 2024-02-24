package ru.kostenko.nework.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import ru.kostenko.nework.R

//class JobDateDialogFragment : DialogFragment() {
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
////        return activity?.let {
//////            val builder = AlertDialog.Builder(it)
//////            builder.setTitle("Do you want to add a post?")
//////                .setMessage("To add a post, you must be logged in.")
//////                .setIcon(R.drawable.baseline_question_mark_24)
//////                .setPositiveButton("Login") {
//////                        dialog, id ->
//////                    findNavController().navigate(R.id.action_mainFragment_to_authFragment)
//////                }
//////                .setNegativeButton("Registration") {
//////                        dialog, id ->
//////                    findNavController().navigate(R.id.action_mainFragment_to_registrationFragment)
//////                }
//////                .setNeutralButton("Back"){
//////                        dialog, id -> dialog.cancel()
//////                }
//////            builder.create()
////        } ?: throw IllegalStateException("Activity cannot be null")
//    }
//}